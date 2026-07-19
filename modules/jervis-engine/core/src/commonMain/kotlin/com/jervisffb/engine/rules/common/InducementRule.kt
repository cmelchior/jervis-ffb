package com.jervisffb.engine.rules.common

import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.inducements.settings.Inducement
import com.jervisffb.engine.model.inducements.settings.InducementType
import com.jervisffb.engine.rules.Rules

/**
 * Enum describing possible broken rules when buying inducements. These
 * are reported by [Rules.isInducementsValid].
 */
sealed interface InducementRule

/** An inducement type was not found for this ruleset */
data class InducementNotFound(val inducement: InducementType): InducementRule

/** Attempting to buy an inducement that is not enabled for this ruleset */
// INVALID_ACTION(this, "${inducement.type} is not enabled for this ruleset")
data class InducementNotEnabled(val inducement: InducementType): InducementRule

/** Too many inducements of a single type was bought */
// INVALID_ACTION(this, "Broke ${inducement.type} limit: ${inducementSettings.max} vs. $updatedCount")
data class InducementLimitExceeded(val inducement: InducementType, val count: Int, val max: Int): InducementRule

/** Inducement is not available to a give team due to it not meeting its requirements */
// INVALID_ACTION(this, "Inducement ${inducement.type} is not available to team ${team.name} as its requirements are not met: ${inducement.getSettings(rules).requirements.joinToString() }")
data class InducementNotAvailableToTeam(val inducement: InducementType, val inducementSettings: Inducement<*>, val team: Team,): InducementRule

/** Used to much gold to buy inducements */
data class TooMuchGoldUsed(val usedGold: Int, val maxLimit: Int): InducementRule
