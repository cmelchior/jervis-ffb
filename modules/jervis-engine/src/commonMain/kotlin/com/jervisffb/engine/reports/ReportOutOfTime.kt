package com.jervisffb.engine.reports

import com.jervisffb.engine.OutOfTimeBehaviour
import com.jervisffb.engine.model.Team

class ReportOutOfTime(activeTeam: Team, otherTeam: Team, outOfTimeBehaviour: OutOfTimeBehaviour) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        when (outOfTimeBehaviour) {
            OutOfTimeBehaviour.NONE -> error("Unexpected out-of-time behaviour: $outOfTimeBehaviour")
            OutOfTimeBehaviour.SHOW_WARNING -> {
                append("${activeTeam.name} ran out of time.")
            }
            OutOfTimeBehaviour.OPPONENT_CALL_TIMEOUT -> {
                append("${activeTeam.name} ran out of time. ${otherTeam.name} can call Out-of-Time")
            }
            OutOfTimeBehaviour.AUTOMATIC_TIMEOUT -> {
                append("${activeTeam.name} ran out of time. The system ")
            }
        }
    }
}
