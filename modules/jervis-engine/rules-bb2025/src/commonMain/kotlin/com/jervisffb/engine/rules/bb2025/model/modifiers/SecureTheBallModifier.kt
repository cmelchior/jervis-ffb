package com.jervisffb.engine.rules.bb2025.model.modifiers

import com.jervisffb.engine.model.modifiers.DiceModifier

/**
 * Static Modifiers that can affect a Secure The Ball roll. Consumed by
 * `SecureTheBallAction` / `SecureTheBallRoll` in bb2025.
 */
enum class SecureTheBallModifier(override val modifier: Int, override val description: String) : DiceModifier {
    EXTRA_ARMS(1, "Extra Arms"),
    MARKED(-1, "Marked"),
    POURING_RAIN(-1, "Pouring Rain"),
}


