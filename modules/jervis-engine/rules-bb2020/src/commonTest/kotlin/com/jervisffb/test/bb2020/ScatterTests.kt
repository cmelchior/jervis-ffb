package com.jervisffb.test.bb2020

import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.PitchSquareSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.test.JervisGameBB2020Test
import com.jervisffb.test.defaultKickOffHomeTeam
import com.jervisffb.test.defaultPregame
import com.jervisffb.test.defaultSetup
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.utils.assertCoordinates
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for "scatter" as described on page 25 in the rulebook.
 */
class ScatterTests: JervisGameBB2020Test() {

    @Test
    fun moveInRandomDirection3Times() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = PitchSquareSelected(17, 7),
                deviate = DiceRollResults(4.d8, 1.d6), // Deviate so lands on player
                kickoffEvent = arrayOf(
                    DiceRollResults(4.d6, 4.d6), // Weather change (to trigger scatter)
                    DiceRollResults(3.d6, 4.d6), // Roll Perfect Conditions
                ),
                bounce = null
            ),
        )
        // Check that we need to roll a random D8 3 times
        assertEquals(BallState.SCATTERED, state.currentBall().state)
        assertEquals(1, controller.getAvailableActions().size)
        controller.rollForward(
            DiceRollResults(2.d8, 1.d8, 7.d8), // Scatter due to weather
            2.d8 // Bounce
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        state.singleBall().assertCoordinates(15, 5)
    }

    @Test
    fun mustCatchIfPlayerIsInLandingSquare() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = PitchSquareSelected(17, 7),
                deviate = DiceRollResults(4.d8, 1.d6), // Deviate so lands on player
                kickoffEvent = arrayOf(
                    DiceRollResults(4.d6, 4.d6), // Weather change (to trigger scatter)
                    DiceRollResults(3.d6, 4.d6), // Roll Perfect Conditions
                    DiceRollResults(2.d8, 4.d8, 8.d8), // Scatter to square with player
                ),
                bounce = null
            ),
        )
        val player = awayTeam["A10".playerId]
        assertTrue(rules.canCatch(player))
        controller.rollForward(
            6.d6 // Catch
        )
        assertTrue(player.hasBall())
    }

    @Test
    fun bounceIfPlayerInLandingSquareCannotCatch() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = PitchSquareSelected(17, 7),
                deviate = DiceRollResults(4.d8, 1.d6), // Deviate so lands on player
                kickoffEvent = arrayOf(
                    DiceRollResults(4.d6, 4.d6), // Weather change (to trigger scatter)
                    DiceRollResults(3.d6, 4.d6), // Roll Perfect Conditions
                ),
                bounce = null
            ),
        )
        val player = awayTeam["A10".playerId]
        player.hasTackleZones = false
        assertFalse(rules.canCatch(player))
        controller.rollForward(
            DiceRollResults(2.d8, 4.d8, 8.d8), // Scatter to square with player
            7.d8 // Bounce
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        assertEquals(PitchCoordinate(16, 8), state.singleBall().coordinates)
    }

    @Test
    fun bounceIfLandingInEmptySquare() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = PitchSquareSelected(17, 7),
                deviate = DiceRollResults(4.d8, 1.d6), // Deviate so lands on player
                kickoffEvent = arrayOf(
                    DiceRollResults(4.d6, 4.d6), // Weather change (to trigger scatter)
                    DiceRollResults(3.d6, 4.d6), // Roll Perfect Conditions
                ),
                bounce = null
            ),
        )
        // Check that we need to roll a random D8 3 times
        assertEquals(BallState.SCATTERED, state.currentBall().state)
        assertEquals(1, controller.getAvailableActions().size)
        controller.rollForward(
            DiceRollResults(2.d8, 1.d8, 7.d8), // Scatter due to weather
        )
        assertEquals(BallState.BOUNCING, state.currentBall().state)
        controller.rollForward(
            2.d8 // Bounce
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        state.singleBall().assertCoordinates(15, 5)
    }
}
