package dk.ilios.jervis.commands

import dk.ilios.jervis.actions.DieResult
import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.utils.INVALID_GAME_STATE

class SaveTemporaryDieRoll(private val team: Team, private val dieRoll: DieResult) : Command {
    override fun execute(
        state: Game,
        controller: GameController,
    ) {
        team.temporaryData.dieRoll.add(dieRoll)
    }

    override fun undo(
        state: Game,
        controller: GameController,
    ) {
        val removed: DieResult = team.temporaryData.dieRoll.removeLast()
        if (removed != dieRoll) {
            INVALID_GAME_STATE("Removing unexpected die roll. Removed $removed. Expected $dieRoll.")
        }
    }
}
