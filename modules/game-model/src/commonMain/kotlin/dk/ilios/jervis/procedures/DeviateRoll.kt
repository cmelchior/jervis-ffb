package dk.ilios.jervis.procedures

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.D8Result
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.DieResult
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.RollDice
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.SetOldContext
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.BallState
import dk.ilios.jervis.model.FieldCoordinate
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.reports.ReportDiceRoll
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.DiceRollType
import dk.ilios.jervis.utils.INVALID_GAME_STATE

data class DeviateRollContext(
    val from: FieldCoordinate,
    val deviateRoll: List<DieResult> = emptyList(),
    val landsAt: FieldCoordinate? = null, // Will be `null` if out of bounds
    val outOfBoundsAt: FieldCoordinate? = null, // Will contain the last field before the ball went out of bounds.
)

/**
 * Resolve a Deviate Roll.
 * Do not try to land the ball after the roll, this is up to the caller of this procedure.
 *
 * See page 25 in the rulebook.
 */
object DeviateRoll : Procedure() {
    override val initialNode: Node = RollDice

    override fun onEnterProcedure(
        state: Game,
        rules: Rules,
    ): Command? {
        if (state.deviateRollContext == null) {
            INVALID_GAME_STATE("Missing scatter roll context")
        }
        val ball = state.field[state.deviateRollContext!!.from].ball
        if (ball?.state != BallState.DEVIATING && ball?.state != BallState.IN_AIR) {
            throw IllegalStateException("Ball is not deviating, but ${ball?.state}")
        }
        return null
    }

    override fun onExitProcedure(
        state: Game,
        rules: Rules,
    ): Command? = null

    object RollDice : ActionNode() {
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            return listOf(RollDice(Dice.D8, Dice.D6))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRoll<D8Result, D6Result>(action) { d8, d6 ->
                val context = state.deviateRollContext!!
                val direction = rules.direction(d8)
                val distance = d6.value

                // Move the ball one at a time and check for out of bounds at every move
                var currentLocation = context.from
                var outOfBoundsAt: FieldCoordinate? = null
                for (i in 1..distance) {
                    val start = currentLocation
                    currentLocation = currentLocation.move(direction, 1)
                    if (currentLocation.isOutOfBounds(rules)) {
                        outOfBoundsAt = start
                        break
                    }
                }

                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.DEVIATE, listOf(d8, d6), showDiceType = true),
                    SetOldContext(
                        Game::deviateRollContext, context.copy(
                            deviateRoll = listOf(d8, d6),
                            landsAt = if (outOfBoundsAt == null) currentLocation else null,
                            outOfBoundsAt = outOfBoundsAt,
                        )
                    ),
                    ExitProcedure()
                )
            }
        }
    }
}
