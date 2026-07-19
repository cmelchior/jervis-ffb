package com.jervisffb.engine.model.modifiers

/**
 * Generic interface for something that modifies the value of a dice roll.
 */
interface DiceModifier {
    val modifier: Int
    val description: String
}
