package fumbbl.net.utils

import com.jervisffb.engine.rules.builder.GameVersion
import com.jervisffb.engine.rules.common.roster.RegionalSpecialRule
import com.jervisffb.engine.rules.common.roster.TeamSpecialRule
import com.jervisffb.fumbbl.net.utils.selectLeague
import com.jervisffb.resources.bb2020.HUMAN_TEAM_BB2020
import com.jervisffb.resources.bb2025.CHAOS_DWARF_TEAM_BB2025
import com.jervisffb.resources.bb2025.HUMAN_TEAM_BB2025
import com.jervisffb.resources.bb2025.KHORNE_TEAM_BB2025
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class SelectLeagueTests {

    @Test
    fun `bb2025 regional rule becomes league and leaves team rules`() {
        val (league, teamRules) = selectLeague(
            GameVersion.BB2025,
            HUMAN_TEAM_BB2025,
            listOf(
                RegionalSpecialRule.OLD_WORLD_CLASSIC,
                TeamSpecialRule.LOW_COST_LINEMEN,
            ),
        )
        assertEquals(RegionalSpecialRule.OLD_WORLD_CLASSIC, league)
        assertEquals(listOf(TeamSpecialRule.LOW_COST_LINEMEN), teamRules)
    }

    @Test
    fun `bb2025 without regional rules leaves league unset`() {
        // Replays do not always carry the league selection. Single-league
        // rosters stay unambiguous (the team builder defaults to it).
        val (league, teamRules) = selectLeague(
            GameVersion.BB2025,
            KHORNE_TEAM_BB2025,
            listOf(TeamSpecialRule.FAVOURED_OF_KHORNE),
        )
        assertNull(league)
        assertEquals(listOf(TeamSpecialRule.FAVOURED_OF_KHORNE), teamRules)
    }

    @Test
    fun `bb2025 multiple regional rules fail loudly`() {
        assertFailsWith<IllegalStateException> {
            selectLeague(
                GameVersion.BB2025,
                CHAOS_DWARF_TEAM_BB2025,
                listOf(
                    RegionalSpecialRule.BADLANDS_BRAWL,
                    RegionalSpecialRule.CHAOS_CLASH,
                ),
            )
        }
    }

    @Test
    fun `bb2020 keeps every rule and has no league`() {
        val rules = listOf(
            RegionalSpecialRule.OLD_WORLD_CLASSIC,
            TeamSpecialRule.LOW_COST_LINEMEN,
        )
        val (league, teamRules) = selectLeague(GameVersion.BB2020, HUMAN_TEAM_BB2020, rules)
        assertNull(league)
        assertEquals(rules, teamRules)
    }
}
