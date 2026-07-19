package com.jervisffb.engine.reports

import com.jervisffb.engine.actions.GameAction

/**
 * Marker log entry that allows us to pinpoint when a new [GameAction] is being handled.
 */
class ReportHandleAction(val action: GameAction) : SimpleLogEntry(
    message = "Selected action: $action",
    category = LogCategory.STATE_MACHINE
)
