package com.jervisffb.engine.rules.common.tables

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
    // What "duration" mean is defined by each sub-class of this interface.
    // It is mostly here to make it possible to remove table results again in
    // an uniform way. Table results that trigger an effect, but otherwise do
    // nothing, should be given a duration of IMMEDIATE.
    val duration: Duration
}
