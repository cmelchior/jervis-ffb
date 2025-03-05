package com.jervisffb.engine.rules.bb2020.procedures

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetContext
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.checkDiceRoll
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.skills.DiceRollType

data class DeviateRollContext(
    val from: FieldCoordinate,
    val deviateRoll: List<DieResult> = emptyList(),
    val landsAt: FieldCoordinate? = null, // Will be `null` if out of bounds
    val outOfBoundsAt: FieldCoordinate? = null, // Will contain the last field before the ball went out of bounds.
): ProcedureContext

/**
 * Resolve a Deviate Roll.
 *
 * Note, this procedure does not move the ball or change its state, it only save the result inside
 * [DeviateRollContext]. It is up to the parent procedure to handle it.
 *
 * See page 25 in the rulebook.
 */
object DeviateRoll : Procedure() {
    override val initialNode: Node = RollDice
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<DeviateRollContext>()
        val ball = state.field[state.getContext<DeviateRollContext>().from].balls.single()
        if (ball.state != BallState.DEVIATING && ball.state != BallState.IN_AIR) {
            throw IllegalStateException("Ball is not deviating, but ${ball.state}")
        }
    }

    object RollDice : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team? = null
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D8, Dice.D6))
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRoll<D8Result, D6Result>(action) { d8, d6 ->
                val context = state.getContext<DeviateRollContext>()
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
                    SetContext(context.copy(
                        deviateRoll = listOf(d8, d6),
                        landsAt = if (outOfBoundsAt == null) currentLocation else null,
                        outOfBoundsAt = outOfBoundsAt,
                    )),
                    ExitProcedure()
                )
            }
        }
    }
}
