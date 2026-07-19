package com.jervisffb.engine.rules.bb2020

import com.jervisffb.engine.model.inducements.settings.BiasedRefereesInducementList
import com.jervisffb.engine.model.inducements.settings.ExpandedMercenaryInducements
import com.jervisffb.engine.model.inducements.settings.Inducement
import com.jervisffb.engine.model.inducements.settings.InducementType
import com.jervisffb.engine.model.inducements.settings.InfamousCoachingStaffsInducementList
import com.jervisffb.engine.model.inducements.settings.SimpleInducement
import com.jervisffb.engine.model.inducements.settings.StandardMercenaryInducement
import com.jervisffb.engine.model.inducements.settings.StarPlayersInducementList
import com.jervisffb.engine.model.inducements.settings.WizardsInducementList
import com.jervisffb.engine.rules.common.roster.RegionalSpecialRule
import com.jervisffb.engine.rules.common.roster.TeamSpecialRule

val DEFAULT_INDUCEMENTS_BB2020 = mapOf<InducementType, Inducement<*>>(
    // Standard Rules
    InducementType.TEMP_AGENCY_CHEERLEADER to SimpleInducement(InducementType.TEMP_AGENCY_CHEERLEADER, "Temp Agency Cheerleaders", 4, 20_000, true),
    InducementType.PART_TIME_ASSISTANT_COACH to SimpleInducement(InducementType.PART_TIME_ASSISTANT_COACH, "Part-time Assistant Coaches", 1, 20_000, true),
    InducementType.WEATHER_MAGE to SimpleInducement(InducementType.WEATHER_MAGE, "Weather Mage", 1, 30_000, true),
    InducementType.BLOODWEISER_KEG to SimpleInducement(InducementType.BLOODWEISER_KEG, "Bloodweiser Kegs", 2, 50_000, true),
    InducementType.SPECIAL_PLAY to SimpleInducement(InducementType.SPECIAL_PLAY, "Special Plays", 5, 100_000, true),
    InducementType.EXTRA_TEAM_TRAINING to SimpleInducement(InducementType.EXTRA_TEAM_TRAINING, "Extra Team Training", 8, 100_000, true),
    InducementType.BRIBE to SimpleInducement(InducementType.BRIBE, "Bribe", 3, 100_000, true), // Half price for Bribery and Corruption
    InducementType.WANDERING_APOTHECARY to SimpleInducement(InducementType.WANDERING_APOTHECARY, "Wandering Apothecaries", 2, 100_000, true),
    InducementType.MORTUARY_ASSISTANT to SimpleInducement(InducementType.MORTUARY_ASSISTANT, "Mortuary Assistant", 1, 100_000, true, requirements = setOf(RegionalSpecialRule.SYLVANIAN_SPOTLIGHT)),
    InducementType.PLAGUE_DOCTOR to SimpleInducement(InducementType.PLAGUE_DOCTOR, "Plague Doctor", 1, 100_000, true, requirements = setOf(TeamSpecialRule.FAVOURED_OF_NURGLE)),
    InducementType.RIOTOUS_ROOKIE to SimpleInducement(InducementType.RIOTOUS_ROOKIE, "Riotous Rookies", 1, 100_000, true, requirements = setOf(TeamSpecialRule.LOW_COST_LINEMEN)),
    InducementType.HALFLING_MASTER_CHEF to SimpleInducement(InducementType.HALFLING_MASTER_CHEF, "Hafling Master Chef", 1, 300_000, true, specialRulesModifier = mapOf(RegionalSpecialRule.HAFLING_THIMBLE_CUP to 1/3f)),
    InducementType.STANDARD_MERCENARY_PLAYERS to StandardMercenaryInducement(enabled = true),
    InducementType.STAR_PLAYERS to StarPlayersInducementList(max = 2, enabled = true),
    InducementType.INFAMOUS_COACHING_STAFF to InfamousCoachingStaffsInducementList(max = 2, enabled = true),
    InducementType.WIZARD to WizardsInducementList(max = 1, enabled = true),
    InducementType.BIASED_REFEREE to BiasedRefereesInducementList(max = 1, enabled = true),

    // DeathZone
    InducementType.WAAAGH_DRUMMER to SimpleInducement(InducementType.WAAAGH_DRUMMER, "Waaagh! Drummer", 1, 50_000, true, requirements = setOf(RegionalSpecialRule.BADLANDS_BRAWL)),
    InducementType.CAVORTING_NURGLINGS to SimpleInducement(InducementType.CAVORTING_NURGLINGS, "Cavorting Nurglings", 3, 30_000, true, requirements = setOf(TeamSpecialRule.FAVOURED_OF_NURGLE)),
    InducementType.DWARFEN_RUNESMITH to SimpleInducement(InducementType.DWARFEN_RUNESMITH, "Dwarfen Runesmith", 1, 50_000, true, requirements = setOf(RegionalSpecialRule.OLD_WORLD_CLASSIC, RegionalSpecialRule.WORLDS_EDGE_SUPERLEAGUE)),
    InducementType.HALFLING_HOTPOT to SimpleInducement(InducementType.HALFLING_HOTPOT, "Halfing Hot Pot", 1, 80_000, true, requirements = setOf(RegionalSpecialRule.HAFLING_THIMBLE_CUP, RegionalSpecialRule.OLD_WORLD_CLASSIC), specialRulesModifier = mapOf(RegionalSpecialRule.HAFLING_THIMBLE_CUP to 0.75f)),
    InducementType.MASTER_OF_BALLISTICS to SimpleInducement(InducementType.MASTER_OF_BALLISTICS, "Master of Ballistics", 1, 40_000, true, requirements = setOf(RegionalSpecialRule.HAFLING_THIMBLE_CUP, RegionalSpecialRule.OLD_WORLD_CLASSIC), specialRulesModifier = mapOf(RegionalSpecialRule.HAFLING_THIMBLE_CUP to 0.75f)),
    InducementType.EXPANDED_MERCENARY_PLAYERS to ExpandedMercenaryInducements(enabled = false),
    InducementType.GIANT to SimpleInducement(InducementType.GIANT, "Giant", 1, 400_000, true),
    InducementType.DESPERATE_MEASURES to SimpleInducement(InducementType.DESPERATE_MEASURES, "Desperate Measures", 5, 50_000, false),
)
