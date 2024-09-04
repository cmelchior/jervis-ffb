package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.rules.tables.PrayerToNuffle

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
