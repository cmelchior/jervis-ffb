package com.jervisffb.engine.bb2025

import com.jervisffb.engine.bb2025.inducements.InducementType2025
import com.jervisffb.engine.bb2025.inducements.biasedreferee.DodgyLeagueRep
import com.jervisffb.engine.bb2025.inducements.wizards.SportsWizard
import com.jervisffb.engine.common.inducements.BiasedRefereeInducement
import com.jervisffb.engine.common.inducements.BiasedRefereesInducementGroup
import com.jervisffb.engine.common.inducements.InducementTypeCommon
import com.jervisffb.engine.common.inducements.InfamousCoachingStaffsInducementGroup
import com.jervisffb.engine.common.inducements.SimpleInducement
import com.jervisffb.engine.common.inducements.StandardMercenaryInducement
import com.jervisffb.engine.common.inducements.StarPlayersInducementGroup
import com.jervisffb.engine.common.inducements.WizardInducement
import com.jervisffb.engine.common.inducements.WizardsInducementGroup
import com.jervisffb.engine.model.inducements.settings.Inducement
import com.jervisffb.engine.model.inducements.settings.InducementType
import com.jervisffb.engine.rules.common.roster.TeamSpecialRule

/**
 * This map contains the setup for all default inducements available in the
 * BB2025 rulebook.
 *
 * See page 142 in the BB2025 rulebook.
 */
val DEFAULT_INDUCEMENTS_BB2025: Map<InducementType, Inducement<*>> = buildMap {
    InducementTypeCommon.entries.forEach { type ->
        val inducement: Inducement<*> = when (type) {
            InducementTypeCommon.BIASED_REFEREE -> BiasedRefereesInducementGroup(
                max = 1,
                enabled = true,
                items = listOf(
                    BiasedRefereeInducement(
                        referee = DodgyLeagueRep(),
                        max = 1,
                        defaultPrice = 120_000,
                        named = false,
                        enabled = true,
                        specialRulesModifier = mapOf(Pair(TeamSpecialRule.BRIBERY_AND_CORRUPTION, 2/3f)),
                    ),
                )
            )
            InducementTypeCommon.BRIBE -> SimpleInducement(type, "Bribe", 3, 100_000, true) // 0.5x price and 2x amount for Bribery and Corruption
            InducementTypeCommon.DESPERATE_MEASURES -> SimpleInducement(type, "Desperate Measures", 5, 50_000, false)
            InducementTypeCommon.EXTRA_TEAM_TRAINING -> SimpleInducement(type, "Extra Team Training", 8, 100_000, true)
            InducementTypeCommon.HALFLING_MASTER_CHEF -> SimpleInducement(type, "Hafling Master Chef", 1, 300_000, true, specialRulesModifier = mapOf(), teamNameModifier = listOf("^Hafling$".toRegex().toString() to 1/3f))
            InducementTypeCommon.INFAMOUS_COACHING_STAFF -> InfamousCoachingStaffsInducementGroup(
                max = 1,
                enabled = true,
                items = emptyList()
            )
            InducementTypeCommon.MORTUARY_ASSISTANT -> SimpleInducement(type, "Mortuary Assistant", 1, 100_000, true, requirements = setOf(TeamSpecialRule.MASTERS_OF_UNDEATH))
            InducementTypeCommon.PART_TIME_ASSISTANT_COACH -> SimpleInducement(type, "Part-time Assistant Coaches", 5, 20_000, true)
            InducementTypeCommon.PLAGUE_DOCTOR -> SimpleInducement(type, "Plague Doctor", 1, 100_000, true, requirements = setOf(TeamSpecialRule.FAVOURED_OF_NURGLE))
            InducementTypeCommon.RIOTOUS_ROOKIE -> SimpleInducement(type, "Riotous Rookies", 1, 150_000, true, requirements = setOf(TeamSpecialRule.LOW_COST_LINEMEN))
            InducementTypeCommon.STANDARD_MERCENARY_PLAYERS -> StandardMercenaryInducement(enabled = true)
            InducementTypeCommon.STAR_PLAYERS -> StarPlayersInducementGroup(max = 2, enabled = true)
            InducementTypeCommon.TEMP_AGENCY_CHEERLEADER -> SimpleInducement(type, "Temp Agency Cheerleaders", 5, 5_000, true)
            InducementTypeCommon.WANDERING_APOTHECARY -> SimpleInducement(type, "Wandering Apothecaries", 2, 100_000, true)
            InducementTypeCommon.WEATHER_MAGE -> SimpleInducement(type, "Weather Mage", 1, 25_000, true)
            InducementTypeCommon.WIZARD -> WizardsInducementGroup(
                max = 1,
                enabled = true,
                items = listOf(
                    WizardInducement(
                        wizard = SportsWizard(),
                        max = 1,
                        defaultPrice = 150_000,
                        named = false,
                        enabled = true
                    )
                )
            )

        }
        put(type, inducement)
    }
    InducementType2025.entries.forEach { type ->
        val inducement: Inducement<*> = when (type) {
            InducementType2025.BLITZERS_BEST_KEGS -> SimpleInducement(type, "Blitzer's Best Kegs", 2, 50_000, true)
            InducementType2025.PRAYERS_TO_NUFFLE -> SimpleInducement(type, "Prayers to Nuffle", 3, 10_000, true)
            InducementType2025.TEAM_MASCOT -> SimpleInducement(type, "Team Mascot", 1, 25_000, true)
        }
        put(type, inducement)
    }
}
