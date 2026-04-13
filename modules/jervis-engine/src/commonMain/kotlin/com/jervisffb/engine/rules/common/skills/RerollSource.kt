package com.jervisffb.engine.rules.common.skills

import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.RerollSourceId
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.common.procedures.DieRoll
import com.jervisffb.engine.rules.common.rerolls.DiceRerollOption

// Should we split this into a "normal dice" and "block dice" interface?
interface RerollSource {
    /**
     * Unique identifier for this reroll type.
     * If a reroll type provides more than one re-roll option, like allowing
     * to re-rolling different individual dice. The [DiceRerollOption] should
     * have the this (same) ID across all options.
     */
    val id: RerollSourceId
    val rerollResetAt: Duration // Not currently used (except for documentation purposes). Can it be removed?
    val rerollDescription: String
    var rerollUsed: Boolean
    val rerollProcedure: Procedure

    /**
     * Returns `true` if this source is allowed to reroll a given roll.
     * This method should only consider the state of itself, and not if
     * another rule prevents the reroll.
     *
     * See [com.jervisffb.engine.rules.Rules.isRerollAllowed]
     */
    fun canReroll(
        state: Game,
        type: DiceRollType,
        dicePool: List<DieRoll<*>>,
        wasSuccess: Boolean? = null
    ): Boolean

    /**
     * This method should only be called if [canReroll] returns true.
     * If this source can reroll one or more dice in the dice pool. All reroll
     * options should be returned.
     */
    fun calculateRerollOptions(
        // What kind of dice roll
        type: DiceRollType,
        // All dice part of the roll
        value: List<DieRoll<*>>,
        // If the roll was "successful" (as some skills only allow rerolls if unsuccessful). For some roll types
        // this concept doesn't make sense, like Block rolls or rolling for a table result.
        wasSuccess: Boolean? = null,
    ): List<DiceRerollOption>

    // Helper method, for just rolling a single dice. Which is by far, the most common scenario.
    fun calculateRerollOptions(type: DiceRollType, die: DieRoll<*>, wasSuccess: Boolean?): List<DiceRerollOption> =
        calculateRerollOptions(
            type,
            listOf(die),
            wasSuccess,
        )
}
