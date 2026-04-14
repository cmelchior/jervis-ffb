package com.jervisffb.test.bb2025.inducements

import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.PitchSquareSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.ext.playerNo
import com.jervisffb.engine.model.getSkill
import com.jervisffb.engine.model.inducements.TeamMascot
import com.jervisffb.engine.model.modifiers.TeamFeatureType
import com.jervisffb.engine.rules.BB2025Rules
import com.jervisffb.engine.rules.StandardBB2025Rules
import com.jervisffb.engine.rules.bb2025.skills.Pro
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.procedures.actions.move.DodgeRoll
import com.jervisffb.engine.rules.common.rerolls.RegularTeamReroll
import com.jervisffb.engine.rules.common.rerolls.TeamMascotReroll
import com.jervisffb.engine.rules.common.roster.PlayerSpecialRule
import com.jervisffb.engine.rules.common.roster.TeamSpecialRule
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.SmartMoveTo
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.defaultDetermineKickingTeam
import com.jervisffb.test.defaultKickOffAwayTeam
import com.jervisffb.test.defaultKickOffHomeTeam
import com.jervisffb.test.defaultPregame
import com.jervisffb.test.defaultSetup
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.moveTo
import com.jervisffb.test.skipTurns
import com.jervisffb.test.teamCaptainRoll
import com.jervisffb.test.teamMascotRoll
import com.jervisffb.test.utils.SelectSkillReroll
import com.jervisffb.test.utils.TeamRerollSelected
import com.jervisffb.test.utils.assertActive
import com.jervisffb.test.utils.assertStanding
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Class testing the functionality of the [TeamMascot] Inducement.
 */
class TeamMascotTests: JervisGameBB2025Test() {

    override val rules: BB2025Rules = StandardBB2025Rules().update {
        hasExtraTime = true
        turnsInExtraTime = 8
    }

