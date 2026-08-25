package com.jervisffb.engine.bb2020.inducements

import com.jervisffb.engine.common.inducements.InducementTypeCommon
import com.jervisffb.engine.model.inducements.settings.InducementType
import kotlinx.serialization.Serializable

/**
 * Inducement types specific to BB2020.
 *
 * See page 89 in the BB2020 rulebook.
 * See [InducementTypeCommon] for the list of inducements shared between BB2020
 * and BB2025.
 */
@Serializable
enum class InducementType2020: InducementType {

    // Common Inducements for BB2020 that are not in BB2025.
    BLOODWEISER_KEG,
    SPECIAL_PLAY,

    // BB2020 DeathZone
    // ...
    WAAAGH_DRUMMER,
    CAVORTING_NURGLINGS,
    DWARFEN_RUNESMITH,
    HALFLING_HOTPOT,
    MASTER_OF_BALLISTICS,
    EXPANDED_MERCENARY_PLAYERS, // Contains a lot of sub options
    GIANT,
    DESPERATE_MEASURES, // Only available for BB7

    // Spike 19
    BRETONNIAN_PASTRIES,
    BRETONNIAN_DAMSEL,

    // Spike 20
    CANOPIC_JAR,
}
