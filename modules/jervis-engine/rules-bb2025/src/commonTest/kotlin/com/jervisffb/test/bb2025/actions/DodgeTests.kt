package com.jervisffb.test.bb2025.actions

import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.procedures.rerolls.StandardTeamReroll
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.SmartMoveTo
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.dodge
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.followUp
import com.jervisffb.test.moveTo
import com.jervisffb.test.standardBlock
import com.jervisffb.test.utils.TeamRerollSelected
import com.jervisffb.test.utils.assertCoordinates
import com.jervisffb.test.utils.assertFallenOver
import com.jervisffb.test.utils.assertKnockedDown
import com.jervisffb.test.utils.assertStanding
import com.jervisffb.test.utils.makeDistracted
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Test a player dodging as described on page 55 in the BB2025 Rulebook.
 *
 * Note, any skills that affect dodges are testedd in their own test class.
 * This class only tests the basic functionality.
 */
class DodgeTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun rollWhenMovingAwayFromMarkingPlayer() {
        controller.rollForward(
            PlayerSelected("A1".playerId),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
        )
        val player = awayTeam["A1".playerId]
        assertTrue(rules.isMarked(player))
        controller.rollForward(
            *moveTo(14, 5),
            *dodge(6.d6),
        )
        assertEquals(PitchCoordinate(14, 5), player.coordinates)
        player.assertStanding()
    }

    @Test
    fun noRollWhenMovingFromOpenToMarked() {
        controller.rollForward(
            PlayerSelected("A6".playerId),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
        )
        val player = awayTeam["A6".playerId]
        assertFalse(rules.isMarked(player))
        controller.rollForward(SmartMoveTo(13, 4))
        assertTrue(rules.isMarked(player))
        player.assertStanding()
    }

    @Test
    fun modifierPrMarkingPlayer() {
        controller.rollForward(
            PlayerSelected("A6".playerId),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            SmartMoveTo(12, 4)
        )
        val player = awayTeam["A6".playerId]
        assertTrue(rules.isMarked(player))
        assertEquals(1, rules.calculateMarks(state, awayTeam, player.coordinates))
        assertEquals(2, rules.calculateMarks(state, awayTeam, PitchCoordinate(11, 5)))
        controller.rollForward(
            *moveTo(11, 5),
            *dodge(4.d6), // Need 5+ to dodge
        )
        player.assertFallenOver()
    }

    @Test
    fun moveBeforeRoll() {
        controller.rollForward(
            PlayerSelected("A1".playerId),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            *moveTo(13, 4) // Move player first
        )
        val player = awayTeam["A1".playerId]
        assertEquals(PitchCoordinate(13, 4), player.coordinates)
        // Then roll for dodge
        controller.rollForward(
            *dodge(6.d6),
        )
        assertEquals(PitchCoordinate(13, 4), player.coordinates)
        player.assertStanding()
    }

    @Test
    fun fallOverInTargetSquareIfFailingRoll() {
        controller.rollForward(
            PlayerSelected("A1".playerId),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            *moveTo(12, 4),
            *dodge(1.d6), // Fail dodge
        )
        val player = awayTeam["A1".playerId]
        player.assertFallenOver()
        assertEquals(PitchCoordinate(12, 4), player.coordinates)
    }

    @Test
    fun rerollAvailable() {
        controller.rollForward(
            PlayerSelected("A1".playerId),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            *moveTo(12, 4),
            1.d6, // Fail dodge
            TeamRerollSelected<StandardTeamReroll>(),
            4.d6 // Succeed
        )
        val player = awayTeam["A1".playerId]
        player.assertStanding()
        assertEquals(PitchCoordinate(12, 4), player.coordinates)
    }

    @Test
    fun doesNotWorkAgainstStumbleIfDistracted() {
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        defender.apply {
            addSkill(SkillType.DODGE)
            makeDistracted()
        }
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 5.dblock),
            DirectionSelected(Direction.UP_LEFT),
            followUp(false),
        )
        attacker.assertCoordinates(13, 5)
        attacker.assertStanding()
        defender.assertCoordinates(11, 4)
        defender.assertKnockedDown()
    }
}
