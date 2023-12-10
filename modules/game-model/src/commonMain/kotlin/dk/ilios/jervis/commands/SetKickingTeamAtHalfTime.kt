package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Team

class SetKickingTeamAtHalfTime(private val kickingTeam: Team) : Command {
    private lateinit var originalKickingTeam: Team
    override fun execute(state: Game, controller: GameController) {
        this.originalKickingTeam = state.kickingTeamInLastHalf
        state.kickingTeamInLastHalf = kickingTeam
        state.kickingTeam = kickingTeam
        state.receivingTeam = kickingTeam.otherTeam()
    }

    override fun undo(state: Game, controller: GameController) {
        state.receivingTeam = originalKickingTeam.otherTeam()
        state.kickingTeam = originalKickingTeam
        state.kickingTeamInLastHalf = originalKickingTeam
    }
}
