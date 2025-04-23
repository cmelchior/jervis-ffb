package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.bb2020.tables.PrayerToNuffle

class AddPrayersToNuffle(private val team: Team, val prayer: PrayerToNuffle) : Command {
    override fun execute(state: Game) {
        team.activePrayersToNuffle.add(prayer)
    }

    override fun undo(state: Game) {
        team.activePrayersToNuffle.remove(prayer)
    }
}
