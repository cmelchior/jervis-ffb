package com.jervisffb.engine.bb2020.tables

import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.rules.common.tables.DesperateMeasuresEvent
import com.jervisffb.engine.rules.common.tables.DesperateMeasuresTable
import kotlinx.serialization.Serializable

/**
 * Class representing the BB7 Prayers To Nuffle Table on page 93 in the Death Zone rulebook.
 */
@Serializable
object BB7DesperateMeasuresTable: DesperateMeasuresTable {
    override val entries: Map<Int, DesperateMeasuresEvent> = emptyMap()
    override fun roll(die: D8Result): DesperateMeasuresEvent = TODO("Not implemented")
}
