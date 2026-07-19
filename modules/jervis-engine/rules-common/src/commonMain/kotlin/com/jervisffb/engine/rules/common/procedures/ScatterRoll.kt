package com.jervisffb.engine.rules.common.procedures

import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.castDiceRollList
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import com.jervisffb.engine.utils.assert

data class ScatterRollContext(
    val from: PitchCoordinate,
    val scatterRoll: List<D8Result> = emptyList(),
    val landsAt: PitchCoordinate? = null,
    val outOfBoundsAt: PitchCoordinate? = null, // Will contain the last square before the ball went out of bounds.
): ProcedureContext

/**
 * Resolve a Scatter.
 *
 * Do not try to land the ball or update its location after the scatter, this is left
 * up to the caller of this procedure.
 *
 * See page 25 in the rulebook.
 */
object ScatterRoll : Procedure() {
    override val initialNode: Node = RollDice
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<ScatterRollContext>()
        state.currentBallOrNull()?.let {
            if (it.state != BallState.SCATTERED) {
                INVALID_GAME_STATE("Ball is not scattered, but ${it.state}")
            }
        }
    }

    object RollDice : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team? = null

        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D8, Dice.D8, Dice.D8))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castDiceRollList<D8Result>(action) { dice: List<D8Result> ->
                assert(dice.size == 3)
                val context = state.getContext<ScatterRollContext>()
                var scatteredLocation = context.from
                var outOfBoundsAt: PitchCoordinate? = null
                for (diceResult in dice) {
                    val startLocation = scatteredLocation
                    scatteredLocation = scatteredLocation.move(rules.direction(diceResult), 1)
                    if (scatteredLocation.isOutOfBounds(rules)) {
                        outOfBoundsAt = startLocation
                        break
                    }
                }
                @Suppress("DATA_CLASS_INVISIBLE_COPY_USAGE_WARNING")
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.SCATTER, dice),
                    UpdateContext(
                        state.getContext<ScatterRollContext>().copy(
                            scatterRoll = dice,
                            landsAt = scatteredLocation,
                            outOfBoundsAt = outOfBoundsAt,
                        )
                    ),
                    ExitProcedure()
                )
            }
        }
    }
}
