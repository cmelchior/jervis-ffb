package com.jervisffb.engine.model.modifiers

/**
 * Static Modifiers that can affect a Pickup roll.
 *
 * @see [com.jervisffb.rules.bb2020.procedures.Pickup]
 * @see [com.jervisffb.rules.bb2020.procedures.PickupRoll]
 * @see [MarkedModifier]
 */
enum class PickupModifier(override val modifier: Int, override val description: String) : DiceModifier {
    MARKED(-1, "Marked"),
    POURING_RAIN(-1, "Pouring Rain"),
}


