package com.jervisffb.engine.rules.bb2020.tables

import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.rules.common.skills.Duration

/**
 * Wrapper around a table result, e.g. rolling on the Kick-Off Table or
 * the Prayers To Nuffle Table.
 *
 * Rolling on these tables all involves more complicated logic that is
 * controlled by procedures. So any node that looks up a TableResult should
 * put the returned procedure on the stack to be executed as the next step.
 */
interface TableResult {
    val description: String
    val procedure: Procedure
    val duration: Duration
}
