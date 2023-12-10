package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Team

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
