package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.inducements.WeatherMage

class AddWeatherMage(private val team: Team) : Command {
    val weatherMage = WeatherMage()
    override fun execute(state: Game) {
        team.weatherMages.add(weatherMage)
    }
    override fun undo(state: Game) {
        team.weatherMages.remove(weatherMage)
    }
}
