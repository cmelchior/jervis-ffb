package com.jervisffb.engine.commands

import com.jervisffb.engine.controller.GameController
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.bb2020.tables.PrayerToNuffle

class RemovePrayersToNuffle(private val team: Team, val prayer: PrayerToNuffle) : Command {
    override fun execute(state: Game, controller: GameController) {
        team.activePrayersToNuffle.remove(prayer)
        team.notifyUpdate()
    }

    override fun undo(state: Game, controller: GameController) {
        team.activePrayersToNuffle.add(prayer)
        team.notifyUpdate()
    }
}
