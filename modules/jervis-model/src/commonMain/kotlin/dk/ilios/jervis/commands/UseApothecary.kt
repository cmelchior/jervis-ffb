package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.inducements.Apothecary

class UseApothecary(private val team: Team, private val apothecary: Apothecary) : Command {
    private var originalUsed: Boolean = false

    override fun execute(state: Game, controller: GameController) {
        originalUsed = apothecary.used
        apothecary.used = true
        team.notifyUpdate()
    }

    override fun undo(state: Game,
        controller: GameController,
    ) {
        apothecary.used = false
        team.notifyUpdate()
    }
}
