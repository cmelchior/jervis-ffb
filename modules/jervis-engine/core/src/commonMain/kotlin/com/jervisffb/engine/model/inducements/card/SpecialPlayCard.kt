package com.jervisffb.engine.model.inducements.card

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.inducements.InducementEffect
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.skills.Duration

/**
 * Interface describing a "Special Play Card". We use this term for all effects
 * that can be played during a game, but are special, independent, one-time
 * effects.
 *
 * Examples: Special Play Cards or Desperate Measures
 */
interface SpecialPlayCard: InducementEffect {
    // Type of Special Play Card.
    val type: SpecialPlayCardCategory
    // When the card stops being "in play".
    // Some cards also add temporary skills, dice modifiers, and other powers
    // these are removed independently. This only tracks "the card"
    val duration: Duration
    // If the card is "played" or in use.
    var isActive: Boolean
    // Some cards have special conditions on their triggers
    // Call this method to ensure that the card truly is available.
    // It assumes that the trigger Timing condition has been met
    fun isApplicable(state: Game, rules: Rules): Boolean {
        return true
    }
}
