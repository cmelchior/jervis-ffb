package com.jervisffb.engine.rules.common.procedures

import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetBallLocation
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.castDiceRoll
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.context.CatchContext
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.reports.ReportBounce
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Resolve a Bounce until it is either caught or lands on the ground.
 * If caught, this includes checking for touchdowns.
 *
 * See page 25 in the BB2020 rulebook.
 * See page 34 in the BB2025 rulebook.
 */
object Bounce : Procedure() {
    override val initialNode: Node = RollDirection
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        val ball = state.currentBall()
        if (ball.state != BallState.BOUNCING) {
            throw IllegalStateException("Ball is not bouncing, but ${ball.state}")
        }
    }

    object RollDirection : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules) = null
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D8))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castDiceRoll<D8Result>(action) { d8 ->
                val direction: Direction = rules.direction(d8)
                val ball = state.currentBall()
                val newLocation: PitchCoordinate = ball.coordinates.move(direction, 1)

                // Out of bounds is normally just outside the pitch, but during kick-off we need to
                // consider the case where the ball bounces back to the kicking teams side.
                val isDuringKickOff = state.stack.containsNode(StartOfDriveSequence.KickOffEvent)
                val isOnKickingTeamSide = if (state.kickingTeam.isHomeTeam()) newLocation.isOnHomeSide(rules) else newLocation.isOnAwaySide(rules)
                val outOfBounds: Boolean = newLocation.isOutOfBounds(rules) || (isDuringKickOff && isOnKickingTeamSide)
                val playerAtTarget: Player? = if (!outOfBounds) state.pitch[newLocation].player else null

                val nextNode: Command =
                    if (outOfBounds) {
                        compositeCommandOf(
                            SetBallLocation(ball, newLocation),
                            SetBallState.outOfBounds(ball, ball.coordinates),
                            if (state.abortIfBallOutOfBounds) {
                                ExitProcedure()
                            } else {
                                GotoNode(ResolveThrowIn)
                            },
                        )
                    } else if (playerAtTarget != null) {
                        val eligiblePlayerForCatching = rules.canCatch(playerAtTarget)
                        if (eligiblePlayerForCatching) {
                            GotoNode(ResolveCatch)
                        } else {
                            GotoNode(ResolveBounce)
                        }
                    } else {
                        GotoNode(ResolveLandingOnTheGround)
                    }

                return compositeCommandOf(
                    ReportDiceRoll(DiceRollType.BOUNCE, d8),
                    SetBallLocation(ball, newLocation),
                    ReportBounce(
                        bounceLocation = newLocation,
                        outOfBoundsAt = if (outOfBounds) ball.coordinates else null,
                        crossedLineOfScrimmageDuringKickOff = (isDuringKickOff && isOnKickingTeamSide)
                    ),
                    nextNode,
                )
            }
        }
    }

    object ResolveLandingOnTheGround : ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val ball = state.currentBall()
            return compositeCommandOf(
                SetBallState.onGround(ball),
                ExitProcedure(),
            )
        }
    }

    object ResolveThrowIn : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val ball = state.currentBall()
            return AddContext(
                ThrowInContext(
                    ball = ball,
                    outOfBoundsAt = ball.outOfBoundsAt!!,
                )
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

    object ResolveBounce : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = Bounce
        override fun onExitNode(state: Game, rules: Rules): Command = ExitProcedure()
    }

    object ResolveCatch : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val ball = state.currentBall()
            val player = state.pitch[ball.coordinates].player ?: INVALID_GAME_STATE("Missing player on: ${ball.coordinates}")
            return AddContext(CatchContext(player, ball))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = Catch
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                RemoveContext<CatchContext>(),
                ExitProcedure()
            )
        }
    }
}
