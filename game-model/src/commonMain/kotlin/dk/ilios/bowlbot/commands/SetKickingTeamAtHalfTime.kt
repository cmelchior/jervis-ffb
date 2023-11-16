package dk.ilios.bowlbot.commands

import dk.ilios.bowlbot.controller.GameController
import dk.ilios.bowlbot.model.Game
import dk.ilios.bowlbot.model.Team

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
