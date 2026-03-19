package com.jervisffb.test.bb2025

import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.EndTurn
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.PassTypeSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.commands.SetBallLocation
import com.jervisffb.engine.ext.d3
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.common.actions.PassType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.SmartMoveTo
import com.jervisffb.test.catch
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
 * Tests for Throw-in as described on page 73 in the BB2025 rulebook.
 */
class ThrowInTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun throwInWhenLeavingField() {
        // Check that leaving the field triggers a throw-in. All other
        // tests in this class will just do this manually.
        // Starting square (0) is the square it had when leaving the field
        controller.rollForward(
            PlayerSelected("A10".playerId),
            PlayerActionSelected(PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(6.d6),
            PassTypeSelected(PassType.STANDARD),
            FieldSquareSelected(25, 0), // Throw into the corner
            *throwBall(6.d6),
            3.d8, // Bounce outside the field
        )
        assertEquals(BallState.OUT_OF_BOUNDS, state.currentBall().state)
        controller.rollForward(
            2.d3, // Roll throw-in direction
        )
        assertEquals(BallState.THROW_IN, state.currentBall().state)
        controller.rollForward(
            DiceRollResults(1.d6, 3.d6) // Distance
        )
        assertEquals(BallState.BOUNCING, state.currentBall().state)
        controller.rollForward(
            3.d8, // Bounce
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        assertEquals(FieldCoordinate(23, 2), state.singleBall().coordinates)
    }

    @Test
    fun throwInFromTopBorder() {
        leaveFieldAt(13, 0)
        controller.rollForward(
            1.d3, // Direction
            DiceRollResults(4.d6, 2.d6), // Distance to [18,5]
            8.d8 // Bounce to [19, 6]
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        state.singleBall().assertCoordinates(19, 6)
    }

    @Test
    fun throwInFromBottomBorder() {
        leaveFieldAt(13, 14)
        controller.rollForward(
            3.d3, // Direction
            DiceRollResults(4.d6, 2.d6), // Distance to [18,9]
            3.d8 // Bounce to [19,8]
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        assertEquals(FieldCoordinate(19, 8), state.singleBall().coordinates)
    }

    @Test
    fun throwInFromLeftBorder() {
        leaveFieldAt(0, 7)
        controller.rollForward(
            3.d3, // Direction
            DiceRollResults(1.d6, 3.d6), // Distance to [3,10]
            8.d8 // Bounce to [4, 11]
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        state.singleBall().assertCoordinates(4, 11)
    }

    @Test
    fun throwInFromRightBorder() {
        leaveFieldAt(25, 7)
        controller.rollForward(
            3.d3, // Direction
            DiceRollResults(1.d6, 1.d6), // Distance to [24,6]
            1.d8 // Bounce to [23,5]
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        state.singleBall().assertCoordinates(23, 5)
    }

    @Test
    fun throwInFromTopLeftCorner() {
        leaveFieldAt(0, 0)
        controller.rollForward(
            3.d3, // Direction
            DiceRollResults(1.d6, 5.d6), // Distance to [0,5]
            7.d8 // Bounce to [0,6]
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        state.singleBall().assertCoordinates(0, 6)
    }

    @Test
    fun throwInFromTopRightCorner() {
        leaveFieldAt(25, 0)
        controller.rollForward(
            1.d3, // Direction
            DiceRollResults(1.d6, 5.d6), // Distance to [25,5]
            7.d8 // Bounce to [25,6]
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        state.singleBall().assertCoordinates(25, 6)
    }

    @Test
    fun throwInFromBottomLeftCorner() {
        leaveFieldAt(0, 14)
        controller.rollForward(
            1.d3, // Direction
            DiceRollResults(1.d6, 5.d6), // Distance to [0,9]
            2.d8 // Bounce to [0,8]
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        state.singleBall().assertCoordinates(0, 8)
    }

    @Test
    fun throwInFromBottomRightCorner() {
        leaveFieldAt(25, 14)
        controller.rollForward(
            1.d3, // Direction
            DiceRollResults(1.d6, 1.d6), // Distance to [24,13]
            2.d8 // Bounce to [24,13]
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        state.singleBall().assertCoordinates(24, 13)
    }

    @Test
    fun playerInLandingSquareMustCatchIfPossible() {
        leaveFieldAt(25, 1)
        controller.rollForward(
            2.d3, // Direction
            DiceRollResults(5.d6, 6.d6), // Distance
        )
        assertEquals(BallState.THROW_IN, state.singleBall().state)
        controller.rollForward(
            *catch(6.d6) // Must catch
        )
        assertTrue(awayTeam["A7".playerId].hasBall())
    }

    @Test
    fun bounceFromLandingSquareIfPlayerCannotCatch() {
        leaveFieldAt(25, 1)
        awayTeam["A7".playerId].state = PlayerState.PRONE
        controller.rollForward(
            2.d3, // Direction
            DiceRollResults(5.d6, 6.d6), // Distance
            2.d8, // Bounce
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        state.singleBall().assertCoordinates(15, 0)
    }

    @Test
    fun bounceFromLandingSquareIfEmpty() {
        leaveFieldAt(25, 1)
        controller.rollForward(
            2.d3, // Direction
            DiceRollResults(2.d6, 3.d6), // Distance
        )
        assertEquals(BallState.BOUNCING, state.singleBall().state)
        controller.rollForward(
            2.d8, // Bounce
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        state.singleBall().assertCoordinates(21, 0)
    }

    @Test
    fun repeatThrowInUntilLandingOnField() {
        leaveFieldAt(25, 4)
        controller.rollForward(
            3.d3, // Direction
            DiceRollResults(6.d6, 6.d6), // Distance
            1.d3, // 2nd throw-in direction
            DiceRollResults(5.d6, 6.d6),
            2.d3, // 3rd throw-in direction
            DiceRollResults(3.d6, 2.d6),
            2.d8, // Bounce
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        state.singleBall().assertCoordinates(21, 3)
    }

    // Manipulate game flow so the ball leaves the field after bouncing from a throw.
    // This method assumes the exit field is empty.
    private fun leaveFieldAt(x: Int, y: Int) {
        val bounceDirection = when {
            y == 0 -> 2.d8
            y == rules.fieldHeight - 1 -> 7.d8
            x == 0 -> 4.d8
            x == rules.fieldWidth - 1 -> 5.d8
            else -> error("Unsupported coordinate: ($x, $y)")
        }

        // For this we need to use the home team since they can reach the ball
        if (x == 0) {
            SetBallLocation(state.singleBall(), FieldCoordinate(x, y)).execute(state)
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
                FieldSquareSelected(x, y), // Throw into the corner
                *throwBall(6.d6),
                bounceDirection,
            )
        }
    }
}
