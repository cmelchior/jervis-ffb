package dk.ilios.jervis.commands

import dk.ilios.jervis.actions.DieResult
import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Team

class DeleteTemporaryDieRoll(private val team: Team) : Command {
    private var deleted: DieResult? = null

    override fun execute(
        state: Game,
        controller: GameController,
    ) {
        deleted = team.temporaryData.dieRoll.removeLast()
    }

    override fun undo(
        state: Game,
        controller: GameController,
    ) {
        team.temporaryData.dieRoll.add(deleted!!)
    }
}
