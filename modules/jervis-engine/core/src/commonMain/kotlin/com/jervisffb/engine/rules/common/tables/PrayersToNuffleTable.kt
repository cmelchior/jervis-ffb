package com.jervisffb.engine.rules.common.tables

import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.RulesParametersHolder

/**
 * Interface representing rolling on the Prayers To Nuffle Table.
 *
 * Developer's Commentary:
 * Due to the way [Rules] and [RulesParametersHolder] are organized, it isn't
 * possible to use a generic [Dice] to represent the die used to roll the table,
 * as it would get type-erased before being used in a [Procedure].
 *
 * For this reason we instead have [die], allowing consumers to check which die
 * to use. If the wrong one is used, a runtime exception will be thrown.
 */
interface PrayersToNuffleTable {
    val die: Dice // Which die is used to roll the table
    val entries: Map<Int, PrayerToNuffleEvent>

    /**
     * Roll on the table. Should throw an [com.jervisffb.engine.utils.INVALID_ACTION]
     * exception if the die does not match [die].
     */
    fun roll(die: DieResult): PrayerToNuffleEvent
}
