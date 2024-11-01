package com.jervisffb.engine.rules.bb2020.procedures

import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.checkDiceRollList
import com.jervisffb.engine.model.Ball
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
import com.jervisffb.engine.utils.assert
import com.jervisffb.engine.commands.compositeCommandOf

data class ScatterRollContext(
    val ball: Ball,
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
        if (context.ball.state != BallState.SCATTERED) {
            throw IllegalStateException("Ball is not scattered, but ${context.ball.state}")
        }
    }

    object RollDice : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team? = null

        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
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
