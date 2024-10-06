package com.jervisffb.engine.reports

import com.jervisffb.engine.actions.ActionDescriptor

/**
 * Marker log entry for tracking available actions.
 */
class ReportAvailableActions(val actions: List<ActionDescriptor>) : SimpleLogEntry(
    message = "Available actions: ${actions.joinToString()}",
    category = LogCategory.STATE_MACHINE
)
