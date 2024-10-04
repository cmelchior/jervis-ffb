package dk.ilios.jervis.reports

import dk.ilios.jervis.actions.ActionDescriptor

/**
 * Marker log entry for tracking available actions.
 */
class ReportAvailableActions(val actions: List<ActionDescriptor>) : SimpleLogEntry(
    message = "Available actions: ${actions.joinToString()}",
    category = LogCategory.STATE_MACHINE
)
