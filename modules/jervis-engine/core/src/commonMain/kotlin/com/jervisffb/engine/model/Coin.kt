package com.jervisffb.engine.model

import com.jervisffb.engine.actions.D2Result
import com.jervisffb.engine.ext.d2

/**
 * Represents a coin with two sides.
 * It also includes mapping to a [D2Result], so coin tosses can be treated
 * by a die roll in the probability system.
 */
enum class Coin(val description: String, val d2: D2Result) {
    HEAD("Heads", 1.d2),
    TAIL("Tails", 2.d2),
}
