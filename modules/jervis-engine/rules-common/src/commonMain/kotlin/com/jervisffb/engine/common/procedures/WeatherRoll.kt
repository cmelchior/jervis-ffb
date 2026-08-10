package com.jervisffb.engine.common.procedures

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.probabiliy.AddChanceObservation
import com.jervisffb.engine.common.commands.SetWeather
import com.jervisffb.engine.common.reports.ReportDiceRoll
import com.jervisffb.engine.common.reports.ReportWeatherResult
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.castDiceRoll
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.tables.Weather
import com.jervisffb.engine.statistics.probability.event.ChanceOutcomeCategory
import com.jervisffb.engine.statistics.probability.event.OutcomeRatio
import com.jervisffb.engine.statistics.probability.observation.ChanceDieResult
import com.jervisffb.engine.statistics.probability.observation.ChanceObservation
import com.jervisffb.engine.statistics.probability.observation.ChanceObservationHandler
import com.jervisffb.engine.statistics.probability.observation.ChanceObservationScope
import com.jervisffb.engine.statistics.probability.observation.ChanceOutcome
import com.jervisffb.engine.statistics.probability.observation.ChanceResultId

/**
 * This procedure controls rolling for the weather.
 *
 * See page 37 in the BB2020 rulebook.
 * See page 46 in the BB2025 rulebook.
 */
object WeatherRoll : Procedure(), ChanceObservationHandler {
    override val initialNode: Node = RollWeatherDice
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object RollWeatherDice : ActionNode() {
        // Technically, both coaches should roll a die, but for now, we just let the home coach do it.
        override fun actionOwner(state: Game, rules: Rules): Team? = state.homeTeam
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            // Each coach should role a dice, but just treat this as a single dice roll here
            return listOf(RollDice(Dice.D6, Dice.D6))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castDiceRoll<D6Result, D6Result>(action) { firstD6, secondD6 ->
                val weather: Weather = rules.weatherTable.roll(firstD6, secondD6)
                val chanceObservation = createChanceObservation(state, rules, firstD6, secondD6, weather)
                // We just store the weather type and let affected procedures handle the
                // effect of it.
                return compositeCommandOf(
                    SetWeather(weather),
                    ReportDiceRoll(DiceRollType.WEATHER, listOf(firstD6, secondD6)),
                    ReportWeatherResult(weather),
                    chanceObservation?.let(::AddChanceObservation),
                    ExitProcedure(),
                )
            }
        }
    }

    private fun createChanceObservation(
        state: Game,
        rules: Rules,
        firstD6: D6Result,
        secondD6: D6Result,
        weather: Weather,
    ): ChanceObservation.DiceRoll? {
        if (!state.collectChanceData) return null

        val index = state.nextAvailableChanceObservationIndex
        val team = state.homeTeam
        val favorableOutcomes = (1..6).sumOf { first ->
            (1..6).count { second ->
                rules.weatherTable.entries[first + second] == weather
            }
        }
        return ChanceObservation.DiceRoll(
            index = index,
            rollType = DiceRollType.WEATHER,
            teamId = team.id,
            dice = listOf(firstD6, secondD6).mapIndexed { resultIndex, result ->
                ChanceDieResult(
                    id = ChanceResultId(index, resultIndex),
                    result = result,
                )
            },
            scope = ChanceObservationScope.fromState(state, team),
            success = true,
            outcome = ChanceOutcome(
                category = ChanceOutcomeCategory.TARGET_SET,
                successProbability = OutcomeRatio(favorableOutcomes, 36),
            ),
            finalized = true,
        )
    }
}
