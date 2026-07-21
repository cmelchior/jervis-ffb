package com.jervisffb.engine.bb2025.commands

import com.jervisffb.engine.bb2025.skills.SecretWeapon
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.model.Game

class SetWasOnPitchDuringDrive(private val skill: SecretWeapon, private val onPitch: Boolean): Command {
    private var originalValue: Boolean = false
    override fun execute(state: Game) {
        originalValue = skill.onPitchDuringDrive
        skill.onPitchDuringDrive = onPitch
    }
    override fun undo(state: Game) {
        skill.onPitchDuringDrive = originalValue
    }
}
