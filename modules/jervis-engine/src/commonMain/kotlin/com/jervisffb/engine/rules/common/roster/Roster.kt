package com.jervisffb.engine.rules.common.roster

import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.model.RosterId
import com.jervisffb.engine.reports.ReportStartingExtraTime.id
import com.jervisffb.engine.serialize.RosterLogo
import kotlinx.serialization.Serializable

/**
 * class describing a teams roster.
 * *
 * Developer's Commentary:
 * For now, this class is a superset of the BB2020 and BB2025 rulesets,
 * which means that some properties might not make sense for a given ruleset.
 * E.g., leagues are only used in BB2025.
 *
 * It is unclear if this strategy is the best, but for now it seems to work.
 * [Position] is using a similar strategy.
 */
@Serializable
data class Roster(
    val id: RosterId,
    val name: String,
    val tier: Int,
    val numberOfRerolls: Int,
    val rerollCost: Int,
    val allowApothecary: Boolean,
    val positions: List<RosterPosition>,
    // Only used in BB2025
    val leagues: List<RegionalSpecialRule>,
    // In BB2020, "leagues" are called "Regional Special Rules" and are on the
    // same level as team special rules.
    val specialRules: List<SpecialRules>,
    val logo: RosterLogo,
) {
    operator fun get(id: PositionId): Position = positions.first { it.id == id }
}
