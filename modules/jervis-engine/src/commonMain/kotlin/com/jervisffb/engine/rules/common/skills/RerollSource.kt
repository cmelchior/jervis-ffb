package com.jervisffb.engine.rules.common.skills

import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.RerollSourceId
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.common.procedures.DieRoll

// Should we split this into a "normal dice" and "block dice" interface?
interface RerollSource {
    val id: RerollSourceId // Unique identifier for this reroll. Should be unique across both teams.
    val rerollResetAt: Duration // Not currently used (except for documentation purposes). Can it be removed?
    val rerollDescription: String
    var rerollUsed: Boolean
    val rerollProcedure: Procedure

    // Returns `true` if `calculateRerollOptions` will return a non-empty list
    fun canReroll(
        state: Game,
        type: DiceRollType,
        value: List<DieRoll<*>>,
        wasSuccess: Boolean? = null
    ): Boolean

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
    fun calculateRerollOptions(type: DiceRollType, value: DieRoll<*>, wasSuccess: Boolean?): List<DiceRerollOption> =
        calculateRerollOptions(
            type,
            listOf(value),
            wasSuccess,
        )
}
