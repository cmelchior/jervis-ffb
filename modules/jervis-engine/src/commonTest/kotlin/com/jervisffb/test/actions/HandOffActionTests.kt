package com.jervisffb.test.actions

import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.Availability
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.PlayerStandardActionType
import com.jervisffb.engine.rules.bb2020.skills.RegularTeamReroll
import com.jervisffb.test.JervisGameTest
import com.jervisffb.test.SmartMoveTo
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.moveTo
import com.jervisffb.test.utils.SelectTeamReroll
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Class responsible for testing the Hand-off action as described on page
 * 51 in the rulebook.
 */
class HandOffActionTests: JervisGameTest() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun cancelBeforeMoveOrHandOffDoesNotUseAction() { val player = awayTeam["A10".playerId]
        assertEquals(1, state.awayTeam.turnData.handOffActions)
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.HAND_OFF),
            EndAction
        )
        assertEquals(1, state.awayTeam.turnData.handOffActions)
        assertEquals(Availability.AVAILABLE, awayTeam["A10".playerId].available)
    }

    @Test
    fun moveWithoutBallUsesAction() {
        assertEquals(1, state.awayTeam.turnData.handOffActions)
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.HAND_OFF),
            *moveTo(17, 6),
            EndAction
        )
        assertEquals(0, state.awayTeam.turnData.handOffActions)
    }

    @Test
    fun canStartActionWithoutBall() {
        val player = awayTeam["A10".playerId]
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.HAND_OFF),
        )
        assertEquals(player, state.activePlayer)
        assertFalse(player.hasBall())
    }

    @Test
    fun actionEndsAfterHandOff() {
        val player = awayTeam["A10".playerId]
        assertEquals(1, state.awayTeam.turnData.handOffActions)
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.HAND_OFF),
        )
        assertEquals(player, state.activePlayer)
        assertFalse(player.hasBall())
        controller.rollForward(
            *moveTo(17, 7),
            4.d6, // Pickup
            NoRerollSelected(),
            SmartMoveTo(15, 2),
            PlayerSelected("A7".playerId), // Target of hand-off
            6.d6, // Catch
            NoRerollSelected(),
        )
        assertTrue(awayTeam["A7".playerId].hasBall())
        assertNull(state.activePlayer)
        assertEquals(0, state.awayTeam.turnData.handOffActions)
    }

    @Test
    fun canHandOffAfterAllMovesAreUsed() {
        val player = awayTeam["A10".playerId]
        assertEquals(1, state.awayTeam.turnData.handOffActions)
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.HAND_OFF),
        )
        assertEquals(player, state.activePlayer)
        assertFalse(player.hasBall())
        controller.rollForward(
            *moveTo(17, 7),
            4.d6, // Pickup
            NoRerollSelected(),
            SmartMoveTo(15, 2),
            *moveTo(15, 3),
            *moveTo(16, 3),
            2.d6, // Rush
            NoRerollSelected(),
            *moveTo(15, 2),
            2.d6, // Rush
            NoRerollSelected(),
        )
        // Hand-off and end action after having used up all available moves
        assertEquals(0, player.movesLeft)
        assertEquals(0, player.rushesLeft)
        assertEquals(player, state.activePlayer)
        controller.rollForward(
            PlayerSelected("A7".playerId), // Target of hand-off
            6.d6, // Catch
            NoRerollSelected(),
        )
        assertTrue(awayTeam["A7".playerId].hasBall())
        assertNull(state.activePlayer)
    }

    @Test
    fun playerWithZeroPassingCanStillHandOff() {
        val player = awayTeam["A10".playerId]
        player.passing = null // Set passing to "-"
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.HAND_OFF),
            *moveTo(17, 7),
            4.d6, // Pickup
            NoRerollSelected(),
            SmartMoveTo(15, 2),
            PlayerSelected("A7".playerId), // Target of hand-off
            6.d6, // Catch
            NoRerollSelected(),
        )
        assertTrue(awayTeam["A7".playerId].hasBall())
    }

    @Test
    fun catchHandOff() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.HAND_OFF),
            *moveTo(17, 7),
            4.d6, // Pickup
            NoRerollSelected(),
            SmartMoveTo(14, 7),
            PlayerSelected("A3".playerId), // Target of hand-off
            5.d6, // Fail catch (3 marks)
            SelectTeamReroll<RegularTeamReroll>(),
            6.d6 // Catch it
        )
        assertTrue(awayTeam["A3".playerId].hasBall())
    }

    @Test
    fun bounceFromTargetIfCatchFailed() {
        val player = awayTeam["A10".playerId]
        player.passing = null // Set passing to "-"
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.HAND_OFF),
            *moveTo(17, 7),
            4.d6, // Pickup
            NoRerollSelected(),
            SmartMoveTo(15, 2),
            PlayerSelected("A7".playerId), // Target of hand-off
            2.d6, // Fail catch
            NoRerollSelected(),
            2.d8 // Bounce
        )
        assertFalse(awayTeam["A7".playerId].hasBall())
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        assertEquals(FieldCoordinate(15, 0), state.singleBall().location)
    }

    @Test
    fun doesNotRequireMove() {
        // Fake state by giving ball to player manually
        SetBallState.carried(state.singleBall(), awayTeam["A6".playerId]).execute(state)
        controller.rollForward(
            *activatePlayer("A6", PlayerStandardActionType.HAND_OFF),
            PlayerSelected("A7".playerId),
            6.d6, // Catch
            NoRerollSelected(),
        )
        assertEquals(0, awayTeam.turnData.handOffActions)
        assertNull(state.activePlayer)
    }
}
