package com.jervisffb.engine.common.commands

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.common.tables.PrayerToNuffleEvent

class AddPrayersToNuffle(private val team: Team, val prayer: PrayerToNuffleEvent) : Command {
    override fun execute(state: Game) {
        team.activePrayersToNuffle.add(prayer)
    }

    override fun undo(state: Game) {
        team.activePrayersToNuffle.remove(prayer)
    }
}
