package com.jervisffb.test

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.rules.BB2020Rules
import com.jervisffb.engine.rules.bb2020.procedures.DetermineKickingTeam
import com.jervisffb.test.ext.rollForward
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Class responsible for testing Extra Time and Sudden Death.
 */
class ExtraTimeTests: JervisGameTest() {

    override val rules: BB2020Rules = object: BB2020Rules() {
        override val hasExtraTime: Boolean = true
        override val turnsInExtraTime: Int = 8
    }

    @Test
    fun stoppingGameAfterNormalTimeIfWinnerFound() {
        controller.state.homeGoals = 1 // Fake Home having one goal
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(),
            *skipTurns(16),
            *defaultSetup(homeFirst = false),
            *defaultKickOffAwayTeam(),
            *skipTurns(16),
        )
        assertTrue(controller.stack.isEmpty()) // Game has ended
        assertEquals(1, state.homeScore)
        assertEquals(1, state.homeGoals)
        assertEquals(0, state.awayScore)
        assertEquals(0, state.awayGoals)
    }

    @Test
    fun goIntoExtraTimeIfDraw() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(),
            *skipTurns(16),
            *defaultSetup(homeFirst = false),
            *defaultKickOffAwayTeam(),
            *skipTurns(16),
        )
        assertEquals(3, state.halfNo)
        assertEquals(0, state.driveNo)
        assertEquals(DetermineKickingTeam.SelectCoinSide, controller.stack.currentNode()) // Game has ended
    }

    @Test
    fun endExtraTimeIfWinnerFound() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(),
            *skipTurns(16),
            *defaultSetup(homeFirst = false),
            *defaultKickOffAwayTeam(),
            *skipTurns(16),
            *defaultDetermineKickingTeam(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(),
            *skipTurns(15)
        )
        state.homeExtraTimeGoals = 1
        controller.rollForward(*skipTurns(1))
        assertTrue(controller.stack.isEmpty()) // Game has ended
        assertEquals(1, state.homeScore)
        assertEquals(0, state.awayScore)
        assertEquals(1, state.homeExtraTimeGoals)
        assertEquals(0, state.awayExtraTimeGoals)
    }

    @Test
    fun suddenDeath() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(),
            *skipTurns(16),
            *defaultSetup(homeFirst = false),
            *defaultKickOffAwayTeam(),
            *skipTurns(16),
            *defaultDetermineKickingTeam(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(),
            *skipTurns(16),
            D6Result(3),
            D6Result(2),
            D6Result(4),
            D6Result(3),
            D6Result(1),
            D6Result(1),
            D6Result(1),
            D6Result(6),
            D6Result(2),
            D6Result(5),
            D6Result(4),
            D6Result(2),
        )
        assertTrue(controller.stack.isEmpty()) // Game has ended
        assertEquals(3, state.halfNo)
        assertEquals(1, state.driveNo)
        assertEquals(3, state.homeScore)
        assertEquals(2, state.awayScore)
        assertEquals(3, state.homeSuddenDeathGoals)
        assertEquals(2, state.awaySuddenDeathGoals)
    }
}
