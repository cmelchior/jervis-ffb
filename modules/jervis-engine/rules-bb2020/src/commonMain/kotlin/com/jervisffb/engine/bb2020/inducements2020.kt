package com.jervisffb.engine.bb2020

import com.jervisffb.engine.bb2020.inducements.ExpandedMercenaryInducements
import com.jervisffb.engine.bb2020.inducements.InducementType2020
import com.jervisffb.engine.bb2020.inducements.infamouscoach.InfamousCoachingStaffType2020
import com.jervisffb.engine.common.inducements.BiasedRefereesInducementGroup
import com.jervisffb.engine.common.inducements.InducementTypeCommon
import com.jervisffb.engine.common.inducements.InfamousCoachingStaffInducement
import com.jervisffb.engine.common.inducements.InfamousCoachingStaffsInducementGroup
import com.jervisffb.engine.common.inducements.SimpleInducement
import com.jervisffb.engine.common.inducements.StandardMercenaryInducement
import com.jervisffb.engine.common.inducements.StarPlayersInducementGroup
import com.jervisffb.engine.common.inducements.WizardsInducementGroup
import com.jervisffb.engine.model.inducements.settings.Inducement
import com.jervisffb.engine.model.inducements.settings.InducementType
import com.jervisffb.engine.rules.common.roster.RegionalSpecialRule
import com.jervisffb.engine.rules.common.roster.TeamSpecialRule
import kotlin.collections.listOf

val DEFAULT_INDUCEMENTS_BB2020: Map<InducementType, Inducement<*>> = buildMap {
    InducementTypeCommon.entries.forEach { type ->
        val inducement: Inducement<*> = when (type) {
            InducementTypeCommon.BIASED_REFEREE -> BiasedRefereesInducementGroup(max = 1, enabled = true)
            InducementTypeCommon.BRIBE -> SimpleInducement(type, "Bribe", 3, 100_000, true) // Half price for Bribery and Corruption
            InducementTypeCommon.DESPERATE_MEASURES -> SimpleInducement(type, "Desperate Measures", 5, 50_000, false)
            InducementTypeCommon.EXTRA_TEAM_TRAINING -> SimpleInducement(type, "Extra Team Training", 8, 100_000, true)
            InducementTypeCommon.HALFLING_MASTER_CHEF -> SimpleInducement(type, "Hafling Master Chef", 1, 300_000, true, specialRulesModifier = mapOf(RegionalSpecialRule.HAFLING_THIMBLE_CUP to 1 / 3f))
            InducementTypeCommon.INFAMOUS_COACHING_STAFF -> InfamousCoachingStaffsInducementGroup(
                max = 2,
                enabled = true,
                items = listOf(
                    InfamousCoachingStaffInducement(
                        InfamousCoachingStaffType2020.JOSEF_BUGMAN,
                        1,
                        100_000,
                        named = true,
                        enabled = true,
                    ),
                )
            )
            InducementTypeCommon.MORTUARY_ASSISTANT -> SimpleInducement(type, "Mortuary Assistant", 1, 100_000, true, requirements = setOf(RegionalSpecialRule.SYLVANIAN_SPOTLIGHT))
            InducementTypeCommon.PART_TIME_ASSISTANT_COACH -> SimpleInducement(type, "Part-time Assistant Coaches", 1, 20_000, true)
            InducementTypeCommon.PLAGUE_DOCTOR -> SimpleInducement(type, "Plague Doctor", 1, 100_000, true, requirements = setOf(TeamSpecialRule.FAVOURED_OF_NURGLE))
            InducementTypeCommon.RIOTOUS_ROOKIE -> SimpleInducement(type, "Riotous Rookies", 1, 100_000, true, requirements = setOf(TeamSpecialRule.LOW_COST_LINEMEN))
            InducementTypeCommon.STANDARD_MERCENARY_PLAYERS -> StandardMercenaryInducement(enabled = true)
            InducementTypeCommon.STAR_PLAYERS -> StarPlayersInducementGroup(max = 2, enabled = true)
            InducementTypeCommon.TEMP_AGENCY_CHEERLEADER -> SimpleInducement(type, "Temp Agency Cheerleaders", 4, 20_000, true)
            InducementTypeCommon.WANDERING_APOTHECARY -> SimpleInducement(type, "Wandering Apothecaries", 2, 100_000, true)
            InducementTypeCommon.WEATHER_MAGE -> SimpleInducement(type, "Weather Mage", 1, 30_000, true)
            InducementTypeCommon.WIZARD -> WizardsInducementGroup(max = 1, enabled = true)
        }
        put(type, inducement)
    }
    InducementType2020.entries.forEach { type ->
        val inducement: Inducement<*> = when (type) {
            InducementType2020.BLOODWEISER_KEG -> SimpleInducement(type, "Bloodweiser Kegs", 2, 50_000, true)
            InducementType2020.SPECIAL_PLAY -> SimpleInducement(type, "Special Plays", 5, 100_000, true)
            InducementType2020.WAAAGH_DRUMMER -> SimpleInducement(type, "Waaagh! Drummer", 1, 50_000, true, requirements = setOf(RegionalSpecialRule.BADLANDS_BRAWL))
            InducementType2020.CAVORTING_NURGLINGS -> SimpleInducement(type, "Cavorting Nurglings", 3, 30_000, true, requirements = setOf(TeamSpecialRule.FAVOURED_OF_NURGLE))
            InducementType2020.DWARFEN_RUNESMITH -> SimpleInducement(type, "Dwarfen Runesmith", 1, 50_000, true, requirements = setOf(RegionalSpecialRule.OLD_WORLD_CLASSIC, RegionalSpecialRule.WORLDS_EDGE_SUPERLEAGUE))
            InducementType2020.HALFLING_HOTPOT -> SimpleInducement(type, "Halfing Hot Pot", 1, 80_000, true, requirements = setOf(RegionalSpecialRule.HAFLING_THIMBLE_CUP, RegionalSpecialRule.OLD_WORLD_CLASSIC), specialRulesModifier = mapOf(RegionalSpecialRule.HAFLING_THIMBLE_CUP to 0.75f))
            InducementType2020.MASTER_OF_BALLISTICS -> SimpleInducement(type, "Master of Ballistics", 1, 40_000, true, requirements = setOf(RegionalSpecialRule.HAFLING_THIMBLE_CUP, RegionalSpecialRule.OLD_WORLD_CLASSIC), specialRulesModifier = mapOf(RegionalSpecialRule.HAFLING_THIMBLE_CUP to 0.75f))
            InducementType2020.EXPANDED_MERCENARY_PLAYERS -> ExpandedMercenaryInducements(enabled = false)
            InducementType2020.GIANT -> SimpleInducement(type, "Giant", 1, 400_000, true)
            InducementType2020.DESPERATE_MEASURES -> SimpleInducement(type, "Desperate Measures", 5, 50_000, false)
            InducementType2020.BRETONNIAN_PASTRIES -> SimpleInducement(type, "Bretonnian Pastries", 1, 15_000, false)
            InducementType2020.BRETONNIAN_DAMSEL -> SimpleInducement(type, "Bretonnian Damsel", 1, 150_000, false)
            InducementType2020.CANOPIC_JAR -> SimpleInducement(type, "Canopic Jar", 1, 50_000, false)
        }
        put(type, inducement)
    }
}
