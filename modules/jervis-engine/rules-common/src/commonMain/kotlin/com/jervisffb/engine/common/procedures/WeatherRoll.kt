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
import com.jervisffb.engine.common.procedures.dicerolls.createFinalTableLookupObservation
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
import com.jervisffb.engine.statistics.probability.observation.ChanceObservation
import com.jervisffb.engine.statistics.probability.observation.ChanceObservationHandler

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
                val chanceObservation = createChanceObservation(state, weather, listOf(firstD6, secondD6))
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
        weatherResult: Weather,
        roll: List<D6Result>,
    ): ChanceObservation.DiceRoll? {
        if (!state.collectChanceData) return null
        val possibleOutcomes = state.rules.weatherTable.entries.size
        val favorableOutcomes = state.rules.weatherTable.entries.values.count { it == weatherResult }
        return createFinalTableLookupObservation(
            state = state,
            team = state.homeTeam,
            rollType = DiceRollType.WEATHER,
            dice = roll,
            favorableOutcomes = favorableOutcomes,
            possibleOutcomes = possibleOutcomes,
        )
    }
}
