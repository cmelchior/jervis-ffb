package com.jervisffb.test

import com.jervisffb.engine.actions.EndTurn
import com.jervisffb.engine.rules.bb2020.procedures.FullGame
import com.jervisffb.test.ext.rollForward
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Class responsible for testing game progress, i.e., moving turn markers and
 * correctly switching halfs and moving into overtime.
 */
class GameProgressTests: JervisGameTest() {

    @Test
    fun increaseTurnAndHalfCounter() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
        )
        // Before setup
        assertEquals(1, state.halfNo)
        assertEquals(1, state.driveNo)
        assertEquals(0, state.homeTeam.turnMarker)
        assertEquals(0, state.awayTeam.turnMarker)
        controller.rollForward(
            *defaultKickOffHomeTeam(),
        )
        // First Away turn
        assertEquals(0, state.homeTeam.turnMarker)
        assertEquals(1, state.awayTeam.turnMarker)
        controller.rollForward(
            EndTurn,
        )
        // First Home turn
        assertEquals(1, state.homeTeam.turnMarker)
        assertEquals(1, state.awayTeam.turnMarker)
        controller.rollForward(*skipTurns(14))
        // End of 1st Half
        assertEquals(8, state.homeTeam.turnMarker)
        assertEquals(8, state.awayTeam.turnMarker)
        controller.rollForward(EndTurn)
        // Start of 2nd Half
        assertEquals(2, state.halfNo)
        assertEquals(1, state.driveNo)
        assertEquals(0, state.homeTeam.turnMarker)
        assertEquals(0, state.awayTeam.turnMarker)
        controller.rollForward(
            *defaultSetup(homeFirst = false),
            *defaultKickOffAwayTeam()
        )
        controller.rollForward(*skipTurns(16))
        // End of Game
        assertTrue(controller.stack.isEmpty())
        assertEquals(2, state.halfNo)
        assertEquals(1, state.driveNo)
        assertEquals(8, state.homeTeam.turnMarker)
        assertEquals(8, state.awayTeam.turnMarker)
    }

    @Test
    @Ignore
    fun driveCounterIncreaseOnScoring() {
        TODO()
    }
}
