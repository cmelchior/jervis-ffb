package com.jervisffb.engine.rules.bb2020.procedures

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetWeather
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.checkDiceRoll
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.reports.ReportWeatherResult
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.tables.Weather

/**
 * This procedure controls rolling for the weather as described on
 * page 37 in the rulebook.
 */
object WeatherRoll : Procedure() {
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
            return checkDiceRoll<D6Result, D6Result>(action) { firstD6, secondD6 ->
                val weather: Weather = rules.weatherTable.roll(firstD6, secondD6)
                // We just store the weather type and let affected procedures handle the
                // effect of it.
                return compositeCommandOf(
                    SetWeather(weather),
                    ReportDiceRoll(DiceRollType.WEATHER, listOf(firstD6, secondD6)),
                    ReportWeatherResult(weather),
                    ExitProcedure(),
                )
            }
        }
    }
}
