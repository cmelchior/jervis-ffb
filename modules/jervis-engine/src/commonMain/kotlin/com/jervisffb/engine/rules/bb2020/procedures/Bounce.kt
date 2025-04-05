package com.jervisffb.engine.rules.bb2020.procedures

import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.RemoveContext
import com.jervisffb.engine.commands.SetBallLocation
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.SetContext
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.checkType
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.reports.ReportBounce
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules

/**
 * Resolve a Bounce.
 *
 * See page 25 in the rulebook.
 */
object Bounce : Procedure() {
    override val initialNode: Node = RollDirection
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        val ball = state.currentBall()
        if (ball.state != BallState.BOUNCING) throw IllegalStateException("Ball is not bouncing, but ${ball.state}")
    }

    object RollDirection : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules) = null
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D8))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkType<D8Result>(action) { d8 ->
                val direction: Direction = rules.direction(d8)
                val ball = state.currentBall()
                val newLocation: FieldCoordinate = ball.location.move(direction, 1)
                val outOfBounds: Boolean = newLocation.isOutOfBounds(rules)
                val playerAtTarget: Player? = if (!outOfBounds) state.field[newLocation].player else null
                val nextNode: Command =
                    if (outOfBounds) {
                        // TODO Throw-in or trigger touchback
                        // TODO For kick-offs, bouncing across the halfline is also considered out-of-bounds
                        compositeCommandOf(
                            SetBallState.outOfBounds(ball, ball.location),
                            if (state.abortIfBallOutOfBounds) {
                                ExitProcedure()
                            } else {
                                GotoNode(ResolveThrowIn)
                            },
                        )
                    } else if (playerAtTarget != null) {
                        val eligiblePlayerForCatching = rules.canCatch(state, playerAtTarget)
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
                    ReportBounce(newLocation, if (outOfBounds) ball.location else null),
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
            return SetContext(
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
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = Catch
        override fun onExitNode(state: Game, rules: Rules): Command = ExitProcedure()
    }
}
