package com.jervisffb.engine.reports

import com.jervisffb.engine.model.context.JumpRollContext
import com.jervisffb.engine.model.locations.FieldCoordinate

class ReportJumpResult(roll: JumpRollContext, landIn: FieldCoordinate) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        val name = roll.player.name
        val agilityTarget = roll.player.agility
        if (roll.isSuccess) {
            append("$name jumps high into the air and lands successfully in ${landIn.toLogString()}: [$agilityTarget <= ${roll.result}] (${roll.toLogString()})")
        } else {
            append("$name jumps, but fails to land and crashes into the ground in ${landIn.toLogString()}: [$agilityTarget <= ${roll.result}] (${roll.toLogString()})")
        }
    }
}
