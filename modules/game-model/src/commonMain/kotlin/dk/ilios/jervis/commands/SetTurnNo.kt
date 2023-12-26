package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Team

class SetTurnNo(private val currentTeam: Team, private val nextTurn: UInt) : Command {

    private var originalTurn: UInt = 0u

    override fun execute(state: Game, controller: GameController) {
        originalTurn = currentTeam.turnData.currentTurn
        currentTeam.turnData.currentTurn = nextTurn
    }

    override fun undo(state: Game, controller: GameController) {
        currentTeam.turnData.currentTurn = originalTurn
    }
}
