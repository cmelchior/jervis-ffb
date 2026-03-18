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
import com.jervisffb.engine.model.context.BlitzActionContext
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.context.hasContext
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.reports.ReportSkillUsed
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.BlockAction
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.HitAndRunStep
import com.jervisffb.engine.rules.common.procedures.actions.blitz.BlitzAction
import com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryContext
import com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryMode
import com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryRoll
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.INVALID_ACTION

data class StabContext(
    val attacker: Player,
    val defender: Player? = null,
    val stabResult: RiskingInjuryContext? = null,
): ProcedureContext

/**
 * Procedure for handling the "Stab"-part of a Stab Special Action. It is in
 * its own procedure, so we can more easily support Block, Frenzy, and Multiple
 * Block.
 *
 * See [StabAction], [BlockAction] and [BlitzAction].
 */
object StabStep: Procedure() {
    override val initialNode: Node = DecideOnFirstStep
    override fun onEnterProcedure(state: Game, rules: Rules): Command? {
        val context = state.getContext<StabContext>()
        val isBlitz = state.hasContext<BlitzActionContext>()
        return when (isBlitz) {
            true -> ReportSkillUsed(context.attacker, SkillType.STAB)
            false -> null
        }
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command? {
        val context = state.getContext<StabContext>()
        val activateContext = state.getContext<ActivatePlayerContext>()
        return if (context.stabResult != null) {
            // For a Block, this doesn't matter, but during a Blitz, the player is not
            // allowed to move further.
            UpdateContext(activateContext.copy(activationEndsImmediately = true))
        } else {
            null
        }
    }
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<StabContext>()
    }

    // During a Blitz, the target is pre-defined, so we can skip this step
    object DecideOnFirstStep: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<StabContext>()
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
            val eligibleDefenders =
                attacker.coordinates.getSurroundingCoordinates(rules)
                    .filter { state.field[it].isOccupied() }
                    .filter { state.field[it].player!!.team != attacker.team }
                    .map { state.field[it].player!! }

            return when (eligibleDefenders.isNotEmpty()) {
                true -> listOf(SelectPlayer.fromPlayers(eligibleDefenders), EndActionWhenReady)
                false -> listOf(EndActionWhenReady)
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                EndAction -> ExitProcedure()
                is PlayerSelected -> {
                    val context = state.getContext<StabContext>().copy(defender = action.getPlayer(state))
                    compositeCommandOf(
                        UpdateContext(context),
                        GotoNode(CheckForFoulAppearance),
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object CheckForFoulAppearance: ParentNode() {
        override fun skipNodeFor(state: Game, rules: Rules): Node? {
            val context = state.getContext<StabContext>()
            val hasFoulAppearance = context.defender?.isSkillAvailable(SkillType.FOUL_APPEARANCE) ?: error("Missing defender: $context")
            return when (hasFoulAppearance) {
                true -> null
                false -> RollForArmourAnInjury
            }
        }
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val breatheContext = state.getContext<StabContext>()
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
                    true -> add(GotoNode(RollForArmourAnInjury))
                    // Stab ends the Action immediately, regardless of a failed Foul Appearance roll or not.
                    false -> addAll(
                        UpdateContext(activePlayerContext.copy(activationEndsImmediately = true)),
                        ExitProcedure()
                    )
                }
            }
        }
    }

    object RollForArmourAnInjury : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<StabContext>()
            val injuryContext = RiskingInjuryContext(
                player = context.defender!!,
                causedBy = context.attacker,
                mode = RiskingInjuryMode.STAB
            )
            return AddContext(injuryContext)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = RiskingInjuryRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val injuryContext = state.getContext<RiskingInjuryContext>()
            val stabContext = state.getContext<StabContext>()
            return compositeCommandOf(
                UpdateContext(stabContext.copy(
                    stabResult = injuryContext
                )),
                RemoveContext<RiskingInjuryContext>(),
                GotoNode(CheckForHitAndRun),
            )
        }
    }

    object CheckForHitAndRun: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = HitAndRunStep
        override fun onExitNode(state: Game, rules: Rules): Command {
            return ExitProcedure()
        }
    }
}
