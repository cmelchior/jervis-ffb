package dk.ilios.jervis.procedures

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.RollDice
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.SetWeather
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.reports.ReportDiceRoll
import dk.ilios.jervis.reports.ReportWeatherResult
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.DiceRollType
import dk.ilios.jervis.rules.tables.Weather

/**
 * This procedure controls rolling for the weather as described on
 * page 37 in the rulebook.
 */
object WeatherRoll : Procedure() {
    override val initialNode: Node = RollWeatherDice
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object RollWeatherDice : ActionNode() {
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
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
