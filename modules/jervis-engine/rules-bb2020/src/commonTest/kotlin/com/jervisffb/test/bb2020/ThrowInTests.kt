package com.jervisffb.test.bb2020

import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.EndTurn
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PassTypeSelected
import com.jervisffb.engine.actions.PitchSquareSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.commands.SetBallLocation
import com.jervisffb.engine.ext.d3
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.PlayerPitchState
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.common.actions.PassType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.test.JervisGameBB2020Test
import com.jervisffb.test.SmartMoveTo
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.moveTo
import com.jervisffb.test.pickup
import com.jervisffb.test.throwBall
import com.jervisffb.test.utils.assertCoordinates
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for Throw-in as described on page 51 in the BB2020 rulebook.
 */
class ThrowInTests: JervisGameBB2020Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun throwInWhenLeavingPitch() {
        // Check that leaving the field triggers a throw-in. All other
        // tests in this class will just do this manually.
        // Starting square (0) is the square it had when leaving the field
        controller.rollForward(
            PlayerSelected("A10".playerId),
            PlayerActionSelected(PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(6.d6),
            PassTypeSelected(PassType.STANDARD),
            PitchSquareSelected(25, 0), // Throw into the corner
            *throwBall(6.d6),
            3.d8, // Bounce outside the field
        )
        assertEquals(BallState.OUT_OF_BOUNDS, state.currentBall().state)
        controller.rollForward(
            2.d3, // Roll throw-in direction
        )
        assertEquals(BallState.THROW_IN, state.currentBall().state)
        controller.rollForward(
            DiceRollResults(1.d6, 2.d6) // Distance
        )
        assertEquals(BallState.BOUNCING, state.currentBall().state)
        controller.rollForward(
            3.d8, // Bounce
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        assertEquals(PitchCoordinate(23, 2), state.singleBall().coordinates)
    }

    @Test
    fun throwInFromTopBorder() {
        leavePitchAt(13, 0)
        controller.rollForward(
            1.d3, // Direction
            DiceRollResults(4.d6, 2.d6), // Distance to [19,6]
            8.d8 // Bounce to [20,7]
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        state.singleBall().assertCoordinates(20, 7)
    }

    @Test
    fun throwInFromBottomBorder() {
        leavePitchAt(13, 14)
        controller.rollForward(
            3.d3, // Direction
            DiceRollResults(4.d6, 2.d6), // Distance to [19,8]
            3.d8 // Bounce to [20,7]
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        state.singleBall().assertCoordinates(20, 7)
    }

    @Test
    fun throwInFromLeftBorder() {
        // Move the ball and let the home team try to pick it up after which it will bounce
        leavePitchAt(0, 7)
        controller.rollForward(
            3.d3, // Direction
            DiceRollResults(1.d6, 3.d6), // Distance to [19,8]
            8.d8 // Bounce to [20,7]
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        state.singleBall().assertCoordinates(5, 12)
    }

    @Test
    fun throwInFromRightBorder() {
        leavePitchAt(25, 7)
        controller.rollForward(
            3.d3, // Direction
            DiceRollResults(1.d6, 1.d6), // Distance to [23,5]
            1.d8 // Bounce to [22,4]
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        state.singleBall().assertCoordinates(22, 4)
    }

    @Test
    fun throwInFromTopLeftCorner() {
        leavePitchAt(0, 0)
        controller.rollForward(
            3.d3, // Direction
            DiceRollResults(1.d6, 5.d6), // Distance to [0,6]
            7.d8 // Bounce to [0,7]
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        state.singleBall().assertCoordinates(0, 7)
    }

    @Test
    fun throwInFromTopRightCorner() {
        leavePitchAt(25, 0)
        controller.rollForward(
            1.d3, // Direction
            DiceRollResults(1.d6, 5.d6), // Distance to [25,6]
            7.d8 // Bounce to [25,7]
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        state.singleBall().assertCoordinates(25, 7)
    }

    @Test
    fun throwInFromBottomLeftCorner() {
        leavePitchAt(0, 14)
        controller.rollForward(
            1.d3, // Direction
            DiceRollResults(1.d6, 5.d6), // Distance to [0,6]
            2.d8 // Bounce to [0,7]
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        state.singleBall().assertCoordinates(0, 7)
    }

    @Test
    fun throwInFromBottomRightCorner() {
        leavePitchAt(25, 14)
        controller.rollForward(
            1.d3, // Direction
            DiceRollResults(1.d6, 1.d6), // Distance to [23,14]
            2.d8 // Bounce to [23,13]
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        state.singleBall().assertCoordinates(23, 13)
    }

    @Test
    fun playerInLandingSquareMustCatchIfPossible() {
        leavePitchAt(25, 1)
        controller.rollForward(
            2.d3, // Direction
            DiceRollResults(5.d6, 5.d6), // Distance
        )
        assertEquals(BallState.THROW_IN, state.singleBall().state)
        controller.rollForward(
            6.d6, // Must catch
            NoRerollSelected()
        )
        assertTrue(awayTeam["A7".playerId].hasBall())
    }

    @Test
    fun bounceFromLandingSquareIfPlayerCannotCatch() {
        leavePitchAt(25, 1)
        awayTeam["A7".playerId].state = PlayerPitchState.PRONE
        controller.rollForward(
            2.d3, // Direction
            DiceRollResults(5.d6, 5.d6), // Distance
            2.d8, // Bounce
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        state.singleBall().assertCoordinates(15, 0)
    }

    @Test
    fun bounceFromLandingSquareIfEmpty() {
        leavePitchAt(25, 1)
        controller.rollForward(
            2.d3, // Direction
            DiceRollResults(2.d6, 2.d6), // Distance
        )
        assertEquals(BallState.BOUNCING, state.singleBall().state)
        controller.rollForward(
            2.d8, // Bounce
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        state.singleBall().assertCoordinates(21, 0)
    }

    @Test
    fun repeatThrowInUntilLandingOnPitch() {
        leavePitchAt(25, 4)
        controller.rollForward(
            3.d3, // Direction
            DiceRollResults(6.d6, 6.d6), // Distance
            1.d3, // 2nd throw-in direction
            DiceRollResults(5.d6, 5.d6),
            2.d3, // 3rd throw-in direction
            DiceRollResults(2.d6, 2.d6),
            2.d8,
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        state.singleBall().assertCoordinates(21, 3)
    }

    // Manipulate game flow so the ball leaves the field after bouncing from a throw.
    // This method assumes the exit field is empty.
    private fun leavePitchAt(x: Int, y: Int) {
        val bounceDirection = when {
            y == 0 -> 2.d8
            y == rules.pitchHeight - 1 -> 7.d8
            x == 0 -> 4.d8
            x == rules.pitchWidth - 1 -> 5.d8
            else -> error("Unsupported coordinate: ($x, $y)")
        }

        // For this we need to use the home team since they can reach the ball
        if (x == 0) {
            SetBallLocation(state.singleBall(), PitchCoordinate(x, y)).execute(state)
            controller.rollForward(
                EndTurn,
                PlayerSelected("H11".playerId),
                PlayerActionSelected(PlayerStandardActionType.MOVE),
                SmartMoveTo(x, y),
                *pickup(1.d6), // Fail pickup
                1.d8, // Bounce out of field
            )
        } else {
            controller.rollForward(
                PlayerSelected("A10".playerId),
                PlayerActionSelected(PlayerStandardActionType.PASS),
                *moveTo(17, 7),
                *pickup(6.d6),
                PassTypeSelected(PassType.STANDARD),
                PitchSquareSelected(x, y), // Throw into the corner
                *throwBall(6.d6),
                bounceDirection,
            )
        }
    }
}
