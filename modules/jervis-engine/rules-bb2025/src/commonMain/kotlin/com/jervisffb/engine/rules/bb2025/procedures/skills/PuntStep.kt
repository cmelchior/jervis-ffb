package com.jervisffb.engine.rules.bb2025.procedures.skills

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.SelectDirection
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetBallLocation
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.SetCurrentBall
import com.jervisffb.engine.commands.SetTurnOver
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
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.model.context.PuntContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.ResolveBallLandingOnPitch
import com.jervisffb.engine.rules.common.procedures.ThrowIn
import com.jervisffb.engine.rules.common.procedures.ThrowInContext
import com.jervisffb.engine.rules.common.procedures.actions.punt.PuntAction
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import com.jervisffb.engine.utils.assert

/**
 * Procedure implementing the punt step for a [PuntAction].
 *
 * See page 135 in the BB2025 rulebook.
 */
object PuntStep : Procedure() {
    override val initialNode: Node = SelectTemplateOrientation
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val context = state.getContext<PuntContext>()
        val ball = context.punter.ball ?: INVALID_GAME_STATE("Punter does not have the ball")
        return SetCurrentBall(ball)
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        return SetCurrentBall(null)
    }
    override fun isValid(state: Game, rules: Rules) {
        assert(state.getContext<PuntContext>().punter.ball != null)
    }

    object SelectTemplateOrientation : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<PuntContext>().punter.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<PuntContext>()
            return listOf(
                CancelWhenReady,
                SelectDirection(
                    context.punter.coordinates,
                    listOf(Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT)
                )
            )
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                Cancel -> ExitProcedure()
                is DirectionSelected -> {
                    val context = state.getContext<PuntContext>()
                    compositeCommandOf(
                        UpdateContext(context.copy(
                            selectedDirection = action.direction,
                            hasPunted = true,
                        )),
                        GotoNode(RollDirectionStep)
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object RollDirectionStep : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<PuntContext>()
            val ball = context.punter.ball ?: INVALID_GAME_STATE("Missing ball: $context")
            return SetBallState.inAir(ball)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = PuntDirectionRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(RollDistanceStep)
        }
    }

    object RollDistanceStep : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = PuntDistanceRoll
        override fun onExitNode(state: Game, rules: Rules): Command = GotoNode(ResolveBallLanding)
    }

    object ResolveBallLanding : ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<PuntContext>()
            val ball = state.currentBall()
            val kickDirection = context.kickDirection ?: INVALID_GAME_STATE("Missing kick direction: $context")
            val kickedDistance = context.distanceRoll?.result?.value ?: INVALID_GAME_STATE("Missing distance roll result: $context")

            var ballPosition = context.punter.coordinates
            var outOfBoundsAt: PitchCoordinate? = null
            for (dist in 1..kickedDistance) {
                val start = ballPosition
                ballPosition = start.move(kickDirection, 1)
                if (ballPosition.isOutOfBounds(rules)) {
                    outOfBoundsAt = start
                    break
                }
            }
            return if (outOfBoundsAt != null) {
                compositeCommandOf(
                    SetBallState.outOfBounds(ball, outOfBoundsAt),
                    SetBallLocation(ball, ballPosition),
                    GotoNode(ResolveOutOfBounds)
                )
            } else {
                compositeCommandOf(
                    SetBallLocation(ball, ballPosition),
                    GotoNode(ResolveLandingOnPitch)
                )
            }
        }
    }

    // Regardless of the ball being caught by the punting team, it is still a turn-over.
    // This is different from Catch, where this is allowed as long as the ball ends up
    // in the hands of the team. The turnover is set in `ThrowIn`.
    object ResolveOutOfBounds : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val ball = state.currentBall()
            return compositeCommandOf(
                AddContext(ThrowInContext(ball, ball.outOfBoundsAt!!))
            )
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ThrowIn
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                RemoveContext<ThrowInContext>(),
                ExitProcedure()
            )
        }
    }

    // If the ball when out-of-bounds at any point when resolving landing on the pitch, a turnover was triggered
    // in `ThrowIn`.
    object ResolveLandingOnPitch : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ResolveBallLandingOnPitch
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<PuntContext>()
            val caughtByOpponent = (state.currentBall().carriedBy?.team == context.punter.team.otherTeam())
            return compositeCommandOf(
                if (caughtByOpponent) SetTurnOver(TurnOver.STANDARD) else null,
                ExitProcedure()
            )
        }
    }
}
