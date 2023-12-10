package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Team

class SetKickingTeam(private val kickingTeam: Team) : Command {
    private lateinit var originalKickingTeam: Team
    private lateinit var originalReceivingTeam: Team
    override fun execute(state: Game, controller: GameController) {
        originalKickingTeam = state.kickingTeam
        originalReceivingTeam = state.receivingTeam
        state.kickingTeam = kickingTeam
        state.receivingTeam = kickingTeam.otherTeam()
    }
    override fun undo(state: Game, controller: GameController) {
        state.kickingTeam = originalKickingTeam
        state.receivingTeam = originalReceivingTeam
    }
}
