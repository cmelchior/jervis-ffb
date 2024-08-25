package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Team

class SetApothecary(private val team: Team, private val apothecaries: Int) : Command {
    private var originalApothecaries: Int = 0

    override fun execute(
        state: Game,
        controller: GameController,
    ) {
        originalApothecaries = team.apothecaries
        team.apothecaries = apothecaries
        team.notifyUpdate()
    }

    override fun undo(
        state: Game,
        controller: GameController,
    ) {
        team.apothecaries = originalApothecaries
        team.notifyUpdate()
    }
}
