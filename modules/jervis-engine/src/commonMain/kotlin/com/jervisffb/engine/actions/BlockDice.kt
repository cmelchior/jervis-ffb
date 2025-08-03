package com.jervisffb.engine.actions;

import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Representation of a Block die.
 * See page 57 in the rulebook.
 */
enum class BlockDice(val description: String) {
    PLAYER_DOWN("Player Down"),
    BOTH_DOWN("Both Down"),
    PUSH_BACK("Pushback"),
    STUMBLE("Stumble"),
    POW("POW!"),
    ;

    companion object {
        fun fromD6(roll: D6Result): BlockDice {
            return when (roll.value) {
                1 -> PLAYER_DOWN
                2 -> BOTH_DOWN
                3, 4 -> PUSH_BACK
                5 -> STUMBLE
                6 -> POW
                else -> INVALID_GAME_STATE("Illegal roll: $roll")
            }
        }
    }
}
