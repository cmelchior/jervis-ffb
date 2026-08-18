package com.jervisffb.engine.common.tables

import com.jervisffb.engine.actions.D16Result
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.rules.common.tables.CasualtyResult
import com.jervisffb.engine.rules.common.tables.CasualtyTable
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import kotlinx.serialization.Serializable

// Placeholder for rulesets that do not have a Casualty Table (BB7)
@Serializable
object DisabledCasualtyTable: CasualtyTable {
    override fun roll(d16: D16Result, modifiers: List<DiceModifier>): CasualtyResult = INVALID_GAME_STATE("This ruleset does not have a Casualty Table configured")
}
