package dk.ilios.jervis.rules.tables

import dk.ilios.jervis.fsm.Procedure

/**
 * Wrapper around a table result, e.g. rolling on the Kick-Off Table or
 * the Prayers To Nuffle Table.
 *
 * Rolling on these tables all involve more complicated logic that is
 * controlled by procedures. So any node that looks up a TableResult should
 * put the returned procedure on the stack to be executed as the next step.
 */
data class TableResult(val name: String, val procedure: Procedure)
