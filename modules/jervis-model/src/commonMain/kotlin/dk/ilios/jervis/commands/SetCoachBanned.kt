package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Team

class SetCoachBanned(private val team: Team, private val banned: Boolean) : Command {
    private var originalValue: Boolean = false

    override fun execute(
        state: Game,
        controller: GameController,
    ) {
        originalValue = team.coachBanned
        team.coachBanned = banned
        team.notifyUpdate()
    }

    override fun undo(
        state: Game,
        controller: GameController,
    ) {
        team.coachBanned = originalValue
        team.notifyUpdate()
    }
}
