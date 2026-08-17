package com.jervisffb.engine.bb2020

import com.jervisffb.engine.bb2020.inducements.BB2020InducementType
import com.jervisffb.engine.bb2020.inducements.ExpandedMercenaryInducements
import com.jervisffb.engine.common.inducements.BiasedRefereesInducementGroup
import com.jervisffb.engine.common.inducements.CommonInducementType
import com.jervisffb.engine.common.inducements.InfamousCoachingStaffsInducementGroup
import com.jervisffb.engine.common.inducements.StandardMercenaryInducement
import com.jervisffb.engine.common.inducements.StarPlayersInducementGroup
import com.jervisffb.engine.common.inducements.WizardsInducementGroup
import com.jervisffb.engine.model.inducements.settings.Inducement
import com.jervisffb.engine.model.inducements.settings.InducementType
import com.jervisffb.engine.common.inducements.SimpleInducement
import com.jervisffb.engine.rules.common.roster.RegionalSpecialRule
import com.jervisffb.engine.rules.common.roster.TeamSpecialRule

val DEFAULT_INDUCEMENTS_BB2020: Map<InducementType, Inducement<*>> = buildMap {
    CommonInducementType.entries.forEach { type ->
        val inducement: Inducement<*> = when (type) {
            CommonInducementType.BIASED_REFEREE -> BiasedRefereesInducementGroup(max = 1, enabled = true)
            CommonInducementType.BRIBE -> SimpleInducement(type, "Bribe", 3, 100_000, true) // Half price for Bribery and Corruption
            CommonInducementType.DESPERATE_MEASURES -> SimpleInducement(type, "Desperate Measures", 5, 50_000, false)
            CommonInducementType.EXTRA_TEAM_TRAINING -> SimpleInducement(type, "Extra Team Training", 8, 100_000, true)
            CommonInducementType.HALFLING_MASTER_CHEF -> SimpleInducement(type, "Hafling Master Chef", 1, 300_000, true, specialRulesModifier = mapOf(RegionalSpecialRule.HAFLING_THIMBLE_CUP to 1 / 3f))
            CommonInducementType.INFAMOUS_COACHING_STAFF -> InfamousCoachingStaffsInducementGroup(max = 2, enabled = true)
            CommonInducementType.MORTUARY_ASSISTANT -> SimpleInducement(type, "Mortuary Assistant", 1, 100_000, true, requirements = setOf(RegionalSpecialRule.SYLVANIAN_SPOTLIGHT))
            CommonInducementType.PART_TIME_ASSISTANT_COACH -> SimpleInducement(type, "Part-time Assistant Coaches", 1, 20_000, true)
            CommonInducementType.PLAGUE_DOCTOR -> SimpleInducement(type, "Plague Doctor", 1, 100_000, true, requirements = setOf(TeamSpecialRule.FAVOURED_OF_NURGLE))
            CommonInducementType.RIOTOUS_ROOKIE -> SimpleInducement(type, "Riotous Rookies", 1, 100_000, true, requirements = setOf(TeamSpecialRule.LOW_COST_LINEMEN))
            CommonInducementType.STANDARD_MERCENARY_PLAYERS -> StandardMercenaryInducement(enabled = true)
            CommonInducementType.STAR_PLAYERS -> StarPlayersInducementGroup(max = 2, enabled = true)
            CommonInducementType.TEMP_AGENCY_CHEERLEADER -> SimpleInducement(type, "Temp Agency Cheerleaders", 4, 20_000, true)
            CommonInducementType.WANDERING_APOTHECARY -> SimpleInducement(type, "Wandering Apothecaries", 2, 100_000, true)
            CommonInducementType.WEATHER_MAGE -> SimpleInducement(type, "Weather Mage", 1, 30_000, true)
            CommonInducementType.WIZARD -> WizardsInducementGroup(max = 1, enabled = true)
        }
        put(type, inducement)
    }
    BB2020InducementType.entries.forEach { type ->
        val inducement: Inducement<*> = when (type) {
            BB2020InducementType.BLOODWEISER_KEG -> SimpleInducement(type, "Bloodweiser Kegs", 2, 50_000, true)
            BB2020InducementType.SPECIAL_PLAY -> SimpleInducement(type, "Special Plays", 5, 100_000, true)
            BB2020InducementType.WAAAGH_DRUMMER -> SimpleInducement(type, "Waaagh! Drummer", 1, 50_000, true, requirements = setOf(RegionalSpecialRule.BADLANDS_BRAWL))
            BB2020InducementType.CAVORTING_NURGLINGS -> SimpleInducement(type, "Cavorting Nurglings", 3, 30_000, true, requirements = setOf(TeamSpecialRule.FAVOURED_OF_NURGLE))
            BB2020InducementType.DWARFEN_RUNESMITH -> SimpleInducement(type, "Dwarfen Runesmith", 1, 50_000, true, requirements = setOf(RegionalSpecialRule.OLD_WORLD_CLASSIC, RegionalSpecialRule.WORLDS_EDGE_SUPERLEAGUE))
            BB2020InducementType.HALFLING_HOTPOT -> SimpleInducement(type, "Halfing Hot Pot", 1, 80_000, true, requirements = setOf(RegionalSpecialRule.HAFLING_THIMBLE_CUP, RegionalSpecialRule.OLD_WORLD_CLASSIC), specialRulesModifier = mapOf(RegionalSpecialRule.HAFLING_THIMBLE_CUP to 0.75f))
            BB2020InducementType.MASTER_OF_BALLISTICS -> SimpleInducement(type, "Master of Ballistics", 1, 40_000, true, requirements = setOf(RegionalSpecialRule.HAFLING_THIMBLE_CUP, RegionalSpecialRule.OLD_WORLD_CLASSIC), specialRulesModifier = mapOf(RegionalSpecialRule.HAFLING_THIMBLE_CUP to 0.75f))
            BB2020InducementType.EXPANDED_MERCENARY_PLAYERS -> ExpandedMercenaryInducements(enabled = false)
            BB2020InducementType.GIANT -> SimpleInducement(type, "Giant", 1, 400_000, true)
            BB2020InducementType.DESPERATE_MEASURES -> SimpleInducement(type, "Desperate Measures", 5, 50_000, false)
            BB2020InducementType.BRETONNIAN_PASTRIES -> SimpleInducement(type, "Bretonnian Pastries", 1, 15_000, false)
            BB2020InducementType.BRETONNIAN_DAMSEL -> SimpleInducement(type, "Bretonnian Damsel", 1, 150_000, false)
            BB2020InducementType.CANOPIC_JAR -> SimpleInducement(type, "Canopic Jar", 1, 50_000, false)
        }
        put(type, inducement)
    }
}
