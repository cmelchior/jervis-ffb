package com.jervisffb.engine.rules.common.procedures.actions.block

import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndActionWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ActivatePlayerContext
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.context.hasContext
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.reports.ReportBreatheFireResult
import com.jervisffb.engine.reports.ReportSkillUsed
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.BlockAction
import com.jervisffb.engine.rules.bb2025.procedures.tables.injury.BB2025KnockedDown
import com.jervisffb.engine.rules.bb2025.procedures.tables.injury.BB2025PlacedProne
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.rules.common.procedures.actions.blitz.BlitzAction
import com.jervisffb.engine.rules.common.procedures.actions.blitz.BlitzActionContext
import com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryContext
import com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryMode
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.INVALID_ACTION

enum class BreatheFireResult {
    ATTACKER_KNOCKED_DOWN,
    TARGET_PLACED_PRONE,
    TARGET_KNOCKED_DOWN,
    NO_EFFECT
}

data class BreatheFireContext(
    val attacker: Player,
    val defender: Player? = null,
    val breatheRoll: D6DieRoll? = null,
    val result: BreatheFireResult? = null,
    val injuryResult: RiskingInjuryContext? = null,
): ProcedureContext

/**
 * Procedure for handling the "Breathe Fire"-part of a Breathe Fire Special
 * Action. It is in its own procedure, so we can more easily support Block,
 * Blitz, and Frenzy.
 *
 * See [BreatheFireAction], [BlockAction] and [BlitzAction].
 */
object BreatheFireStep: Procedure() {
    override val initialNode: Node = DecideOnFirstStep
    override fun onEnterProcedure(state: Game, rules: Rules): Command? {
        val context = state.getContext<BreatheFireContext>()
        val isBlitz = state.hasContext<BlitzActionContext>()
        return when (isBlitz) {
            true -> ReportSkillUsed(context.attacker, SkillType.BREATHE_FIRE)
            false -> null
        }
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command? {
        val context = state.getContext<BreatheFireContext>()
        val activateContext = state.getContext<ActivatePlayerContext>()
        return if (context.injuryResult != null) {
            // For a Block, this doesn't matter, but during a Blitz, the player is not
            // allowed to move further.
            UpdateContext(activateContext.copy(activationEndsImmediately = true))
        } else {
            null
        }
    }
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<BreatheFireContext>()
    }

