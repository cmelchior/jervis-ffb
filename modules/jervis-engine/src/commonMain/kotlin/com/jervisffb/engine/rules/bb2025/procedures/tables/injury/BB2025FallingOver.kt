package com.jervisffb.engine.rules.bb2025.procedures.tables.injury

import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.ConfirmWhenReady
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.commands.Command
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
import com.jervisffb.engine.model.context.SteadyFootingRollContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.isSkillAvailable
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
 * Resolve a player falling over.
 *
 * See page 40 in the BB2025 rulebook.
 */
object BB2025FallingOver: Procedure() {
    override val initialNode: Node = ChooseToUseSteadyFooting
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? {
        return when (state.currentBallOrNull() != null) {
            true -> SetCurrentBall(null)
            false -> null
        }
    }
    override fun isValid(state: Game, rules: Rules) {
        val context = state.getContext<RiskingInjuryContext>()
        if (context.player.state == PlayerState.FALLEN_OVER) {
            INVALID_GAME_STATE("Player is already falling over: ${context.player}")
        }
        if (context.mode != RiskingInjuryMode.FALLING_OVER && context.mode != RiskingInjuryMode.BAD_LANDING) {
            INVALID_GAME_STATE("Player needs to have landed badly or be falling over oto use this procedure: ${context.mode}")
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
            return if (useSteadyFooting) {
                compositeCommandOf(
                    ReportSkillUsed(context.player, SkillType.STEADY_FOOTING),
                    GotoNode(RollForSteadyFooting)
                )
            } else {
                GotoNode(ResolveSafePairOfHands)
            }
        }
    }

    object RollForSteadyFooting: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command? {
            val injuryContext = state.getContext<RiskingInjuryContext>()
            val context = SteadyFootingRollContext(injuryContext.player, RiskingInjuryMode.FALLING_OVER)
            return SetContext(context)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = SteadyFootingRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<SteadyFootingRollContext>()
            return if (context.isSuccess) {
                compositeCommandOf(
                    ReportSteadyFootingResult(context, RiskingInjuryMode.FALLING_OVER),
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
            return GotoNode(PlayerFallsOver)
        }
    }

    object PlayerFallsOver: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<RiskingInjuryContext>()
            val player = context.player
            return buildCompositeCommand {
                add(SetPlayerState(player, PlayerState.FALLEN_OVER, hasTackleZones = false))
                /**
                 * If the player falling over, carried a ball, they will drop the ball, and it
                 * will bounce from this square.
                 *
                 * In case a ball was lying on the ground in the square the player was falling
                 * over in. It will bounce from the square as part of [com.jervisffb.engine.rules.common.procedures.actions.move.MovePlayerIntoSquare],
                 * so when we get to this procedure and the player drops the ball, there should only
                 * be one ball in the square.
                 */
                if (player.hasBall()) {
                    val ball = player.ball!!
                    addAll(
                        SetCurrentBall(ball),
                        SetBallLocation(ball, player.coordinates),
                        SetBallState.bouncing(ball),
                    )
                }
                // Falling over results in a turn-over pr. the list of turnovers on page 35 in the BB2025 rulebook.
                // But only if they are the active player, or if they are a thrown player with the ball(i.e. thrown
                // players not holding the ball does not trigger this)
                val isOnActiveTeam = (player.team == state.activeTeam)
                val playerThrown = (context.mode == RiskingInjuryMode.BAD_LANDING)
                val hasBall = player.hasBall()
                if ((isOnActiveTeam && !playerThrown) || (isOnActiveTeam && playerThrown && hasBall)) {
                    add(SetTurnOver(TurnOver.STANDARD))
                }
                add(GotoNode(RollForInjury))
            }
        }
    }

    object RollForInjury: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = RiskingInjuryRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<RiskingInjuryContext>()
            val ball = state.currentBallOrNull()
            return if (ball?.state == BallState.BOUNCING) {
                GotoNode(BounceBall)
            } else {
                ExitProcedure()
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
