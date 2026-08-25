package com.jervisffb.engine.common.inducements

import com.jervisffb.engine.model.inducements.settings.InducementType
import kotlinx.serialization.Serializable

/**
 * Inducement types shared between BB2020 and BB2025.
 *
 * See page 89 in the BB2020 rulebook.
 * See page 142 in the BB2025 rulebook.
 */
@Serializable
enum class InducementTypeCommon: InducementType {
    BIASED_REFEREE,
    BRIBE,
    DESPERATE_MEASURES, // Used in BB7
    EXTRA_TEAM_TRAINING,
    HALFLING_MASTER_CHEF,
    INFAMOUS_COACHING_STAFF,
    MORTUARY_ASSISTANT,
    PART_TIME_ASSISTANT_COACH,
    PLAGUE_DOCTOR,
    RIOTOUS_ROOKIE,
    STANDARD_MERCENARY_PLAYERS,
    STAR_PLAYERS,
    TEMP_AGENCY_CHEERLEADER,
    WANDERING_APOTHECARY,
    WEATHER_MAGE,
    WIZARD,
}
