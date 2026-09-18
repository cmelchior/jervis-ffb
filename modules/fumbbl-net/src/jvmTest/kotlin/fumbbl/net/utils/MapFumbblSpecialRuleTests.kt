package fumbbl.net.utils

import com.jervisffb.engine.rules.common.roster.RegionalSpecialRule
import com.jervisffb.engine.rules.common.roster.TeamSpecialRule
import com.jervisffb.fumbbl.net.model.SpecialRule
import com.jervisffb.fumbbl.net.utils.mapFumbblSpecialRule
import kotlin.test.Test
import kotlin.test.assertEquals

class MapFumbblSpecialRuleTests {

    @Test
    fun `every wire rule maps to its jervis equivalent`() {
        assertEquals(RegionalSpecialRule.BADLANDS_BRAWL, mapFumbblSpecialRule(SpecialRule.BADLANDS_BRAWL))
        assertEquals(TeamSpecialRule.BRIBERY_AND_CORRUPTION, mapFumbblSpecialRule(SpecialRule.BRIBERY_AND_CORRUPTION))
        assertEquals(TeamSpecialRule.BRAWLIN_BRUTES, mapFumbblSpecialRule(SpecialRule.BRAWLIN_BRUTES))
        assertEquals(RegionalSpecialRule.ELVEN_KINGDOMS_LEAGUE, mapFumbblSpecialRule(SpecialRule.ELVEN_KINGDOMS_LEAGUE))
        assertEquals(TeamSpecialRule.FAVOURED_OF_KHORNE, mapFumbblSpecialRule(SpecialRule.FAVOURED_OF_KHORNE))
        assertEquals(TeamSpecialRule.FAVOURED_OF_NURGLE, mapFumbblSpecialRule(SpecialRule.FAVOURED_OF_NURGLE))
        assertEquals(TeamSpecialRule.FAVOURED_OF_SLAANESH, mapFumbblSpecialRule(SpecialRule.FAVOURED_OF_SLAANESH))
        assertEquals(TeamSpecialRule.FAVOURED_OF_TZEENTCH, mapFumbblSpecialRule(SpecialRule.FAVOURED_OF_TZEENTCH))
        assertEquals(TeamSpecialRule.FAVOURED_OF_CHAOS_UNDIVIDED, mapFumbblSpecialRule(SpecialRule.FAVOURED_OF_UNDIVIDED))
        assertEquals(RegionalSpecialRule.HAFLING_THIMBLE_CUP, mapFumbblSpecialRule(SpecialRule.HALFLING_THIMBLE_CUP))
        assertEquals(TeamSpecialRule.LOW_COST_LINEMEN, mapFumbblSpecialRule(SpecialRule.LOW_COST_LINEMEN))
        assertEquals(RegionalSpecialRule.LUSTRIAN_SUPERLEAGUE, mapFumbblSpecialRule(SpecialRule.LUSTRIAN_SUPERLEAGUE))
        assertEquals(TeamSpecialRule.MASTERS_OF_UNDEATH, mapFumbblSpecialRule(SpecialRule.MASTERS_OF_UNDEATH))
        assertEquals(RegionalSpecialRule.OLD_WORLD_CLASSIC, mapFumbblSpecialRule(SpecialRule.OLD_WORLD_CLASSIC))
        assertEquals(TeamSpecialRule.SWARMING, mapFumbblSpecialRule(SpecialRule.SWARMING))
        assertEquals(RegionalSpecialRule.SYLVANIAN_SPOTLIGHT, mapFumbblSpecialRule(SpecialRule.SYLVANIAN_SPOTLIGHT))
        assertEquals(RegionalSpecialRule.UNDERWORLD_CHALLENGE, mapFumbblSpecialRule(SpecialRule.UNDERWORLD_CHALLENGE))
        assertEquals(RegionalSpecialRule.WORLDS_EDGE_SUPERLEAGUE, mapFumbblSpecialRule(SpecialRule.WORLDS_EDGE_SUPERLEAGUE))
    }
}
