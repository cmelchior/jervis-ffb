package com.jervisffb.engine.actions

import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.inducements.settings.InducementType
import com.jervisffb.engine.model.inducements.settings.SingleInducement
import com.jervisffb.engine.rules.Rules

/**
 * This interface is used to capture information about each bought inducement.
 * It is up to the Rules Engine to map these into the concrete inducements in the
 * model layer.
 *
 * This is done in [com.jervisffb.engine.rules.common.procedures.ApplyInducements].
 */
interface InducementSelection<T: SingleInducement<*>> {

    val type: InducementType
    val count: Int

    fun getSettings(rules: Rules): T
    // Returns the full price that must be paid for this inducement by the current team.
    // This takes into account any discounts that may be available to the team.
    fun getPrice(team: Team): Int = getSettings(team.game.rules).getPrice(team) * count
    // Returns `false` if this inducement is not available to the given team.
    // This method is a shortcut for looking up the same information in the Rules for the inducement.
    fun isAvailableToTeam(team: Team): Boolean {
        val settings = getSettings(team.game.rules).requirements
        return settings.isEmpty() || team.specialRules.any { it in settings }
    }
}
