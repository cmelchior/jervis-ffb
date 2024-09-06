package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game

class SetCanUseTeamRerolls(private val canUseRerolls: Boolean) : Command {
    private var originalValue: Boolean = false

    override fun execute(state: Game, controller: GameController) {
        originalValue = state.canUseTeamRerolls
        state.canUseTeamRerolls = canUseRerolls
    }

    override fun undo(state: Game, controller: GameController) {
        state.canUseTeamRerolls = originalValue
    }
}
