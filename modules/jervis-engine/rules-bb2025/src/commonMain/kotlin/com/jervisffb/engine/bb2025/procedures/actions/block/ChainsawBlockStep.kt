package com.jervisffb.engine.bb2025.procedures.actions.block

import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndActionWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.bb2025.procedures.actions.blitz.BlitzAction
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetBallLocation
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.SetCurrentBall
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
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ActivatePlayerContext
import com.jervisffb.engine.model.context.BlitzActionContext
import com.jervisffb.engine.model.context.ChainsawContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.context.hasContext
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.reports.ReportSkillUsed
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.Bounce
import com.jervisffb.engine.rules.common.procedures.actions.block.FoulAppearanceContext
import com.jervisffb.engine.rules.common.procedures.actions.block.FoulAppearanceRoll
import com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryContext
import com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryMode
import com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryRoll
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Procedure for handling the "Chainsaw"-part of a Chainsaw Special
 * Action. It is in its own procedure, so we can more easily support Block,
 * Blitz, and Frenzy.
 *
 * See [ChainsawAction], and [BlitzAction].
 * Using Chainsaw for fouling is handled in [ChainsawFoulStep]
 */
object ChainsawBlockStep: Procedure() {
    override val initialNode: Node = DecideOnFirstStep
    override fun onEnterProcedure(state: Game, rules: Rules): Command? {
        val context = state.getContext<ChainsawContext>()
        val isBlitz = state.hasContext<BlitzActionContext>()
        return when (isBlitz) {
            true -> ReportSkillUsed(context.attacker, SkillType.CHAINSAW)
            false -> null
        }
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command? {
        val context = state.getContext<ChainsawContext>()
        val activateContext = state.getContext<ActivatePlayerContext>()
        return if (context.kickbackRoll != null) {
            // For a Block, this doesn't matter, but during a Blitz, the player is not
            // allowed to move further.
            UpdateContext(activateContext.copy(activationEndsImmediately = true))
        } else {
            null
        }
    }
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<ChainsawContext>()
    }

    // During a Blitz or Frenzy, the target is pre-defined, so we can skip this step
    object DecideOnFirstStep: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<ChainsawContext>()
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
                    .filter { state.pitch[it].isOccupied() }
                    .filter { state.pitch[it].player!!.team != attacker.team }
                    .map { state.pitch[it].player!! }
                    .let { SelectPlayer.fromPlayers(it) }

            return listOf(eligibleDefenders, EndActionWhenReady)
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                EndAction -> ExitProcedure()
                is PlayerSelected -> {
                    val context = state.getContext<ChainsawContext>()
                    val selectedPlayer = action.getPlayer(state)
                    compositeCommandOf(
                        UpdateContext(context.copy(
                            defender = selectedPlayer,
                            defenderOriginalCoordinates = selectedPlayer.coordinates
                        )),
                        GotoNode(CheckForFoulAppearance),
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object CheckForFoulAppearance: ParentNode() {
        override fun skipNodeFor(state: Game, rules: Rules): Node? {
            val context = state.getContext<ChainsawContext>()
            val hasFoulAppearance = context.defender?.isSkillAvailable(SkillType.FOUL_APPEARANCE) ?: error("Missing defender: $context")
            return when (hasFoulAppearance) {
                true -> null
                false -> RollForChainsaw
            }
        }
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val breatheContext = state.getContext<ChainsawContext>()
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
                    true -> add(GotoNode(RollForChainsaw))
                    // Chainsaw ends the Action immediately, regardless of a failed Foul Appearance roll or not.
                    false -> addAll(
                        UpdateContext(activePlayerContext.copy(activationEndsImmediately = true)),
                        ExitProcedure()
                    )
                }
            }
        }
    }

    object RollForChainsaw: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ChainsawRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<ChainsawContext>()
            return when (context.isSuccess) {
                true -> GotoNode(ResolveChainsawHittingDefender)
                false -> GotoNode(ResolveAttackerKnockedDown)
            }
        }
    }

    object ResolveAttackerKnockedDown: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<ChainsawContext>()
            val injuryContext = RiskingInjuryContext(
                player = context.attacker,
                causedBy = null,
                mode = RiskingInjuryMode.KNOCKED_DOWN
            )
            return AddContext(injuryContext)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = rules.knockedDownStep
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                RemoveContext<RiskingInjuryContext>(),
                ExitProcedure()
            )
        }
    }

    object ResolveChainsawHittingDefender: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<ChainsawContext>()
            val target = context.defender ?: INVALID_GAME_STATE("Missing target: $context")
            val injuryContext = RiskingInjuryContext(
                player = target,
                causedBy = context.attacker,
                mode = RiskingInjuryMode.CHAINSAW
            )
            return AddContext(injuryContext)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = RiskingInjuryRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<RiskingInjuryContext>()
            return compositeCommandOf(
                RemoveContext(context),
                when (context.player.hasBall()) {
                    true -> GotoNode(BounceBall)
                    false -> ExitProcedure()
                }
            )
        }
    }

    object BounceBall: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<ChainsawContext>()
            val defender = context.defender ?: INVALID_GAME_STATE("Missing defender: $context")
            val ball = defender.ball ?: INVALID_GAME_STATE("Defender did not have a ball: $context")
            val ballCoordinates = context.defenderOriginalCoordinates ?: INVALID_GAME_STATE("Missing ball coordinates: $context")
            return compositeCommandOf(
                SetBallState.bouncing(ball),
                SetBallLocation(ball, ballCoordinates),
                SetCurrentBall(ball),
            )
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = Bounce
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                SetCurrentBall(null),
                ExitProcedure(),
            )
        }
    }
}
