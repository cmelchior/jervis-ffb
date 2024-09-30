package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Team

class SetTurnMarker(private val currentTeam: Team, private val nextTurn: Int) : Command {
    private var originalTurn: Int = 0

    override fun execute(state: Game, controller: GameController) {
        originalTurn = currentTeam.turnMarker
        currentTeam.turnMarker = nextTurn
    }

    override fun undo(state: Game, controller: GameController) {
        currentTeam.turnMarker = originalTurn
    }
}
