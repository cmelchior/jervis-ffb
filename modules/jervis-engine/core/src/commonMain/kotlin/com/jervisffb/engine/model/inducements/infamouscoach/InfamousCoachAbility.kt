package com.jervisffb.engine.model.inducements.infamouscoach

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.inducements.InducementEffect
import com.jervisffb.engine.rules.Rules

/**
 * Interface describing infamous coach abilities that are optional
 * to use
 */
interface InfamousCoachAbility: InducementEffect {
    // Some cards have special conditions on their triggers
    // Call this method to ensure that the card truly is available.
    // It assumes that the trigger Timing condition has been met
    fun isApplicable(state: Game, rules: Rules): Boolean {
        return true
    }
}
