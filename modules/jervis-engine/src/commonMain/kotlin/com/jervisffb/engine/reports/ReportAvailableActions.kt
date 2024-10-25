package com.jervisffb.engine.reports

import com.jervisffb.engine.ActionsRequest

/**
 * Marker log entry for tracking available actions.
 */
class ReportAvailableActions(val actions: ActionsRequest) : SimpleLogEntry(
    message = "Available actions (${actions.team?.name}): ${actions.actions.joinToString()}",
    category = LogCategory.STATE_MACHINE
)
