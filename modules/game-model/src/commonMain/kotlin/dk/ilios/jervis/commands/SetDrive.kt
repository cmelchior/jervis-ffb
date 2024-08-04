package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game

class SetDrive(private val nextDrive: Int) : Command {
    private var originalDrive: Int = 0

    override fun execute(
        state: Game,
        controller: GameController,
    ) {
        originalDrive = state.driveNo
        state.driveNo = nextDrive
    }

    override fun undo(
        state: Game,
        controller: GameController,
    ) {
        state.driveNo = originalDrive
    }
}
