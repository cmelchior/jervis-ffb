package com.jervisffb.test

import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.EndSetup
import com.jervisffb.engine.actions.EndTurn
import com.jervisffb.engine.ext.d3
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.test.ext.rollForward
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Class responsible for testing game progress, i.e., moving turn markers and
 * correctly switching halfs and moving into overtime.
 */
class GameProgressTests: JervisGameTest() {

    @Test
    fun noActiveTeamDuringKickOffEvent() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(4.d6, 5.d6), // Quick Snap
                    3.d3
                ),
                bounce = null,
            ),
        )
        assertNull(state.activeTeam)
        controller.rollForward(
            EndSetup,
            3.d8 // Bounce
        )
        assertEquals(awayTeam, state.activeTeam)
    }

    @Test
    fun teamIsActiveDuringTeamTurn() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam()
        )
        assertEquals(awayTeam, state.activeTeam)
        controller.rollForward(EndTurn)
        assertEquals(homeTeam, state.activeTeam)
    }

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
