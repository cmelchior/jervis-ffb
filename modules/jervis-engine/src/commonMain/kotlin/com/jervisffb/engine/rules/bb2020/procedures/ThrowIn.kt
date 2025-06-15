package com.jervisffb.engine.rules.bb2020.procedures

import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetBallLocation
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.SetContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.checkDiceRoll
import com.jervisffb.engine.fsm.checkDiceRollList
import com.jervisffb.engine.model.Ball
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.utils.assert
import com.jervisffb.engine.utils.sum

data class ThrowInContext(
    val ball: Ball,
    val outOfBoundsAt: FieldCoordinate,
    val directionRoll: D3Result? = null,
    val direction: Direction? = null,
    val distance: List<D6Result> = emptyList(),
): ProcedureContext

/**
 * Resolve a Throw In after a ball went out of bounds, up until the ball is caught
 * or lands on an empty square. This includes checking for touchdowns.
 *
 * See page 51 in the rulebook.
 */
object ThrowIn : Procedure() {
    override val initialNode: Node = RollDirection
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<ThrowInContext>()

    object RollDirection : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team? = null

        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D3))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRoll<D3Result>(action) { d3 ->
                val context = state.getContext<ThrowInContext>()
                val direction = rules.throwIn(context.outOfBoundsAt, d3)
                val ball = context.ball
                return compositeCommandOf(
                    SetContext(context.copy(
                        directionRoll =  d3,
                        direction = direction,
                    )),
                    SetBallState.thrownIn(ball),
                    GotoNode(RollDistance)
                )
            }
        }
    }

    object RollDistance : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team? = null
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D6, Dice.D6))
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRollList<D6Result>(action) { dice ->
                assert(dice.size == 2)
                val context = state.getContext<ThrowInContext>()
                val distance = dice.sum()

                // Move the ball the entire distance until it either goes out of bounds again
                // or hit an empty location
                val direction = context.direction!!
                val ball = context.ball
                var ballPosition = context.outOfBoundsAt
                var outOfBoundsAt: FieldCoordinate? = null
                for (d in 1..distance) {
                    val start = ballPosition
                    ballPosition = start.move(direction, 1)
                    if (ballPosition.isOutOfBounds(rules)) {
                        outOfBoundsAt = start
                        break
                    }
                }

                return if (outOfBoundsAt != null) {
                    compositeCommandOf(
                        SetContext(context.copy(distance = dice)),
                        SetBallState.outOfBounds(ball, outOfBoundsAt),
                        GotoNode(ResolveOutOfBounds)
                    )
                } else {
                    compositeCommandOf(
                        SetContext(context.copy(distance = dice)),
                        SetBallLocation(ball, ballPosition),
                        GotoNode(ResolveLandOnField)
                    )
                }
            }
        }
    }

    object ResolveOutOfBounds : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command? {
            // Replace the current throw in context
            // TODO Does this ruin reporting logging?
            val oldContext = state.getContext<ThrowInContext>()
            return SetContext(ThrowInContext(oldContext.ball, oldContext.ball.outOfBoundsAt!!))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ThrowIn
        override fun onExitNode(state: Game, rules: Rules): Command {
            return ExitProcedure()
        }
    }

    object ResolveLandOnField : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command? {
            val ball = state.getContext<ThrowInContext>().ball
            val canCatch = state.field[ball.location].player?.let { rules.canCatch(state, it) } ?: false
            return if (!canCatch) {
                SetBallState.bouncing(ball)
            } else {
                null
            }
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            val ball = state.getContext<ThrowInContext>().ball
            return if (ball.state != BallState.BOUNCING) {
                Catch
            } else {
                Bounce
            }
        }
        override fun onExitNode(state: Game, rules: Rules): Command {
            return ExitProcedure()
        }
    }
}
