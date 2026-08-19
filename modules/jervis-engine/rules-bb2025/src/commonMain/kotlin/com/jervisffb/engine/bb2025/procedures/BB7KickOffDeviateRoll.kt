package com.jervisffb.engine.bb2025.procedures

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.probabiliy.AddChanceObservation
import com.jervisffb.engine.common.context.DeviateRollContext
import com.jervisffb.engine.common.procedures.dicerolls.createFinalTableLookupObservation
import com.jervisffb.engine.common.reports.ReportDiceRoll
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.statistics.probability.observation.ChanceObservationHandler
import com.jervisffb.engine.utils.INVALID_ACTION
import kotlin.math.min

/**
 * When rolling for Deviate during Kick-off in BB7, you roll 1D8 + 2D6 and
 * choose the lower D6.
 *
 * See page 10 in Spike 22.
 */
object BB7KickOffDeviateRoll : Procedure(), ChanceObservationHandler {
    override val initialNode: Node = RollDice
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<DeviateRollContext>()
    }

    object RollDice : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team? = null
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D8, Dice.D6, Dice.D6, type = DiceRollType.DEVIATE))
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            if (action !is DiceRollResults || action.rolls.size != 3) INVALID_ACTION(action)
            val d8 = action.rolls.first() as? D8Result ?: INVALID_ACTION(action, "First roll must be D8")
            val firstD6 = action.rolls[1] as? D6Result ?: INVALID_ACTION(action, "Second roll must be D6")
            val secondD6 = action.rolls[2] as? D6Result ?: INVALID_ACTION(action, "Third roll must be D6")

            val context = state.getContext<DeviateRollContext>()
            val direction = rules.direction(d8)
            val distance = min(firstD6.value, secondD6.value)

            // Is this how this chance is calculated? :thinking:
            val chanceObservation = createFinalTableLookupObservation(
                state = state,
                team = state.homeTeam,
                rollType = DiceRollType.DEVIATE,
                dice = listOf(d8, firstD6, secondD6),
                favorableOutcomes = if (firstD6 == secondD6) 1 else 2,
                possibleOutcomes = D8Result.SIDES * D6Result.SIDES * D6Result.SIDES,
            )

            // Move the ball one at a time and check for out of bounds at every move
            var currentLocation = context.from
            var outOfBoundsAt: PitchCoordinate? = null
            for (i in 1..distance) {
                val start = currentLocation
                currentLocation = currentLocation.move(direction, 1)
                if (currentLocation.isOutOfBounds(rules)) {
                    outOfBoundsAt = start
                    break
                }
            }

            return compositeCommandOf(
                ReportDiceRoll(DiceRollType.DEVIATE, listOf(d8, firstD6, secondD6, secondD6), showDiceType = true),
                chanceObservation?.let(::AddChanceObservation),
                UpdateContext(context.copy(
                    deviateRoll = listOf(d8, firstD6, secondD6),
                    landsAt = currentLocation,
                    outOfBoundsAt = outOfBoundsAt,
                )),
                ExitProcedure()
            )
        }
    }
}
