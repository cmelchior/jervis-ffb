package com.jervisffb.test

import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.EndTurn
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.NoRerollSelected
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
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.test.ext.rollForward
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for Throw-in as described on page 51 in the rulebook.
 */
class ThrowInTests: JervisGameTest() {

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
            6.d6, // Pick-up
            NoRerollSelected(),
            Confirm, // Start Pass section
            FieldSquareSelected(25, 0), // Throw into the corner
            6.d6, // Throw ball
            NoRerollSelected(), // No Reroll
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
        assertEquals(FieldCoordinate(23, 2), state.singleBall().location)
    }

    @Test
    fun throwInFromTopBorder() {
        leaveFieldAt(13, 0)
        controller.rollForward(
            1.d3, // Direction
            DiceRollResults(4.d6, 2.d6), // Distance to [19,6]
            8.d8 // Bounce to [20,7]
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        assertEquals(FieldCoordinate(20,7), state.singleBall().location)
    }

    @Test
    fun throwInFromBottomBorder() {
        leaveFieldAt(13, 14)
        controller.rollForward(
            3.d3, // Direction
            DiceRollResults(4.d6, 2.d6), // Distance to [19,8]
            3.d8 // Bounce to [20,7]
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        assertEquals(FieldCoordinate(20,7), state.singleBall().location)
    }

    @Test
    fun throwInFromLeftBorder() {
        // Move the ball and let the home team try to pick it up after which it will bounce
        leaveFieldAt(0, 7)
        controller.rollForward(
            3.d3, // Direction
            DiceRollResults(1.d6, 3.d6), // Distance to [19,8]
            8.d8 // Bounce to [20,7]
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        assertEquals(FieldCoordinate(5,12), state.singleBall().location)
    }

    @Test
    fun throwInFromRightBorder() {
        leaveFieldAt(25, 7)
        controller.rollForward(
            3.d3, // Direction
            DiceRollResults(1.d6, 1.d6), // Distance to [23,5]
            1.d8 // Bounce to [22,4]
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        assertEquals(FieldCoordinate(22,4), state.singleBall().location)
    }

    @Test
    fun throwInFromTopLeftCorner() {
        leaveFieldAt(0, 0)
        controller.rollForward(
            3.d3, // Direction
            DiceRollResults(1.d6, 5.d6), // Distance to [0,6]
            7.d8 // Bounce to [0,7]
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        assertEquals(FieldCoordinate(0,7), state.singleBall().location)
    }

    @Test
    fun throwInFromTopRightCorner() {
        leaveFieldAt(25, 0)
        controller.rollForward(
            1.d3, // Direction
            DiceRollResults(1.d6, 5.d6), // Distance to [25,6]
            7.d8 // Bounce to [25,7]
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        assertEquals(FieldCoordinate(25,7), state.singleBall().location)
    }

    @Test
    fun throwInFromBottomLeftCorner() {
        leaveFieldAt(0, 14)
        controller.rollForward(
            1.d3, // Direction
            DiceRollResults(1.d6, 5.d6), // Distance to [0,6]
            2.d8 // Bounce to [0,7]
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        assertEquals(FieldCoordinate(0,7), state.singleBall().location)
    }

    @Test
    fun throwInFromBottomRightCorner() {
        leaveFieldAt(25, 14)
        controller.rollForward(
            1.d3, // Direction
            DiceRollResults(1.d6, 1.d6), // Distance to [23,14]
            2.d8 // Bounce to [23,13]
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        assertEquals(FieldCoordinate(23,13), state.singleBall().location)
    }

    @Test
    fun playerInLandingSquareMustCatchIfPossible() {
        leaveFieldAt(25, 1)
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
        leaveFieldAt(25, 1)
        awayTeam["A7".playerId].state = PlayerState.PRONE
        controller.rollForward(
            2.d3, // Direction
            DiceRollResults(5.d6, 5.d6), // Distance
            2.d8, // Bounce
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        assertEquals(FieldCoordinate(15,0), state.singleBall().location)
    }

    @Test
    fun bounceFromLandingSquareIfEmpty() {
        leaveFieldAt(25, 1)
        controller.rollForward(
            2.d3, // Direction
            DiceRollResults(2.d6, 2.d6), // Distance
        )
        assertEquals(BallState.BOUNCING, state.singleBall().state)
        controller.rollForward(
            2.d8, // Bounce
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        assertEquals(FieldCoordinate(21,0), state.singleBall().location)
    }

    @Test
    fun repeatThrowInUntilLandingOnField() {
        leaveFieldAt(25, 4)
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
        assertEquals(FieldCoordinate(21,3), state.singleBall().location)
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
                1.d6, // Fail pickup
                NoRerollSelected(),
                1.d8, // Bounce out of field
            )
        } else {
            controller.rollForward(
                PlayerSelected("A10".playerId),
                PlayerActionSelected(PlayerStandardActionType.PASS),
                *moveTo(17, 7),
                6.d6, // Pick-up
                NoRerollSelected(),
                Confirm, // Start Pass section
                FieldSquareSelected(x, y), // Throw into the corner
                6.d6, // Throw ball
                NoRerollSelected(), // No Reroll
                bounceDirection,
            )
        }
    }
}
