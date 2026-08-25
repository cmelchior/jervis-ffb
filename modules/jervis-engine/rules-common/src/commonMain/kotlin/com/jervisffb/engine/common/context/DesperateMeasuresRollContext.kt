package com.jervisffb.engine.common.context

import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.rules.common.tables.DesperateMeasuresEvent
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf

/**
 * Context data required to track rolling on the Desperate Measures Table.
 */
data class DesperateMeasuresRollContext(
    val team: Team,
    val rollsRemaining: Int,
    val result: DesperateMeasuresEvent? = null,
    val resultApplied: Boolean = false,
    val rolledEvents: PersistentSet<DesperateMeasuresEvent> = persistentSetOf()
) : ProcedureContext
