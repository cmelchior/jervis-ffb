package dk.ilios.bowlbot.commands

import dk.ilios.bowlbot.controller.GameController
import dk.ilios.bowlbot.model.Game
import dk.ilios.bowlbot.model.Team

class SetActiveTeam(private val activeTeam: Team): Command {
    private lateinit var originalTeam: Team
    override fun execute(state: Game, controller: GameController) {
        originalTeam = state.activeTeam
        state.activeTeam = activeTeam
        state.inactiveTeam = activeTeam.otherTeam()
    }
    override fun undo(state: Game, controller: GameController) {
        state.inactiveTeam = originalTeam.otherTeam()
        state.activeTeam = originalTeam
    }
}
