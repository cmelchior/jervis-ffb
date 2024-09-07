package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.rules.tables.Weather

class SetWeather(private val weather: Weather) : Command {
    private lateinit var originalWeather: Weather

    override fun execute(state: Game, controller: GameController) {
        originalWeather = state.weather
        state.weather = weather
        state.notifyUpdate()
    }

    override fun undo(state: Game, controller: GameController) {
        state.weather = originalWeather
        state.notifyUpdate()
    }
}
