package com.jervisffb.engine.rules.common.tables

import com.jervisffb.engine.actions.D16Result
import com.jervisffb.engine.model.modifiers.DiceModifier

/**
 * Interface representing the Casualty Table.
 *
 * See page 60 in the BB2020 rulebook.
 * See page 67 in the BB2025 rulebook.
 */
interface CasualtyTable {
    /**
     * Roll on the Injury table and return the result.
     */
    fun roll(d16: D16Result, modifiers: List<DiceModifier>): CasualtyResult
}
