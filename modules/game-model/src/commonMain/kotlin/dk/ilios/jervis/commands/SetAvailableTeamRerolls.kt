package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.rules.skills.RegularTeamReroll
import dk.ilios.jervis.rules.skills.TeamReroll

class SetAvailableTeamRerolls(private val team: Team) : Command {
    private var originalRerolls = mutableListOf<TeamReroll>()

    override fun execute(
        state: Game,
        controller: GameController,
    ) {
        originalRerolls.addAll(team.rerolls)
        team.rerolls.clear()
        repeat(team.rerollsCountOnRoster) {
            team.rerolls.add(RegularTeamReroll(team))
        }
    }

    override fun undo(
        state: Game,
        controller: GameController,
    ) {
        team.rerolls.clear()
        team.rerolls.addAll(originalRerolls)
    }
}
