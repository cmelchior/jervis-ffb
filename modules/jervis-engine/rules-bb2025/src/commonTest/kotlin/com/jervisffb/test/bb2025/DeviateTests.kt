package com.jervisffb.test.bb2025

import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.PitchSquareSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.context.CatchContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.defaultKickOffEvent
import com.jervisffb.test.defaultPregame
import com.jervisffb.test.defaultSetup
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.utils.assertCoordinates
import com.jervisffb.test.utils.makeDistracted
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for "deviate" as described on page 34 in the BB2025 rulebook.
 *
 * In BB2025, the only way deviate roll is during kick-off. Jervis treats
 * this differently since the ball isn't coming down yet. So there is no known
 * way to test for [BallState.DEVIATING] right now.
 */
class DeviateTests: JervisGameBB2025Test() {

    @Test
    fun moveInRandomDirection() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            PlayerSelected(PlayerId("H10")), // Select Kicker
            PitchSquareSelected(19, 7), // Center of Away Half
        )
        // Check that we need to roll a random D8 + D6 to deviate
        assertEquals(BallState.IN_AIR, state.currentBall().state)
        assertEquals(1, controller.getAvailableActions().size)
        controller.rollForward(
            DiceRollResults(5.d8, 5.d6), // Deviate ball in a random direction
            *defaultKickOffEvent(),
            2.d8 // Bounce
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
    }

    @Test
    fun mustCatchIfPlayerIsInLandingSquare() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            PlayerSelected(PlayerId("H10")), // Select Kicker
            PitchSquareSelected(19, 7), // Center of Away Half
            DiceRollResults(5.d8, 3.d6),
            *defaultKickOffEvent(),
            6.d6 // Catch Landing
        )
        assertTrue(awayTeam["A11".playerId].hasBall())
    }

    @Test
    fun noNegativeModifierOnCatch() {
        val catcher = awayTeam["A11".playerId]
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            PlayerSelected(PlayerId("H10")), // Select Kicker
            PitchSquareSelected(19, 7), // Center of Away Half
            DiceRollResults(5.d8, 3.d6),
            *defaultKickOffEvent(),
        )
        assertEquals(3, catcher.agility)
        assertTrue(state.getContext<CatchContext>().modifiers.isEmpty())
        controller.rollForward(
            3.d6
        )
        assertTrue(catcher.hasBall())
    }

    @Test
    fun bounceIfPlayerInLandingSquareCannotCatch() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup()
        )
        awayTeam["A11".playerId].makeDistracted()
        controller.rollForward(
            PlayerSelected(PlayerId("H10")), // Select Kicker
            PitchSquareSelected(19, 7), // Center of Away Half
            DiceRollResults(5.d8, 3.d6),
            *defaultKickOffEvent(),
            8.d8 // Bounce because A11 cannot catch
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        state.singleBall().assertCoordinates(23, 8)
    }

    @Test
    fun bounceIfLandingInEmptySquare() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            PlayerSelected(PlayerId("H10")), // Select Kicker
            PitchSquareSelected(19, 7), // Center of Away Half
            DiceRollResults(5.d8, 4.d6),
            *defaultKickOffEvent(),
        )
        assertEquals(BallState.BOUNCING, state.singleBall().state)
        controller.rollForward(
            5.d8 // Bounce
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        state.singleBall().assertCoordinates(24, 7)
    }
}
