package dk.ilios.jervis.procedures

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.D8Result
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.RollDice
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.BallState
import dk.ilios.jervis.model.FieldCoordinate
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.context.ProcedureContext
import dk.ilios.jervis.model.context.assertContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.reports.ReportDiceRoll
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.DiceRollType
import dk.ilios.jervis.utils.assert

data class ScatterRollContext(
    val from: FieldCoordinate,
    val scatterRoll: List<D8Result> = emptyList(),
    val landsAt: FieldCoordinate? = null, // Will be `null` if out of bounds
    val outOfBoundsAt: FieldCoordinate? = null, // Will contain the last field before the ball went out of bounds.
): ProcedureContext

/**
 * Resolve a Scatter.
 * Do not try to land the ball after the scatter.
 * Just scatter the ball and let the caller handle the result.
 *
 * See page 25 in the rulebook.
 */
object Scatter : Procedure() {
    override val initialNode: Node = RollDice
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<ScatterRollContext>()
        val context = state.getContext<ScatterRollContext>()
        val ball = state.field[context.from].ball
        if (ball?.state != BallState.SCATTERED) {
            throw IllegalStateException("Ball is not scattered, but ${ball?.state}")
        }
    }

    object RollDice : ActionNode() {
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            return listOf(RollDice(Dice.D8, Dice.D8, Dice.D8))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRollList<D8Result>(action) { dice: List<D8Result> ->
                assert(dice.size == 3)
                val context = state.getContext<ScatterRollContext>()
                var scatteredLocation = context.from
                var outOfBoundsAt: FieldCoordinate? = null
                for (diceResult in dice) {
                    val startLocation = scatteredLocation
                    scatteredLocation = scatteredLocation.move(rules.direction(diceResult), 1)
                    if (scatteredLocation.isOutOfBounds(rules)) {
                        outOfBoundsAt = startLocation
                        break
                    }
                }
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.SCATTER, dice),
                    SetContext(
                        state.getContext<ScatterRollContext>().copy(
                            scatterRoll = dice,
                            landsAt = if (outOfBoundsAt == null) scatteredLocation else null,
                            outOfBoundsAt = outOfBoundsAt,
                        )
                    ),
                    ExitProcedure()
                )
            }
        }
    }
}
