package com.jervisffb.engine.commands

import com.jervisffb.engine.controller.GameController
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.bb2020.tables.Weather

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
