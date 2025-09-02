package com.jervisffb.engine.rules.common.tables

import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.DieResult

/**
 * Interface representing rolling on the Prayers To Nuffle Table.
 *
 * @param D which type of die to roll
 */
interface PrayersToNuffleTable {
    val die: Dice // Which die is used to roll the table
    /**
     * Roll on the table. Should throw an [com.jervisffb.engine.utils.INVALID_ACTION]
     * exception if the die does not match [die].
     */
    fun roll(die: DieResult): PrayerToNuffle
}
