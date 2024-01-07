package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Team

class SetFanFactor(private val team: Team, private val fanFactor: Int) : Command {

    private var originalValue: Int = 0

    override fun execute(state: Game, controller: GameController) {
        originalValue = team.fanFactor
        team.fanFactor = fanFactor
    }

    override fun undo(state: Game, controller: GameController) {
        team.fanFactor = originalValue
    }
}
