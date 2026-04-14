package com.jervisffb.test.bb2025

import com.jervisffb.engine.actions.PassTypeSelected
import com.jervisffb.engine.actions.PitchSquareSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.rules.common.actions.PassType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.catch
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.moveTo
import com.jervisffb.test.pickup
import com.jervisffb.test.throwBall
import com.jervisffb.test.utils.assertCoordinates
import com.jervisffb.test.utils.makeDistracted
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for "bounce" as described on page 25 in the rulebook.
 */
class BounceTests: JervisGameBB2025Test() {

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
            *pickup(1.d6), // Fail pickup
        )
        assertEquals(1, controller.getAvailableActions().size)
        controller.rollForward(
            1.d8 // Bounce
        )
        // Just check a single direction
        state.singleBall().assertCoordinates(16, 6)
    }

    @Test
    fun mustCatchIfPlayerIsInSquare() {
        controller.rollForward(
            PlayerSelected("A10".playerId),
            PlayerActionSelected(PlayerStandardActionType.PASS),
            *moveTo(17, 7), // Move into the ball
            *pickup(6.d6),
            PassTypeSelected(PassType.STANDARD),
            PitchSquareSelected(15, 1), // Target player
            *throwBall(6.d6),
            *catch(1.d6), // Fail to catch
            4.d8, // Bounce to another player
        )
        val player = awayTeam["A6".playerId]
        assertTrue(rules.canCatch(player))
        controller.rollForward(
            *catch(6.d6), // Forced to roll for catch
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
            *pickup(6.d6),
            PassTypeSelected(PassType.STANDARD),
            PitchSquareSelected(15, 1), // Target player
            *throwBall(6.d6),
            *catch(1.d6), // Fail to catch
        )
        val player = awayTeam["A6".playerId]
        player.makeDistracted()
        assertFalse(rules.canCatch(player))
        controller.rollForward(
            4.d8, // Bounce to another player
            5.d8, // Player cannot catch, so bounce back to original player
            *catch(6.d6), // Target player catches it
        )
        assertTrue(awayTeam["A7".playerId].hasBall())
    }

    @Test
    fun keepBouncingUntilComesToRest() {
        controller.rollForward(
            PlayerSelected("A10".playerId),
            PlayerActionSelected(PlayerStandardActionType.PASS),
            *moveTo(17, 7), // Move into the ball
            *pickup(6.d6),
            PassTypeSelected(PassType.STANDARD),
            PitchSquareSelected(15, 1), // Target player
            *throwBall(6.d6), // Throw ball to A7
            *catch(1.d6), // Fail to catch
            4.d8, // Bounce to A6
            *catch(2.d6), // Fail to catch
            5.d8, // Bounce back to A7
            *catch(3.d6), // Fail to catch
            4.d8, // Bounce back to A6
            *catch(1.d6), // Fail to catch
            4.d8, // Bounce to [13, 1]
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state,)
    }
}
