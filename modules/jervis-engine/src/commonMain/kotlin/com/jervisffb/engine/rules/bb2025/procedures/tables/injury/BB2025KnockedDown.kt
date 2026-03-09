package com.jervisffb.engine.rules.bb2025.procedures.tables.injury

import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.ConfirmWhenReady
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.RemovePlayerStatusEffect
import com.jervisffb.engine.commands.SetBallLocation
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.SetCurrentBall
import com.jervisffb.engine.commands.SetPlayerState
import com.jervisffb.engine.commands.SetTurnOver
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.SetContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.model.context.BB2025MultipleBlockContext
import com.jervisffb.engine.model.context.SteadyFootingRollContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.model.modifiers.PlayerStatusEffectType
import com.jervisffb.engine.reports.ReportSkillUsed
import com.jervisffb.engine.reports.ReportSteadyFootingResult
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2025.procedures.skills.SafePairOfHandsStep
import com.jervisffb.engine.rules.common.procedures.Bounce
import com.jervisffb.engine.rules.common.procedures.SteadyFootingRoll
import com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryContext
import com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryMode
import com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryRoll
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Resolve a player being Knocked Down. This includes skills triggering just
 * before being Knocked Down, like Steady Footing, as well as skills triggering
 * after, like Pile Driver.
 *
 * See page 40 in the BB2025 rulebook.
 */
object BB2025KnockedDown: Procedure() {
    override val initialNode: Node = ChooseToUseSteadyFooting
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        val context = state.getContext<RiskingInjuryContext>()
        if (context.player.state == PlayerState.KNOCKED_DOWN) {
            INVALID_GAME_STATE("Player is already knocked down: ${context.player.state}")
        }
        if (context.mode != RiskingInjuryMode.KNOCKED_DOWN && context.mode != RiskingInjuryMode.BAD_LANDING) {
            INVALID_GAME_STATE("Player needs to have a bad landing or be knocked down to use this procedure: ${context.mode}")
        }
    }

    object ChooseToUseSteadyFooting: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team {
            val context = state.getContext<RiskingInjuryContext>()
            return context.player.team
        }
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<RiskingInjuryContext>()
            val hasSteadyFooting = context.player.isSkillAvailable(SkillType.STEADY_FOOTING)
            return when (hasSteadyFooting) {
                true -> listOf(ConfirmWhenReady, CancelWhenReady)
                false -> listOf(ContinueWhenReady)
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<RiskingInjuryContext>()
            val useSteadyFooting = (action == Confirm)
            return when (useSteadyFooting) {
                true -> compositeCommandOf(
                    ReportSkillUsed(context.player, SkillType.STEADY_FOOTING),
                    GotoNode(RollForSteadyFooting)
                )
                false -> GotoNode(ResolveSafePairOfHands)
            }
        }
    }

    object RollForSteadyFooting: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command? {
            val injuryContext = state.getContext<RiskingInjuryContext>()
            val context = SteadyFootingRollContext(injuryContext.player, RiskingInjuryMode.KNOCKED_DOWN)
            return SetContext(context)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = SteadyFootingRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<SteadyFootingRollContext>()
            return if (context.isSuccess) {
                compositeCommandOf(
                    ReportSteadyFootingResult(context, RiskingInjuryMode.KNOCKED_DOWN),
                    ExitProcedure()
                )
            } else {
                GotoNode(ResolveSafePairOfHands)
            }
        }
    }

    object ResolveSafePairOfHands: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = SafePairOfHandsStep
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(KnockdownPlayer)
        }
    }

    object KnockdownPlayer: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<RiskingInjuryContext>()
            val player = context.player
            val rootedStatus = player.statusEffects.firstOrNull { it.type == PlayerStatusEffectType.ROOTED }
            return buildCompositeCommand {
                add(SetPlayerState(player, PlayerState.KNOCKED_DOWN, hasTackleZones = false))
                if (rootedStatus != null) {
                    add(RemovePlayerStatusEffect(player, rootedStatus))
                }
                // Note, in BB2020, thrown players where knocked down after landing on other players,
                // but this was explicitly not a turnover (after errata). The wording in BB2025
                // is the original wording, so landing on your team players is always a turnover for now.
                val isOnActiveTeam = (context.player.team == state.activeTeam)
                if (isOnActiveTeam) {
                    add(SetTurnOver(TurnOver.STANDARD))
                }
                if (player.hasBall()) {
                    val ball = player.ball!!
                    addAll(
                        SetCurrentBall(ball),
                        SetBallLocation(ball, player.coordinates),
                        SetBallState.bouncing(ball)
                    )
                }
                add(GotoNode(RollForInjury))
            }
        }
    }

    object RollForInjury: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = RiskingInjuryRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<RiskingInjuryContext>()
            // If we are part of a Multiple Block, the bounce is delayed until later,
            // so in that case, we just knock the ball loose and handle it later, otherwise
            // we resolve it here.
            val isBouncing = state.currentBallOrNull()?.state == BallState.BOUNCING
            return when {
                isBouncing && context.isPartOfMultipleBlock -> {
                    val mbContext = state.getContext<BB2025MultipleBlockContext>()
                    compositeCommandOf(
                        SetCurrentBall(null),
                        ExitProcedure()
                    )
                }
                isBouncing && !context.isPartOfMultipleBlock-> {
                    GotoNode(BounceBall)
                }
                else -> {
                    compositeCommandOf(
                        SetCurrentBall(null),
                        ExitProcedure()
                    )
                }
            }
        }
    }

    object BounceBall: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = Bounce
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                SetCurrentBall(null),
                ExitProcedure()
            )
        }
    }
}
