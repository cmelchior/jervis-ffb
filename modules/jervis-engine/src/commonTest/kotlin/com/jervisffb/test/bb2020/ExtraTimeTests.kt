package com.jervisffb.test.bb2020

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.rules.BB2020Rules
import com.jervisffb.engine.rules.StandardBB2020Rules
import com.jervisffb.engine.rules.common.procedures.DetermineKickingTeamStep
import com.jervisffb.test.JervisGameBB2020Test
import com.jervisffb.test.defaultDetermineKickingTeam
import com.jervisffb.test.defaultKickOffAwayTeam
import com.jervisffb.test.defaultKickOffHomeTeam
import com.jervisffb.test.defaultSetup
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.skipTurns
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Class responsible for testing Extra Time and Sudden Death.
 */
class ExtraTimeTests: JervisGameBB2020Test() {

    override val rules: BB2020Rules = StandardBB2020Rules().toBuilder().run {
        hasExtraTime = true
        turnsInExtraTime = 8
        build()
    }

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun stoppingGameAfterNormalTimeIfWinnerFound() {
        controller.state.homeGoals = 1 // Fake Home having one goal
        controller.rollForward(
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
            *skipTurns(16),
            *defaultSetup(homeFirst = false),
            *defaultKickOffAwayTeam(),
            *skipTurns(16),
        )
        assertEquals(3, state.halfNo)
        assertEquals(0, state.driveNo)
        assertEquals(DetermineKickingTeamStep.SelectCoinSide, controller.stack.currentNode()) // Game has ended
    }

    @Test
    fun endExtraTimeIfWinnerFound() {
        controller.rollForward(
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
