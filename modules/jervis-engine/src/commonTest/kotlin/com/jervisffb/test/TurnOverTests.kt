package com.jervisffb.test

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.rules.PlayerStandardActionType
import com.jervisffb.test.ext.rollForward
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

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
        TODO()
    }

    @Test
    fun failedToCatchPass_ground() {
        TODO()
    }

    @Test
    fun failedToCatchPass_opponent() {
        TODO()
    }

    @Test
    fun failedToCatchHandOff_ground() {
        TODO()
    }

    @Test
    fun failedToCatchHandOff_opponent() {
        TODO()
    }

    @Test
    fun deflectedPass_ground() {
        TODO()
    }

    @Test
    fun deflectedPass_opponent() {
        TODO()
    }

    @Test
    fun interceptedPass() {
        TODO()
    }
}
