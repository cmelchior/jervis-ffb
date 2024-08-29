package dk.ilios.jervis.procedures

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.D8Result
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.RollDice
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.BallState
import dk.ilios.jervis.model.FieldCoordinate
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.utils.INVALID_GAME_STATE
import dk.ilios.jervis.utils.assert

data class ScatterRollContext(
    val from: FieldCoordinate,
    val scatterRoll: List<D8Result> = emptyList(),
    val landsAt: FieldCoordinate? = null, // Will be `null` if out of bounds
    val outOfBoundsAt: FieldCoordinate? = null, // Will contain the last field before the ball went out of bounds.
)

/**
 * Resolve a Scatter.
 * Do not try to land the ball after the scatter.
 * Just scatter the ball and let the caller handle the result.
 *
 * See page 25 in the rulebook.
 */
object Scatter : Procedure() {
    override val initialNode: Node = RollDice

    override fun onEnterProcedure(
        state: Game,
        rules: Rules,
    ): Command? {
        if (state.scatterRollContext == null) {
            INVALID_GAME_STATE("Missing scatter roll context")
        }
        val ball = state.field[state.scatterRollContext!!.from].ball
        if (ball?.state != BallState.SCATTERED) {
            throw IllegalStateException("Ball is not scattered, but ${ball?.state}")
        }
        return null
    }

    override fun onExitProcedure(
        state: Game,
        rules: Rules,
    ): Command? = null

    object RollDice : ActionNode() {
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            return listOf(RollDice(Dice.D8, Dice.D8, Dice.D8))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRollList<D8Result>(action) { dice: List<D8Result> ->
                assert(dice.size == 3)
                val context = state.passContext!!
                var scatteredLocation = context.target!!
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
                    SetContext(
                        Game::scatterRollContext, state.scatterRollContext!!.copy(
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
