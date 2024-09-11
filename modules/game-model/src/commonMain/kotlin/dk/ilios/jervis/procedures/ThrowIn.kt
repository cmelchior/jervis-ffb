package dk.ilios.jervis.procedures

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.D3Result
import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.RollDice
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.SetBallLocation
import dk.ilios.jervis.commands.SetBallState
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.commands.fsm.GotoNode
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.FieldCoordinate
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.context.ProcedureContext
import dk.ilios.jervis.model.context.assertContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.tables.Direction
import dk.ilios.jervis.utils.assert
import dk.ilios.jervis.utils.sum

data class ThrowInContext(
    val outOfBoundsAt: FieldCoordinate,
    val directionRoll: D3Result? = null,
    val direction: Direction? = null,
    val distance: List<D6Result> = emptyList(),
): ProcedureContext

/**
 * Resolve a Throw In after a ball went out of bounds.
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

        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            return listOf(RollDice(Dice.D3))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRoll<D3Result>(action) { d3 ->
                val context = state.getContext<ThrowInContext>()
                val direction = rules.throwIn(context.outOfBoundsAt, d3)
                return compositeCommandOf(
                    SetContext(context.copy(
                        directionRoll =  d3,
                        direction = direction,
                    )),
                    SetBallState.thrownIn(),
                    GotoNode(RollDistance)
                )
            }
        }
    }

    object RollDistance : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team? = null
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
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
                var ballPosition = context.outOfBoundsAt
                var outOfBoundsAt: FieldCoordinate? = null
                for (d in 1..distance) {
                    val start = ballPosition
                    ballPosition = start.move(direction, 1)
                    if (ballPosition.isOutOfBounds(rules)) {
                        outOfBoundsAt = ballPosition
                        break
                    }
                }

                return if (outOfBoundsAt != null) {
                    compositeCommandOf(
                        SetContext(context.copy(distance = dice)),
                        SetBallState.outOfBounds(outOfBoundsAt),
                        SetBallLocation(FieldCoordinate.OUT_OF_BOUNDS),
                        GotoNode(ResolveOutOfBounds)
                    )
                } else {
                    compositeCommandOf(
                        SetContext(context.copy(distance = dice)),
                        SetBallState.thrownIn(),
                        SetBallLocation(ballPosition),
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
            return SetContext(ThrowInContext(state.ball.outOfBoundsAt!!))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ThrowIn
        override fun onExitNode(state: Game, rules: Rules): Command {
            return ExitProcedure()
        }

    }

    object ResolveLandOnField : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            val isStandingPlayer = state.ballSquare.player?.isStanding(rules)
            return if (isStandingPlayer == true) {
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
