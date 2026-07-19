package com.jervisffb.engine.reports

import com.jervisffb.engine.model.context.LeapRollContext
import com.jervisffb.engine.model.locations.PitchCoordinate

class ReportLeapResult(roll: LeapRollContext, landIn: PitchCoordinate) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        val name = roll.player.name
        val agilityTarget = roll.player.agility
        if (roll.isSuccess) {
            append("$name leaps high into the air and lands successfully in ${landIn.toLogString()}: [$agilityTarget <= ${roll.modifiedResult}] (${roll.toLogString()})")
        } else {
            append("$name leaps, but fails to land and crashes into the ground in ${landIn.toLogString()}: [$agilityTarget <= ${roll.modifiedResult}] (${roll.toLogString()})")
        }
    }
}