    // During a Blitz or Frenzy, the target is pre-defined, so we can skip this step
    object DecideOnFirstStep: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<BreatheFireContext>()
            return when (context.defender != null) {
                true -> GotoNode(CheckForFoulAppearance)
                false -> GotoNode(SelectDefenderOrEndAction)
            }
        }
    }

    object SelectDefenderOrEndAction : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.activePlayer!!.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val attacker = state.activePlayer!!
            val eligibleDefenders: GameActionDescriptor =
                attacker.coordinates.getSurroundingCoordinates(rules)
                    .filter { state.field[it].isOccupied() }
                    .filter { state.field[it].player!!.team != attacker.team }
                    .map { state.field[it].player!! }
                    .let { SelectPlayer.fromPlayers(it) }

            return listOf(eligibleDefenders, EndActionWhenReady)
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                EndAction -> ExitProcedure()
                is PlayerSelected -> {
                    val context = state.getContext<BreatheFireContext>()
                    compositeCommandOf(
                        UpdateContext(context.copy(defender = action.getPlayer(state))),
                        GotoNode(CheckForFoulAppearance),
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object CheckForFoulAppearance: ParentNode() {
        override fun skipNodeFor(state: Game, rules: Rules): Node? {
            val context = state.getContext<BreatheFireContext>()
            val hasFoulAppearance = context.defender?.isSkillAvailable(SkillType.FOUL_APPEARANCE) ?: error("Missing defender: $context")
            return when (hasFoulAppearance) {
                true -> null
                false -> RollForBreatheFire
            }
        }
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val breatheContext = state.getContext<BreatheFireContext>()
            val foulAppearanceContext = FoulAppearanceContext(breatheContext.attacker, breatheContext.defender!!)
            return AddContext(foulAppearanceContext)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = FoulAppearanceRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val activePlayerContext = state.getContext<ActivatePlayerContext>()
            val context = state.getContext<FoulAppearanceContext>()
            return buildCompositeCommand {
                add(RemoveContext<FoulAppearanceContext>())
                when (context.isSuccess) {
                    true -> add(GotoNode(RollForBreatheFire))
                    // Breathe Fire ends the Action immediately, regardless of a failed Foul Appearance roll or not.
                    false -> addAll(
                        UpdateContext(activePlayerContext.copy(activationEndsImmediately = true)),
                        ExitProcedure()
                    )
                }
            }
        }
    }

    object RollForBreatheFire: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = BreatheFireRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<BreatheFireContext>()
            val diceRoll = context.breatheRoll?.result ?: error("Missing die roll: $context")

            val defender = context.defender!!
            val modifier = if (defender.strength >= 5) -1 else 0
            val d6Result = diceRoll.value + modifier
            val breathFireResult = when {
                d6Result == 6 -> BreatheFireResult.TARGET_KNOCKED_DOWN
                d6Result >= 4 -> BreatheFireResult.TARGET_PLACED_PRONE
                d6Result in 2..3 -> BreatheFireResult.NO_EFFECT
                d6Result <= 1 -> BreatheFireResult.ATTACKER_KNOCKED_DOWN
                else -> error("Unsupported value: $d6Result")
            }
            val updatedContext = context.copy(result = breathFireResult)

            return buildCompositeCommand {
                add(ReportBreatheFireResult(updatedContext))
                add(UpdateContext(updatedContext))
                val nextNode = when (breathFireResult) {
                    BreatheFireResult.ATTACKER_KNOCKED_DOWN -> ResolveAttackerKnockedDown
                    BreatheFireResult.TARGET_PLACED_PRONE -> ResolveDefenderPlacedProne
                    BreatheFireResult.TARGET_KNOCKED_DOWN -> ResolveDefenderKnockedDown
                    BreatheFireResult.NO_EFFECT -> ResolveNoEffect
                }
                add(GotoNode(nextNode))
            }
        }
    }

    object ResolveAttackerKnockedDown: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<BreatheFireContext>()
            val injuryContext = RiskingInjuryContext(
                player = context.attacker,
                causedBy = null, // The opponent does not get to use their skills on a failed Breathe Fire
                mode = RiskingInjuryMode.KNOCKED_DOWN
            )
            return AddContext(injuryContext)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = BB2025KnockedDown
        override fun onExitNode(state: Game, rules: Rules): Command {
            val injuryContext = state.getContext<RiskingInjuryContext>()
            val breatheContext = state.getContext<BreatheFireContext>()
            return compositeCommandOf(
                UpdateContext(breatheContext.copy(
                    injuryResult = injuryContext
                )),
                RemoveContext<RiskingInjuryContext>(),
                ExitProcedure()
            )
        }
    }

    object ResolveDefenderPlacedProne: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<BreatheFireContext>()
            val injuryContext = RiskingInjuryContext(
                player = context.defender!!,
                causedBy = null, // The opponent does not get to use their skills on a failed Breathe Fire
                mode = RiskingInjuryMode.PLACED_PRONE
            )
            return AddContext(injuryContext)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = BB2025PlacedProne
        override fun onExitNode(state: Game, rules: Rules): Command {
            val injuryContext = state.getContext<RiskingInjuryContext>()
            val breatheContext = state.getContext<BreatheFireContext>()
            return compositeCommandOf(
                UpdateContext(breatheContext.copy(
                    injuryResult = injuryContext
                )),
                RemoveContext<RiskingInjuryContext>(),
                ExitProcedure()
            )
        }
    }

    object ResolveDefenderKnockedDown: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<BreatheFireContext>()
            val injuryContext = RiskingInjuryContext(
                player = context.defender!!,
                causedBy = null, // The attacker does not get to use other skills on the armour/injury roll
                mode = RiskingInjuryMode.KNOCKED_DOWN
            )
            return AddContext(injuryContext)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = BB2025KnockedDown
        override fun onExitNode(state: Game, rules: Rules): Command {
            val injuryContext = state.getContext<RiskingInjuryContext>()
            val breatheContext = state.getContext<BreatheFireContext>()
            return compositeCommandOf(
                UpdateContext(breatheContext.copy(
                    injuryResult = injuryContext
                )),
                RemoveContext<RiskingInjuryContext>(),
                ExitProcedure()
            )
        }
    }

    object ResolveNoEffect: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            return ExitProcedure()
        }
    }
}
