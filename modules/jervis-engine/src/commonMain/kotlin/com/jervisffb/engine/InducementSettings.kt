package com.jervisffb.engine

import com.jervisffb.engine.model.inducements.settings.BiasedRefereesInducement
import com.jervisffb.engine.model.inducements.settings.ExpandedMercenaryInducements
import com.jervisffb.engine.model.inducements.settings.Inducement
import com.jervisffb.engine.model.inducements.settings.InducementBuilder
import com.jervisffb.engine.model.inducements.settings.InducementType
import com.jervisffb.engine.model.inducements.settings.InfamousCoachingStaffsInducement
import com.jervisffb.engine.model.inducements.settings.SimpleInducement
import com.jervisffb.engine.model.inducements.settings.StandardMercenaryInducements
import com.jervisffb.engine.model.inducements.settings.StarPlayersInducement
import com.jervisffb.engine.model.inducements.settings.WizardsInducement
import com.jervisffb.engine.rules.bb2020.roster.RegionalSpecialRule
import com.jervisffb.engine.rules.bb2020.roster.TeamSpecialRule
import kotlinx.serialization.Serializable

val DEFAULT_INDUCEMENTS = mapOf<InducementType, Inducement<*>>(

    // Standard Rules
    InducementType.TEMP_AGENCY_CHEERLEADER to SimpleInducement(InducementType.TEMP_AGENCY_CHEERLEADER, "Temp Agency Cheerleaders", 4, 20_000, true),
    InducementType.PART_TIME_ASSISTANT_COACH to SimpleInducement(InducementType.PART_TIME_ASSISTANT_COACH, "Part-time Assistant Coaches", 1, 20_000, true),
    InducementType.WEATHER_MAGE to SimpleInducement(InducementType.WEATHER_MAGE, "Weather Mage", 1, 30_000, true),
    InducementType.BLOODWEISER_KEG to SimpleInducement(InducementType.BLOODWEISER_KEG, "Bloodweiser Kegs", 2, 50_000, true),
    InducementType.SPECIAL_PLAY to SimpleInducement(InducementType.SPECIAL_PLAY, "Special Plays", 5, 100_000, true),
    InducementType.EXTRA_TEAM_TRAINING to SimpleInducement(InducementType.EXTRA_TEAM_TRAINING, "Extra Team Training", 8, 100_000, true),
    InducementType.BRIBE to SimpleInducement(InducementType.BRIBE, "Bribe", 3, 100_000, true), // Half price for Bribery and Corruption
    InducementType.WANDERING_APOTHECARY to SimpleInducement(InducementType.WANDERING_APOTHECARY, "Wandering Apothecaries", 2, 100_000, true),
    InducementType.MORTUARY_ASSISTANT to SimpleInducement(InducementType.MORTUARY_ASSISTANT, "Mortuary Assistant", 1, 100_000, true, requirements = listOf(RegionalSpecialRule.SYLVIAN_SPOTLIGHT)),
    InducementType.PLAGUE_DOCTOR to SimpleInducement(InducementType.PLAGUE_DOCTOR, "Plague Doctor", 1, 100_000, true, requirements = listOf(TeamSpecialRule.FAVOURED_OF_NURGLE)),
    InducementType.RIOTOUS_ROOKIE to SimpleInducement(InducementType.RIOTOUS_ROOKIE, "Riotous Rookies", 1, 100_000, true, requirements = listOf(TeamSpecialRule.LOW_COST_LINEMEN)),
    InducementType.HALFLING_MASTER_CHEF to SimpleInducement(InducementType.HALFLING_MASTER_CHEF, "Hafling Master Chef", 1, 300_000, true, modifier = listOf(Pair(RegionalSpecialRule.HAFLING_THIMBLE_CUP, 1/3f))),
    InducementType.STANDARD_MERCENARY_PLAYERS to StandardMercenaryInducements(enabled = true),
    InducementType.STAR_PLAYERS to StarPlayersInducement(max = 2, enabled = true),
    InducementType.INFAMOUS_COACHING_STAFF to SimpleInducement(InducementType.INFAMOUS_COACHING_STAFF, "(In)famous Coaching Staff", 2, null, true),
    InducementType.WIZARD to WizardsInducement(max = 1, enabled = true),
    InducementType.BIASED_REFEREE to BiasedRefereesInducement(max = 1, enabled = true),

    // DeathZone
    InducementType.WAAAGH_DRUMMER to SimpleInducement(InducementType.WAAAGH_DRUMMER, "Waaagh! Drummer", 1, 50_000, true, requirements = listOf(RegionalSpecialRule.BADLANDS_BRAWL)),
    InducementType.CAVORTING_NURGLINGS to SimpleInducement(InducementType.CAVORTING_NURGLINGS, "Cavorting Nurglings", 3, 30_000, true, requirements = listOf(TeamSpecialRule.FAVOURED_OF_NURGLE)),
    InducementType.DWARFEN_RUNESMITH to SimpleInducement(InducementType.DWARFEN_RUNESMITH, "Dwarfen Runesmith", 1, 50_000, true, requirements = listOf(RegionalSpecialRule.OLD_WORLD_CLASSIC, RegionalSpecialRule.WORLDS_EDGE_SUPERLEAGUE)),
    InducementType.HALFLING_HOTPOT to SimpleInducement(InducementType.HALFLING_HOTPOT, "Halfing Hot Pot", 1, 80_000, true, requirements = listOf(RegionalSpecialRule.HAFLING_THIMBLE_CUP, RegionalSpecialRule.OLD_WORLD_CLASSIC), modifier = listOf(Pair(RegionalSpecialRule.HAFLING_THIMBLE_CUP, 0.75f))),
    InducementType.MASTER_OF_BALLISTICS to SimpleInducement(InducementType.MASTER_OF_BALLISTICS, "Master of Ballistics", 1, 40_000, true, requirements = listOf(RegionalSpecialRule.HAFLING_THIMBLE_CUP, RegionalSpecialRule.OLD_WORLD_CLASSIC), modifier = listOf(Pair(RegionalSpecialRule.HAFLING_THIMBLE_CUP, 0.75f))),
    InducementType.EXPANDED_MERCENARY_PLAYERS to ExpandedMercenaryInducements(enabled = false),
    InducementType.GIANT to SimpleInducement(InducementType.GIANT, "Giant", 1, 400_000, true)
)

@Serializable
class InducementSettings(private val inducements: Map<InducementType, Inducement<*>>) : MutableMap<InducementType, Inducement<*>> by inducements.toMutableMap() {
    fun toBuilder(): Builder {
        val builders = this.entries.associate {
            it.key to it.value.toBuilder()
        }
        return Builder(builders)
    }

    class Builder(private val builders: Map<InducementType, InducementBuilder>) : MutableMap<InducementType, InducementBuilder> by builders.toMutableMap() {
        fun build(): InducementSettings {
            val inducements = this.entries.associate {
                it.key to when (val builder = it.value) {
                    is BiasedRefereesInducement.Builder -> builder.build()
                    is ExpandedMercenaryInducements.Builder -> builder.build()
                    is InfamousCoachingStaffsInducement.Builder -> builder.build()
                    is SimpleInducement.Builder -> builder.build()
                    is StandardMercenaryInducements.Builder -> builder.build()
                    is StarPlayersInducement.Builder -> builder.build()
                    is WizardsInducement.Builder -> builder.build()
                }
            }
            return InducementSettings(inducements)
        }
    }

}





