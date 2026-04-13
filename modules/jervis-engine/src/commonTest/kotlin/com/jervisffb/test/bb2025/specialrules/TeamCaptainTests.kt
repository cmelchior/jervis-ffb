package com.jervisffb.test.bb2025.specialrules

import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DogoutSelected
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndSetup
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.locations.DogOut
import com.jervisffb.engine.rules.common.TeamCaptainNotOnField
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.procedures.SetupTeam
import com.jervisffb.engine.rules.common.procedures.TheKickOff
import com.jervisffb.engine.rules.common.rerolls.RegularTeamReroll
import com.jervisffb.engine.rules.common.roster.PlayerSpecialRule
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.defaultAwaySetup
import com.jervisffb.test.defaultHomeSetup
import com.jervisffb.test.defaultPregame
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.moveTo
import com.jervisffb.test.teamCaptainRoll
import com.jervisffb.test.utils.TeamRerollSelected
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Class testing the functionality of the [PlayerSpecialRule.TEAM_CAPTAIN] special rule.
 */
class TeamCaptainTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun mustSetupIfPossible() {
        super.setUp()
        val captain = awayTeam["A12".playerId]
        captain.extraSpecialRules.add(PlayerSpecialRule.TEAM_CAPTAIN)
        controller.rollForward(
            *defaultPregame(),
            *defaultHomeSetup(),
            *defaultAwaySetup(endSetup = true),
        )
        assertEquals(SetupTeam.InformOfInvalidSetup, controller.currentNode())
        val brokenRule = rules.isSetupValid(state, awayTeam).filterIsInstance<TeamCaptainNotOnField>().first()
        assertEquals(1, brokenRule.availablePlayers.size)
        assertEquals("A12".playerId, brokenRule.availablePlayers.single())
        controller.rollForward(
            Confirm, //Dismiss invalid setup message
            PlayerSelected("A1".playerId),
            DogoutSelected,
            PlayerSelected("A12".playerId),
            FieldSquareSelected(13, 5),
            EndSetup
        )
        assertEquals(TheKickOff.NominateKickingPlayer, controller.currentNode())
    }

    @Test
    fun noSetupErrorIfNotAvailable() {
        super.setUp()
        awayTeam["A12".playerId].apply {
            extraSpecialRules.add(PlayerSpecialRule.TEAM_CAPTAIN)
            state = PlayerState.BADLY_HURT
        }
        controller.rollForward(
            *defaultPregame(),
            *defaultHomeSetup(),
            *defaultAwaySetup(endSetup = true),
        )
        assertEquals(TheKickOff.NominateKickingPlayer, controller.currentNode())
    }

    @Test
    fun gainsFreeProSkill() {
        // Right now we assume that whoever creates the team will ensure this
        // rule is followed and the Player has Pro, but it is not enforced
        // by Jervis.
        val player = awayTeam["A1".playerId]
        player.apply {
            extraSpecialRules.add(PlayerSpecialRule.TEAM_CAPTAIN)
            addSkill(SkillType.PRO)
        }
        assertTrue(player.specialRules.contains(PlayerSpecialRule.TEAM_CAPTAIN))
        assertEquals(1, player.skills.count { it.type == SkillType.PRO })
    }

    @Test
    fun saveTeamRerollIfOnPitch() {
        val player = awayTeam["A1".playerId].apply {
            extraSpecialRules.add(PlayerSpecialRule.TEAM_CAPTAIN)
            addSkill(SkillType.PRO)
        }
        assertTrue(awayTeam.rerolls.none { it.rerollUsed })
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            2.d6, // Fail dodge
            TeamRerollSelected<RegularTeamReroll>(),
            *teamCaptainRoll(6.d6),
            3.d6, // Succeed dodge,
            EndAction
        )
        assertTrue(awayTeam.rerolls.none { it.rerollUsed })
    }

    @Test
    fun failRollToSaveReroll() {
        val player = awayTeam["A1".playerId].apply {
            extraSpecialRules.add(PlayerSpecialRule.TEAM_CAPTAIN)
            addSkill(SkillType.PRO)
        }
        assertTrue(awayTeam.rerolls.none { it.rerollUsed })
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            2.d6, // Fail dodge
            TeamRerollSelected<RegularTeamReroll>(),
            *teamCaptainRoll(5.d6),
            3.d6, // Succeed dodge,
            EndAction
        )
        assertEquals(1, awayTeam.rerolls.count { it.rerollUsed })
    }

    @Test
    fun cannotSaveRerollIfNotOnPitch() {
        val captain = awayTeam["A12".playerId].apply {
            extraSpecialRules.add(PlayerSpecialRule.TEAM_CAPTAIN)
            addSkill(SkillType.PRO)
        }
        assertEquals(DogOut, captain.location)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            2.d6, // Fail dodge
            TeamRerollSelected<RegularTeamReroll>(),
            3.d6, // Succeed dodge,
            EndAction
        )
        assertEquals(1, awayTeam.rerolls.count { it.rerollUsed })
    }

    @Ignore
    @Test
    fun saveLeaderRoll() {
        TODO("Waiting for Leader support")
    }
}
