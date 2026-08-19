package com.jervisffb.test.bb2025.bb7

import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.PassTypeSelected
import com.jervisffb.engine.actions.PitchSquareSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.ext.d3
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.common.actions.PassType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.test.JervisGameBB72025Test
import com.jervisffb.test.SmartMoveTo
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.pickup
import com.jervisffb.test.throwBall
import com.jervisffb.test.utils.assertCoordinates
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * This class test differences in throwing in the ball compared to normal
 * standard BB2025.
 */
class BB7ThrowIn: JervisGameBB72025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun throwInWhenLeavingPitch() {
        val thrower = awayTeam["A1".playerId]
        controller.rollForward(
            PlayerSelected(thrower),
            PlayerActionSelected(PlayerStandardActionType.PASS),
            SmartMoveTo(14, 5),
            *pickup(6.d6),
            PassTypeSelected(PassType.STANDARD),
            PitchSquareSelected(14, 0),
            *throwBall(6.d6),
            2.d8, // Bounce outside the pitch at [14, -1]
        )
        assertEquals(BallState.OUT_OF_BOUNDS, state.currentBall().state)
        controller.rollForward(
            2.d3, // Roll throw-in direction
        )
        assertEquals(BallState.THROW_IN, state.currentBall().state)
        controller.rollForward(
            DiceRollResults(1.d6) // Distance
        )
        assertEquals(BallState.BOUNCING, state.currentBall().state)
        controller.rollForward(
            3.d8, // Bounce
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        assertEquals(PitchCoordinate(15, 1), state.singleBall().coordinates)
    }

    @Test
    fun throwInAgainIfLeavingPitchAgain() {
        val thrower = awayTeam["A1".playerId]
        controller.rollForward(
            PlayerSelected(thrower),
            PlayerActionSelected(PlayerStandardActionType.PASS),
            SmartMoveTo(14, 5),
            *pickup(6.d6),
            PassTypeSelected(PassType.STANDARD),
            PitchSquareSelected(18, 0),
            *throwBall(6.d6),
            2.d8, // Bounce outside the pitch at [18,-1]
            1.d3, // Throw-in direction
            6.d6, // Throw-in distance. Leave pitch at [19,1]
            2.d3, // Throw-in direction
            2.d6, // Throw-in distance
            2.d8, // Bounce
        )

        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        state.singleBall().assertCoordinates(16, 0)
    }
}
