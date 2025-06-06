package com.jervisffb.test

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.PlayerStandardActionType
import com.jervisffb.engine.rules.bb2020.procedures.actions.pass.PassContext
import com.jervisffb.engine.rules.bb2020.procedures.actions.pass.PassingType
import com.jervisffb.test.ext.rollForward
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Turnovers are always associated with other actions and should normally
 * explicitly on their own page, we also compile them together here.
 * be covered by tests for those actions, but since turnovers are described
 *
 * See page 23 in the rulebook.
 */
class TurnOverTests: JervisGameTest() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun sentOffByRef() {
        homeTeam["H1".playerId].state = PlayerState.PRONE
        assertEquals(awayTeam, state.activeTeam)
        assertEquals(1, awayTeam.turnMarker)
        assertEquals(0, homeTeam.turnMarker)
        controller.rollForward(
            *activatePlayer("A6", PlayerStandardActionType.FOUL),
            PlayerSelected("H1".playerId),
            SmartMoveTo(13, 4),
            PlayerSelected("H1".playerId), // Start foul
            DiceRollResults(2.d6, 2.d6), // Roll double -> Sent off
            Cancel // Do not argue the call
        )
        assertEquals(homeTeam, state.activeTeam)
        assertEquals(1, awayTeam.turnMarker)
        assertEquals(1, awayTeam.turnMarker)
    }

    @Test
    fun fumblesPassAction() {
        // Still a turn over even though the fumbled pass ends up on the throwers team
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            4.d6, // Pickup
            NoRerollSelected(),
            SmartMoveTo(14, 5),
            Confirm, // Start pass
            FieldSquareSelected(13, 5),
            1.d6, // Fumbbl pass
            NoRerollSelected(),
        )
        assertEquals(PassingType.FUMBLED, state.getContext<PassContext>().passingResult)
        controller.rollForward(
            4.d8, // Bounce
            6.d6, // Catch
            NoRerollSelected(),
        )
        assertTrue(awayTeam["A1".playerId].hasBall())
        assertEquals(homeTeam, state.activeTeam)
    }

    @Test
    fun failedToCatchPass_ground() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            4.d6, // Pickup
            NoRerollSelected(),
            Confirm, // Start pass
            FieldSquareSelected(15, 1),
            6.d6, // Pass
            NoRerollSelected(),
            1.d6, // Fail catch
            NoRerollSelected(),
            2.d8, // Bounce
        )
        assertEquals(homeTeam, state.activeTeam)
    }

    @Test
    fun failedToCatchPass_opponent() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            4.d6, // Pickup
            NoRerollSelected(),
            Confirm, // Start pass
            FieldSquareSelected(13, 5),
            6.d6, // Pass
            NoRerollSelected(),
            1.d6, // Fail catch
            NoRerollSelected(),
            4.d8, // Bounce
            6.d6, // Caught by opponent
        )
        assertEquals(homeTeam, state.activeTeam)
    }

    @Test
    fun failedToCatchHandOff_ground() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.HAND_OFF),
            *moveTo(17, 7),
            4.d6, // Pickup
            NoRerollSelected(),
            SmartMoveTo(14, 5),
            PlayerSelected("A1".playerId),
            1.d6, // Fail catch
            NoRerollSelected(),
            3.d8, // Bounce
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        assertEquals(FieldCoordinate(14, 4), state.singleBall().location)
        assertEquals(homeTeam, state.activeTeam)
    }

    @Test
    fun failedToCatchHandOff_opponent() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.HAND_OFF),
            *moveTo(17, 7),
            4.d6, // Pickup
            NoRerollSelected(),
            SmartMoveTo(14, 5),
            PlayerSelected("A1".playerId),
            1.d6, // Fail catch
            NoRerollSelected(),
            4.d8, // Bounce
            6.d6 // Caught by opponent
        )
        assertTrue(homeTeam["H1".playerId].hasBall())
        assertEquals(homeTeam, state.activeTeam)
    }

    @Test
    fun passAction_deflectEndsUpOnFloor() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            4.d6, // Pickup
            NoRerollSelected(),
            SmartMoveTo(12, 3),
            Confirm, // Start pass
            FieldSquareSelected(13, 9),
            6.d6, // Pass
            NoRerollSelected(),
            PlayerSelected("H2".playerId), // Select Interceptor
            6.d6, // Deflect
            2.d6, // Fail Intercept
            DiceRollResults(5.d8, 5.d8, 5.d8), // Scatter
            5.d8 // Bounce
        )
        // Results in a turnover
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        assertEquals(FieldCoordinate(16, 6), state.singleBall().location)
        assertEquals(homeTeam, state.activeTeam)
    }

    @Test
    fun passAction_deflectEndsUpOnInterceptorTeam() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            4.d6, // Pickup
            NoRerollSelected(),
            SmartMoveTo(12, 3),
            Confirm, // Start pass
            FieldSquareSelected(13, 9),
            6.d6, // Pass
            NoRerollSelected(),
            PlayerSelected("H2".playerId), // Select Interceptor
            6.d6, // Deflect
            2.d6, // Fail Intercept
            DiceRollResults(2.d8, 7.d8, 5.d8), // Scatter
            6.d6, // Catch
            NoRerollSelected()
        )
        // No turnover, but pass action ends
        assertEquals(BallState.CARRIED, state.singleBall().state)
        assertTrue(awayTeam["A2".playerId].hasBall())
        assertEquals(awayTeam, state.activeTeam)
        assertNull(state.activePlayer)
        assertEquals(0, awayTeam.turnData.passActions)
    }

    @Test
    fun successfulInterception() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            4.d6, // Pickup
            NoRerollSelected(),
            SmartMoveTo(12, 3),
            Confirm, // Start pass
            FieldSquareSelected(13, 9),
            6.d6, // Pass
            NoRerollSelected(),
            PlayerSelected("H2".playerId), // Select Interceptor
            6.d6, // Deflect
            6.d6, // Intercept
        )
        assertTrue(homeTeam["H2".playerId].hasBall())
        assertEquals(homeTeam, state.activeTeam)
    }
}