    private fun setupWithTeamMascot() {
        super.setUp()
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
        )
        // Until we get proper Inducements Support, manually add Mascot
        val mascot = TeamMascot(state.awayTeam.id)
        awayTeam.mascots.add(mascot)
        awayTeam.rerolls.add(mascot.reroll)
    }

    @Test
    fun plus1ToCheeringFans() {
        setupWithTeamMascot()
        assertEquals(0, homeTeam.cheerleaders)
        assertEquals(0, awayTeam.cheerleaders)
        controller.rollForward(
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(1.d6, 5.d6), // Roll Cheering Fans
                    4.d6, // Home team roll
                    3.d6, // Away team roll
                ),
            ),
        )
        assertTrue(homeTeam.hasFeature(TeamFeatureType.CHEERING_FANS_OFFENSIVE_ASSIST))
        assertTrue(awayTeam.hasFeature(TeamFeatureType.CHEERING_FANS_OFFENSIVE_ASSIST))
    }

    @Test
    fun plus1ToCheeringFansWhenRerollUsed() {
        setupWithTeamMascot()
        controller.rollForward(
            *defaultKickOffHomeTeam(
                placeKick = PitchSquareSelected(25, 0),
                deviate = DiceRollResults(2.d8, 1.d6), // Out-of-bounds
                bounce = null,
            ),
            PlayerSelected("A6".playerId), // Give ball to this player and start turn
        )
        val player = awayTeam[6.playerNo]
        assertTrue(player.hasBall())

        // Give player enough move to reach the End Zone in one turn.
        // Use Mascot on Dodge along the way
        player.movesLeft = 20
        controller.rollForward(
            PlayerSelected(player.id),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            SmartMoveTo(12, 4),
            *moveTo(11, 3),
            1.d6, // Fail dodge
            TeamRerollSelected<TeamMascotReroll>(),
            *teamMascotRoll(4.d6),
            4.d6, // Dodge works
            SmartMoveTo(0, 3)
        )
        assertTrue(awayTeam.mascots.single().reroll.rerollUsed)
        controller.rollForward(
            Confirm,
            *defaultSetup(homeFirst = false),
            *defaultKickOffAwayTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(1.d6, 5.d6), // Roll Cheering Fans
                    3.d6, // Away team roll
                    4.d6, // Home team roll
                ),
            ),
        )
        assertTrue(homeTeam.hasFeature(TeamFeatureType.CHEERING_FANS_OFFENSIVE_ASSIST))
        assertTrue(awayTeam.hasFeature(TeamFeatureType.CHEERING_FANS_OFFENSIVE_ASSIST))
    }

    @Test
    fun addRerollAtStartOfGame() {
        // TODO Right now this test does nothing useful. Rework when buying inducements are added
        setupWithTeamMascot()
        val mascot = awayTeam.mascots.single()
        assertTrue(awayTeam.rerolls.contains(mascot.reroll))
        assertTrue(homeTeam.mascots.isEmpty())
        assertFalse(homeTeam.rerolls.any { it is TeamMascotReroll })
    }

    @Test
    fun usedRerollComesBackNextHalf() {
        setupWithTeamMascot()
        controller.rollForward(
            *defaultKickOffHomeTeam()
        )
        val player = awayTeam["A1".playerId]
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            1.d6, // Fail dodge
            TeamRerollSelected<TeamMascotReroll>(),
            *teamMascotRoll(4.d6),
            4.d6, // Dodge works
            EndAction
        )
        assertTrue(awayTeam.mascots.single().reroll.rerollUsed)
        controller.rollForward(
            *skipTurns(16)
        )
        controller.rollForward(
            *defaultSetup(homeFirst = false),
            *defaultKickOffAwayTeam()
        )
        assertFalse(awayTeam.mascots.single().reroll.rerollUsed)
    }

    @Test
    fun usedRerollDoesNotComeBackForExtraTime() {
        setupWithTeamMascot()
        controller.rollForward(
            *defaultKickOffHomeTeam(),
            *skipTurns(16),
            *defaultSetup(homeFirst = false),
            *defaultKickOffAwayTeam(),
            *skipTurns(1)
        )
        val player = awayTeam["A1".playerId]
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            1.d6, // Fail dodge
            TeamRerollSelected<TeamMascotReroll>(),
            *teamMascotRoll(4.d6),
            4.d6, // Dodge works
            EndAction
        )
        assertTrue(awayTeam.mascots.single().reroll.rerollUsed)
        controller.rollForward(
            *skipTurns(15)
        )
        // Go into Extra Time
        controller.rollForward(
            *defaultDetermineKickingTeam(),
            *defaultSetup(),
            *defaultKickOffHomeTeam()
        )
        assertTrue(awayTeam.mascots.single().reroll.rerollUsed)
    }

    @Test
    fun proDoesNotWorkOnMascotRolls() {
        setupWithTeamMascot()
        controller.rollForward(
            *defaultKickOffHomeTeam(),
            *skipTurns(16),
            *defaultSetup(homeFirst = false),
            *defaultKickOffAwayTeam(),
            *skipTurns(1)
        )
        val player = awayTeam["A1".playerId]
        player.addSkill(SkillType.PRO)
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            1.d6, // Fail dodge
            TeamRerollSelected<TeamMascotReroll>(),
            4.d6, // Team Mascot roll, skip rerolls because none is available
            4.d6, // Dodge works
            EndAction
        )
        player.assertStanding()
    }

    // Keeping the Mascot re-roll using Team Captain, does not prevent the need
    // to roll for Mascot, either the first or 2nd time it is used.
    @Test
    fun teamCaptainDoesNotPreventRollToWork() {
        setupWithTeamMascot()
        awayTeam.specialRules.add(TeamSpecialRule.TEAM_CAPTAIN)
        val player = awayTeam["A1".playerId]
        player.extraSpecialRules.add(PlayerSpecialRule.TEAM_CAPTAIN)
        controller.rollForward(
            *defaultKickOffHomeTeam()
        )
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            1.d6, // Fail dodge
            TeamRerollSelected<TeamMascotReroll>(),
            *teamMascotRoll(4.d6),
            *teamCaptainRoll(6.d6), // Save Mascot re-roll
            4.d6, // Dodge works
            *moveTo(13, 5),
            *moveTo(14, 5),
            1.d6, // Fail dodge (again)
            TeamRerollSelected<TeamMascotReroll>(),
            *teamMascotRoll(4.d6),
            *teamCaptainRoll(4.d6),
            4.d6, // Dodge works
            EndAction
        )
        assertTrue(awayTeam.mascots.single().reroll.rerollUsed)
        awayTeam.assertActive()
        player.assertStanding()
    }

    @Test
    fun teamRerollCannotBeUsedToRerollFailure() {
        setupWithTeamMascot()
        val player = awayTeam["A1".playerId]
        controller.rollForward(
            *defaultKickOffHomeTeam()
        )
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            1.d6, // Fail dodge
            TeamRerollSelected<TeamMascotReroll>(),
            *teamMascotRoll(4.d6),
        )
        assertEquals(DodgeRoll.ReRollDie, controller.currentNode())
    }

    @Ignore
    @Test
    fun skillRerollCanBeUsedToRerollFailure() {
        TODO("No known skills can offer this reroll. Maybe Hafling Luck, but right now it acts like Pro")
    }

    // Stress-testing the re-roll framework by the following sequence:
    // 1. Player fails a Dodge Roll.
    // 2. Use Pro, but fail the 3+
    // 3. Use Mascot to reroll Pro Roll
    // 4. Fail Mascot Roll
    // 5. Use Team Captain to save Mascot. It fails
    // 6. Use Team Reroll to replace failed Mascot
    // 7. Use Team Captain to save Team Reroll
    // 8. Reroll Dodge Roll with Team Reroll and succeed.
    @Test
    fun worksAcrossChainOfRolls() {
        setupWithTeamMascot()
        awayTeam.specialRules.add(TeamSpecialRule.TEAM_CAPTAIN)
        val player = awayTeam["A1".playerId].apply {
            extraSpecialRules.add(PlayerSpecialRule.TEAM_CAPTAIN)
            addSkill(SkillType.PRO)
        }
        controller.rollForward(
            *defaultKickOffHomeTeam(),
            *activatePlayer(player, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            1.d6, // Fail dodge
            SelectSkillReroll(SkillType.PRO),
            2.d6, // Fail Pro roll
            TeamRerollSelected<TeamMascotReroll>(),
            *teamMascotRoll(3.d6), // Fail mascot roll
            TeamRerollSelected<RegularTeamReroll>(), // Use normal reroll instead of Mascot that failed
            *teamCaptainRoll(6.d6), // Save Team Reroll replacing Mascot
            *teamCaptainRoll(5.d6), // Fail to save Mascot
            4.d6, // Pro re-roll works
            4.d6, // Dodge works
        )
        player.assertActive()
        player.assertStanding()
        assertTrue(player.getSkill<Pro>().used)
        assertTrue(player.getSkill<Pro>().rerollUsed)
        assertTrue(awayTeam.mascots.single().reroll.rerollUsed)
        assertTrue(awayTeam.rerolls.filterIsInstance<RegularTeamReroll>().none { it.rerollUsed })
    }
}
