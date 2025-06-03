package com.jervisffb.test

import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.PlayerStandardActionType
import com.jervisffb.test.ext.rollForward
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for "bounce" as described on page 25 in the rulebook.
 */
class BounceTests: JervisGameTest() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame() //  Ball landed at [17,7]
    }
    @Test
    fun moveOneSquare() {
        controller.rollForward(
            PlayerSelected("A10".playerId),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            *moveTo(17, 7), // Move into the ball
            1.d6, // Fail pickup
            NoRerollSelected(),
            1.d8 // Bounce
        )
        // Just check a single direction
        assertEquals(FieldCoordinate(16,6), state.singleBall().location)
    }

    @Test
    fun mustCatchIfPlayerIsInSquare() {
        controller.rollForward(
            PlayerSelected("A10".playerId),
            PlayerActionSelected(PlayerStandardActionType.PASS),
            *moveTo(17, 7), // Move into the ball
            6.d6, // Pickup ball
            NoRerollSelected(),
            Confirm, // Start pass
            FieldSquareSelected(15, 1), // Target player
            6.d6, // Throw ball
            NoRerollSelected(),
            1.d6, // Fail to catch
            NoRerollSelected(),
            4.d8, // Bounce to another player
        )
        val player = awayTeam["A6".playerId]
        assertTrue(rules.canCatch(state, player))
        controller.rollForward(
            6.d6, // Forced to roll for catch
            NoRerollSelected(),
        )
        assertTrue(player.hasBall())
    }

    // Will bounce again if player in square has lost their tackle zone, is prone or stunned
    // Will continue to do so
    @Test
    fun bounceAgainIfPlayerCannotCatch() {
        controller.rollForward(
            PlayerSelected("A10".playerId),
            PlayerActionSelected(PlayerStandardActionType.PASS),
            *moveTo(17, 7), // Move into the ball
            6.d6, // Pickup ball
            NoRerollSelected(),
            Confirm, // Start pass
            FieldSquareSelected(15, 1), // Target player
            6.d6, // Throw ball
            NoRerollSelected(),
            1.d6, // Fail to catch
            NoRerollSelected(),
        )
        val player = awayTeam["A6".playerId]
        player.hasTackleZones = false
        assertFalse(rules.canCatch(state, player))
        controller.rollForward(
            4.d8, // Bounce to another player
            5.d8, // Player cannot catch, so bounce back to original player
            6.d6, // Target player catches it
            NoRerollSelected(),
        )
        assertTrue(awayTeam["A7".playerId].hasBall())
    }

    @Test
    fun keepBouncingUntilComesToRest() {
        controller.rollForward(
            PlayerSelected("A10".playerId),
            PlayerActionSelected(PlayerStandardActionType.PASS),
            *moveTo(17, 7), // Move into the ball
            6.d6, // Pickup ball
            NoRerollSelected(),
            Confirm, // Start pass
            FieldSquareSelected(15, 1), // Target player
            6.d6, // Throw ball to A7
            NoRerollSelected(),
            1.d6, // Fail to catch
            NoRerollSelected(),
            4.d8, // Bounce to A6
            2.d6, // Fail to catch
            NoRerollSelected(),
            5.d8, // Bounce back to A7
            3.d6, // Fail to catch
            NoRerollSelected(),
            4.d8, // Bounce back to A6
            1.d6, // Fail to catch
            NoRerollSelected(),
            4.d8, // Bounce to [13, 1]
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state,)
    }
}
