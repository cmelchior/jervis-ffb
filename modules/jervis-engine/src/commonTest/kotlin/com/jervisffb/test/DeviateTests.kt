package com.jervisffb.test

import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for "deviate" as described on page 25 in the rulebook.
 */
class DeviateTests: JervisGameTest() {

    @Test
    fun moveInRandomDirection() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(17, 7),
                deviate = DiceRollResults(4.d8, 1.d6), // Deviate so lands on player
                bounce = null
            ),
            6.d6, // Catch Landing
            PlayerSelected("A10".playerId),
            PlayerActionSelected(PlayerStandardActionType.PASS),
            Confirm, // Start Pass section
            FieldSquareSelected(21, 7), // Throw Short Pass to empty square
            2.d6, // Wildly Inaccurate Pass
            NoRerollSelected(), // No Reroll
        )

        // Check that we need to roll a random D8 + D6 to deviate
        assertEquals(BallState.DEVIATING, state.currentBall().state)
        assertEquals(1, controller.getAvailableActions().size)
        controller.rollForward(
            DiceRollResults(5.d8, 5.d6), // Deviate ball in a random direction
            2.d8 // Bounce
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
    }

    @Test
    fun mustCatchIfPlayerIsInLandingSquare() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(17, 7),
                deviate = DiceRollResults(4.d8, 1.d6), // Deviate so lands on player
                bounce = null
            ),
            6.d6, // Catch Landing
            PlayerSelected("A10".playerId),
            PlayerActionSelected(PlayerStandardActionType.PASS),
            Confirm, // Start Pass section
            FieldSquareSelected(22, 7), // Throw Short Pass to empty square
            2.d6, // Wildly Inaccurate Pass
            NoRerollSelected(), // No Reroll
        )

        // Check that player in landing field must attempt to catch the ball
        assertEquals(BallState.DEVIATING, state.currentBall().state)
        controller.rollForward(
            DiceRollResults(5.d8, 6.d6), // Deviate roll
            6.d6, // Catch roll
            NoRerollSelected(),
        )
        assertTrue(awayTeam["A11".playerId].hasBall())
    }

    @Test
    fun bounceIfPlayerInLandingSquareCannotCatch() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(17, 7),
                deviate = DiceRollResults(4.d8, 1.d6), // Deviate so lands on player
                bounce = null
            ),
            6.d6, // Catch Landing
            PlayerSelected("A10".playerId),
            PlayerActionSelected(PlayerStandardActionType.PASS),
            Confirm, // Start Pass section
            FieldSquareSelected(22, 7), // Throw Short Pass to empty square
        )
        awayTeam["A11".playerId].hasTackleZones = false
        controller.rollForward(
            2.d6, // Wildly Inaccurate Pass
            NoRerollSelected(), // No Reroll
        )
        // Check that ball bounce if landing on a player who cannot catch
        assertEquals(BallState.DEVIATING, state.currentBall().state)
        controller.rollForward(
            DiceRollResults(5.d8, 6.d6), // Deviate roll
            8.d8, // Bounce
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        assertEquals(FieldCoordinate(23,8), state.singleBall().location)
    }

    @Test
    fun bounceIfLandingInEmptySquare() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(17, 7),
                deviate = DiceRollResults(4.d8, 1.d6), // Deviate so lands on player
                bounce = null
            ),
            6.d6, // Catch Landing
            PlayerSelected("A10".playerId),
            PlayerActionSelected(PlayerStandardActionType.PASS),
            Confirm, // Start Pass section
            FieldSquareSelected(21, 7), // Throw Short Pass to empty square
            2.d6, // Wildly Inaccurate Pass
            NoRerollSelected(), // No Reroll
        )

        // Check that a ball will bounce after landing in an empty square
        assertEquals(BallState.DEVIATING, state.currentBall().state)
        controller.rollForward(
            DiceRollResults(5.d8, 5.d6), // Deviate ball in a random direction
        )
        assertEquals(BallState.BOUNCING, state.singleBall().state)
        controller.rollForward(
            3.d8 // Bounce
        )
    }
}
