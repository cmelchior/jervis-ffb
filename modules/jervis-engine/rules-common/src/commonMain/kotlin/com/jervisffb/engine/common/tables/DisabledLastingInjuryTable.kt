package com.jervisffb.engine.common.tables

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.rules.common.tables.LastingInjuryResult
import com.jervisffb.engine.rules.common.tables.LastingInjuryTable
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import kotlinx.serialization.Serializable

// Placeholder for rulesets that do not have a Casualty Table (BB7)
@Serializable
object DisabledLastingInjuryTable: LastingInjuryTable {
    override val entries: Map<Int, LastingInjuryResult> = emptyMap()
    override fun roll(d6: D6Result): LastingInjuryResult = INVALID_GAME_STATE("This ruleset does not have a Lasting Injury Table configured")
}
