package com.jervisffb.engine.bb2025.inducements

import com.jervisffb.engine.common.inducements.InducementTypeCommon
import com.jervisffb.engine.model.inducements.settings.InducementType
import kotlinx.serialization.Serializable

/**
 * Inducement types specific to BB2025.
 *
 * See page 142 in the BB2025 rulebook.
 * See [InducementTypeCommon] for the list of inducements shared between BB2020
 * and BB2025.
 */
@Serializable
enum class InducementType2025: InducementType {
    BLITZERS_BEST_KEGS,
    PRAYERS_TO_NUFFLE,
    TEAM_MASCOT,
}
