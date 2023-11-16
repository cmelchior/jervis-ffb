package dk.ilios.bowlbot.commands

import dk.ilios.bowlbot.controller.GameController
import dk.ilios.bowlbot.model.Game
import dk.ilios.bowlbot.model.Team

class SetTurnNo(private val currentTeam: Team, private val nextTurn: Int) : Command {

    private var originalTurn: Int = 0

    override fun execute(state: Game, controller: GameController) {
        originalTurn = currentTeam.turnData.currentTurn
        currentTeam.turnData.currentTurn = nextTurn
    }

    override fun undo(state: Game, controller: GameController) {
        currentTeam.turnData.currentTurn = originalTurn
    }
}
