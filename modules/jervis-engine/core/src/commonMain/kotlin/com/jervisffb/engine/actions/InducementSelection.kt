package com.jervisffb.engine.actions

import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.inducements.settings.InducementType
import com.jervisffb.engine.model.inducements.settings.SingleInducement
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * This interface is used to capture information about each bought inducement.
 * It is up to the Rules Engine to map these into the concrete inducements in the
 * model layer.
 *
 * This is done in [com.jervisffb.engine.rules.common.procedures.ApplyInducements].
 */
interface InducementSelection<T: SingleInducement<*>> {
    val type: InducementType
    /**
     * How many of this inducement was bought. This is what the price is
     * multiplied by, and what is counted against the inducement's
     * [com.jervisffb.engine.model.inducements.settings.Inducement.max].
     */
    val count: Int

    // Returns the settings for this inducement, or `null` if the selection
    // doesn't describe an inducement that is available in this ruleset.
    fun getSettingsOrNull(rules: Rules): T?
    fun getSettings(rules: Rules): T = getSettingsOrNull(rules)
        ?: INVALID_GAME_STATE("Not a valid inducement in this ruleset: $this")
    // Returns the full price that must be paid for this inducement by the current team.
    // This takes into account any discounts that may be available to the team.
    fun getPrice(team: Team): Int = getSettings(team.game.rules).getPrice(team) * count
    // Returns `false` if this inducement is not available to the given team.
    // This method is a shortcut for looking up the same information in the Rules for the inducement.
    fun isAvailableToTeam(team: Team): Boolean {
        val settings = getSettings(team.game.rules).requirements
        return settings.isEmpty() || team.allSpecialRules.any { it in settings }
    }
}
