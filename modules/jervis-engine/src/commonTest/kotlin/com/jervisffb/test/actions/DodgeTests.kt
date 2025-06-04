package com.jervisffb.test.actions

import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.PlayerStandardActionType
import com.jervisffb.engine.rules.bb2020.skills.RegularTeamReroll
import com.jervisffb.test.JervisGameTest
import com.jervisffb.test.SmartMoveTo
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.moveTo
import com.jervisffb.test.utils.SelectTeamReroll
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Test a player dodging as described on page 45 in the BB2020 Rulebook.
 *
 * Note, any skills that affect dodges are testing in their own test class.
 * This class only tests the basic functionality.
 */
class DodgeTests: JervisGameTest() {

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
            6.d6,
            NoRerollSelected()
        )
        assertEquals(FieldCoordinate(14, 5), player.coordinates)
        assertEquals(PlayerState.STANDING, player.state)
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
        assertEquals(PlayerState.STANDING, player.state)
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
        assertEquals(2, rules.calculateMarks(state, awayTeam, FieldCoordinate(11, 5)))
        controller.rollForward(
            *moveTo(11, 5),
            4.d6, // Need 5+ to dodge
            NoRerollSelected()
        )
        assertEquals(PlayerState.FALLEN_OVER, player.state)
    }

    @Test
    fun moveBeforeRoll() {
        controller.rollForward(
            PlayerSelected("A1".playerId),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            *moveTo(13, 4) // Move player first
        )
        val player = awayTeam["A1".playerId]
        assertEquals(FieldCoordinate(13, 4), player.coordinates)
        // Then roll for dodge
        controller.rollForward(
            6.d6,
            NoRerollSelected()
        )
        assertEquals(FieldCoordinate(13, 4), player.coordinates)
        assertEquals(PlayerState.STANDING, player.state)
    }

    @Test
    fun fallOverInTargetSquareIfFailingRoll() {
        controller.rollForward(
            PlayerSelected("A1".playerId),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            *moveTo(12, 4),
            1.d6, // Fail dodge
            NoRerollSelected()
        )
        val player = awayTeam["A1".playerId]
        assertEquals(PlayerState.FALLEN_OVER, player.state)
        assertEquals(FieldCoordinate(12, 4), player.coordinates)
    }

    @Test
    fun rerollAvailable() {
        controller.rollForward(
            PlayerSelected("A1".playerId),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            *moveTo(12, 4),
            1.d6, // Fail dodge
            SelectTeamReroll<RegularTeamReroll>(),
            4.d6 // Succeed
        )
        val player = awayTeam["A1".playerId]
        assertEquals(PlayerState.STANDING, player.state)
        assertEquals(FieldCoordinate(12, 4), player.coordinates)
    }
}
