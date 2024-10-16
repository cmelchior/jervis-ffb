package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game

class SetDrive(private val nextDrive: Int) : Command {
    private var originalDrive: Int = 0

    override fun execute(
        state: Game,
    ) {
        originalDrive = state.driveNo
        state.driveNo = nextDrive
    }

    override fun undo(
        state: Game,
    ) {
        state.driveNo = originalDrive
    }
}
