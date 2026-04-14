package com.jervisffb.test.bb2025

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndTurn
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.rules.BB2025Rules
import com.jervisffb.engine.rules.StandardBB2025Rules
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.procedures.DetermineKickingTeamStep
import com.jervisffb.engine.rules.common.rerolls.RegularTeamReroll
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.defaultDetermineKickingTeam
import com.jervisffb.test.defaultKickOffAwayTeam
import com.jervisffb.test.defaultKickOffHomeTeam
import com.jervisffb.test.defaultSetup
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.moveTo
import com.jervisffb.test.skipTurns
import com.jervisffb.test.utils.TeamRerollSelected
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Class responsible for testing Extra Time and Sudden Death.
 */
class ExtraTimeTests: JervisGameBB2025Test() {

    override val rules: BB2025Rules = StandardBB2025Rules().update {
        hasExtraTime = true
        turnsInExtraTime = 8
    }

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun unusedRerollsCarryOverIntoExtraTime() {
        controller.rollForward(
            *skipTurns(16),
            *defaultSetup(homeFirst = false),
            *defaultKickOffAwayTeam(),
            EndTurn,
        )
        val player = awayTeam["A1".playerId]
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            1.d6, // Fail dodge
            TeamRerollSelected<RegularTeamReroll>(),
            4.d6, // Succeed dodge
            EndAction
        )
        assertEquals(1, awayTeam.rerolls.count { it.rerollUsed })
        assertEquals(3, awayTeam.rerolls.count { !it.rerollUsed })
        assertTrue(homeTeam.rerolls.none { it.rerollUsed })
        controller.rollForward(
            *skipTurns(15), // End 2nd half
            *defaultDetermineKickingTeam(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(),
        )
        // Rerolls keep their state from 2nd half
        assertEquals(1, awayTeam.rerolls.count { it.rerollUsed })
        assertEquals(3, awayTeam.rerolls.count { !it.rerollUsed })
        assertTrue(homeTeam.rerolls.none { it.rerollUsed })
    }

    @Test
    fun stoppingGameAfterNormalTimeIfWinnerFound() {
        controller.state.homeTouchdowns = 1 // Fake Home having one touchdown
        controller.rollForward(
            *skipTurns(16),
            *defaultSetup(homeFirst = false),
            *defaultKickOffAwayTeam(),
            *skipTurns(16),
        )
        assertTrue(controller.stack.isEmpty()) // Game has ended
        assertEquals(1, state.homeScore)
        assertEquals(1, state.homeTouchdowns)
        assertEquals(0, state.awayScore)
        assertEquals(0, state.awayTouchdowns)
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
        state.homeExtraTimeTouchdowns = 1
        controller.rollForward(*skipTurns(1))
        assertTrue(controller.stack.isEmpty()) // Game has ended
        assertEquals(1, state.homeScore)
        assertEquals(0, state.awayScore)
        assertEquals(1, state.homeExtraTimeTouchdowns)
        assertEquals(0, state.awayExtraTimeTouchdowns)
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
        assertEquals(3, state.homeSuddenDeathTouchdowns)
        assertEquals(2, state.awaySuddenDeathTouchdowns)
    }
}
