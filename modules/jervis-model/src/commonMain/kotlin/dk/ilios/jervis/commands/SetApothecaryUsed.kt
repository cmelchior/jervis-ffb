package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.inducements.Apothecary

class SetApothecaryUsed(
    private val team: Team,
    private val apothecary: Apothecary,
    private val used: Boolean
) : Command {
    private var originalValue: Boolean = false

    override fun execute(state: Game, controller: GameController) {
        originalValue = apothecary.used
        apothecary.used = used
        team.notifyUpdate()
   }

    override fun undo(state: Game, controller: GameController) {
        apothecary.used = originalValue
        team.notifyUpdate()
    }
}
