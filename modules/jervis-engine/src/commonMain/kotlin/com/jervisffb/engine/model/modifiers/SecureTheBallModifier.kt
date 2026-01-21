package com.jervisffb.engine.model.modifiers

import com.jervisffb.engine.rules.bb2025.procedures.actions.securetheball.SecureTheBallAction
import com.jervisffb.engine.rules.bb2025.procedures.actions.securetheball.SecureTheBallRoll

/**
 * Static Modifiers that can affect a Secure The Ball roll.
 *
 * @see [SecureTheBallAction]
 * @see [SecureTheBallRoll]
 * @see [MarkedModifier]
 */
enum class SecureTheBallModifier(override val modifier: Int, override val description: String) : DiceModifier {
    MARKED(-1, "Marked"),
    POURING_RAIN(-1, "Pouring Rain"),
}


