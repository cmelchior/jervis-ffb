package com.jervisffb.test.bb2020

import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.PassTypeSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.rules.common.actions.PassType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.test.JervisGameBB2020Test
import com.jervisffb.test.catch
import com.jervisffb.test.defaultKickOffHomeTeam
import com.jervisffb.test.defaultPregame
import com.jervisffb.test.defaultSetup
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.throwBall
import com.jervisffb.test.utils.assertCoordinates
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for "deviate" as described on page 25 in the rulebook.
 */
class DeviateTests: JervisGameBB2020Test() {

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
            PassTypeSelected(PassType.STANDARD),
            FieldSquareSelected(21, 7), // Throw Short Pass to empty square
            *throwBall(2.d6), // Wildly Inaccurate Pass
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
            PassTypeSelected(PassType.STANDARD),
            FieldSquareSelected(22, 7), // Throw Short Pass to empty square
            *throwBall(2.d6), // Wildly Inaccurate Pass
        )

        // Check that player in landing field must attempt to catch the ball
        assertEquals(BallState.DEVIATING, state.currentBall().state)
        controller.rollForward(
            DiceRollResults(5.d8, 6.d6), // Deviate roll
            *catch(6.d6)
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
            PassTypeSelected(PassType.STANDARD),
            FieldSquareSelected(22, 7), // Throw Short Pass to empty square
        )
        awayTeam["A11".playerId].hasTackleZones = false
        controller.rollForward(
            *throwBall(2.d6), // Wildly Inaccurate Pass
        )
        // Check that ball bounce if landing on a player who cannot catch
        assertEquals(BallState.DEVIATING, state.currentBall().state)
        controller.rollForward(
            DiceRollResults(5.d8, 6.d6), // Deviate roll
            8.d8, // Bounce
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        state.singleBall().assertCoordinates(23, 8)
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
            PassTypeSelected(PassType.STANDARD),
            FieldSquareSelected(21, 7), // Throw Short Pass to empty square
            *throwBall(2.d6), // Wildly Inaccurate Pass
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
