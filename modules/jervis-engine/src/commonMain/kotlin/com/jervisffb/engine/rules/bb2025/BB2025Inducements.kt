package com.jervisffb.engine.rules.bb2025

import com.jervisffb.engine.model.inducements.settings.BiasedRefereesInducement
import com.jervisffb.engine.model.inducements.settings.Inducement
import com.jervisffb.engine.model.inducements.settings.InducementType
import com.jervisffb.engine.model.inducements.settings.SimpleInducement
import com.jervisffb.engine.model.inducements.settings.StandardMercenaryInducements
import com.jervisffb.engine.model.inducements.settings.StarPlayersInducement
import com.jervisffb.engine.model.inducements.settings.WizardsInducement
import com.jervisffb.engine.rules.common.roster.RegionalSpecialRule
import com.jervisffb.engine.rules.common.roster.TeamSpecialRule

// See page 142 in the BB2025 rulebook.
val DEFAULT_INDUCEMENTS_BB2025 = mapOf<InducementType, Inducement<*>>(

    // Standard Rules
    InducementType.PRAYERS_TO_NUFFLE to SimpleInducement(InducementType.PRAYERS_TO_NUFFLE, "Prayers to Nuffle", 3, 10_000, true),
    InducementType.PART_TIME_ASSISTANT_COACH to SimpleInducement(InducementType.PART_TIME_ASSISTANT_COACH, "Part-time Assistant Coaches", 5, 20_000, true),
    InducementType.TEMP_AGENCY_CHEERLEADER to SimpleInducement(InducementType.TEMP_AGENCY_CHEERLEADER, "Temp Agency Cheerleaders", 5, 5_000, true),
    InducementType.TEAM_MASCOT to SimpleInducement(InducementType.TEAM_MASCOT, "Team Mascot", 1, 25_000, true),
    InducementType.WEATHER_MAGE to SimpleInducement(InducementType.WEATHER_MAGE, "Weather Mage", 1, 25_000, true),
    InducementType.BLITZERS_BEST_KEGS to SimpleInducement(InducementType.BLITZERS_BEST_KEGS, "Blitzer's Best Kegs", 2, 50_000, true),
    InducementType.BRIBE to SimpleInducement(InducementType.BRIBE, "Bribe", 3, 100_000, true), // 0.5x price and 2x amount for Bribery and Corruption
    InducementType.EXTRA_TEAM_TRAINING to SimpleInducement(InducementType.EXTRA_TEAM_TRAINING, "Extra Team Training", 8, 100_000, true),
    InducementType.MORTUARY_ASSISTANT to SimpleInducement(InducementType.MORTUARY_ASSISTANT, "Mortuary Assistant", 1, 100_000, true, requirements = listOf(TeamSpecialRule.MASTERS_OF_UNDEATH)),
    InducementType.PLAGUE_DOCTOR to SimpleInducement(InducementType.PLAGUE_DOCTOR, "Plague Doctor", 1, 100_000, true, requirements = listOf(TeamSpecialRule.FAVOURED_OF_NURGLE)),
    InducementType.RIOTOUS_ROOKIE to SimpleInducement(InducementType.RIOTOUS_ROOKIE, "Riotous Rookies", 1, 150_000, true, requirements = listOf(TeamSpecialRule.LOW_COST_LINEMEN)),
    InducementType.WANDERING_APOTHECARY to SimpleInducement(InducementType.WANDERING_APOTHECARY, "Wandering Apothecaries", 2, 100_000, true),
    InducementType.HALFLING_MASTER_CHEF to SimpleInducement(InducementType.HALFLING_MASTER_CHEF, "Hafling Master Chef", 1, 300_000, true, modifier = listOf(Pair(RegionalSpecialRule.HAFLING_THIMBLE_CUP, 1/3f))), // TODO Should only apply to hafling teams
    InducementType.BIASED_REFEREE to BiasedRefereesInducement(max = 1, enabled = true),
    InducementType.INFAMOUS_COACHING_STAFF to SimpleInducement(InducementType.INFAMOUS_COACHING_STAFF, "(In)famous Coaching Staff", 1, null, true),
    InducementType.STANDARD_MERCENARY_PLAYERS to StandardMercenaryInducements(enabled = true),
    InducementType.STAR_PLAYERS to StarPlayersInducement(max = 2, enabled = true),
    InducementType.WIZARD to WizardsInducement(max = 1, enabled = true),

    // Spike 19
    InducementType.BRETONNIAN_PASTRIES to SimpleInducement(InducementType.BRETONNIAN_PASTRIES, "Bretonnian Pastries", 1, 15_000, false),
    // While not specified in the rules, this is probably counted as a Wizard
    InducementType.BRETONNIAN_DAMSEL to SimpleInducement(InducementType.BRETONNIAN_DAMSEL, "Bretonnian Damsel", 1, 150_000, false),

    // Spike 20
    InducementType.CANOPIC_JAR to SimpleInducement(InducementType.CANOPIC_JAR, "Canopic Jar", 1, 50_000, false),
)
