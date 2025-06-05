package com.jervisffb.test.tables

import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.EndTurn
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.locations.DogOut
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.model.modifiers.BrilliantCoachingModifiers
import com.jervisffb.engine.rules.PlayerStandardActionType
import com.jervisffb.engine.rules.bb2020.skills.BrilliantCoachingReroll
import com.jervisffb.test.JervisGameTest
import com.jervisffb.test.SmartMoveTo
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.defaultKickOffAwayTeam
import com.jervisffb.test.defaultSetup
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.skipTurns
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for the outcomes of rolling on Argue the Call Table as defined
 * on page 63 in the rulebook.
 */
class ArgueTheCallTests: JervisGameTest() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun youAreOuttaHere() {
        homeTeam["H1".playerId].state = PlayerState.PRONE

        // Foul 1st time and get the coach banned
        controller.rollForward(
            *activatePlayer("A6", PlayerStandardActionType.FOUL),
            PlayerSelected("H1".playerId),
            SmartMoveTo(13, 4),
            PlayerSelected("H1".playerId), // Start foul
            DiceRollResults(2.d6, 2.d6), // Roll double -> Sent off
            Confirm,
            1.d6, // Roll You're Outta Here
        )
        assertEquals(homeTeam, state.activeTeam)
        assertTrue(awayTeam.coachBanned)
        assertEquals(1, awayTeam.brilliantCoachingModifiers.size)
        assertContains(awayTeam.brilliantCoachingModifiers, BrilliantCoachingModifiers.YOU_ARE_OUTTA_HERE)

        // Do a 2nd Foul and check that it is no longer possible to argue the call
        controller.rollForward(
            EndTurn, // Give control back to away team
            *activatePlayer("A1", PlayerStandardActionType.FOUL),
            PlayerSelected("H1".playerId), // Select same foul target
            PlayerSelected("H1".playerId),  // Start the foul
            DiceRollResults(2.d6, 2.d6), // Caught again by the ref. Is sent off without being offered the chance to argue
        )
        assertEquals(homeTeam, state.activeTeam)
        assertEquals(PlayerState.BANNED, awayTeam["A1".playerId].state)
        assertEquals(DogOut, awayTeam["A1".playerId].location)

        // Skip to next halfs
        controller.rollForward(
            *skipTurns(13),
        )
        // Unban players (to avoid breaking setup)
        awayTeam["A1".playerId].state = PlayerState.RESERVE
        awayTeam["A6".playerId].state = PlayerState.RESERVE
        controller.rollForward(
            *defaultSetup(homeFirst = false),
        )
        assertEquals(4, homeTeam.rerolls.size)
        assertEquals(4, awayTeam.rerolls.size)
        assertEquals(0, homeTeam.assistantCoaches)
        assertEquals(0, awayTeam.assistantCoaches)

        // And Brilliant Coaching now has a penalty when rolling on the Kick-off Table
        controller.rollForward(
            *defaultKickOffAwayTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(3.d6, 4.d6), // Roll Brilliant Coaching
                    1.d6, // Brilliant coaching - Kicking Team (away)
                    1.d6 // Brilliant coaching - Receiving Team (home)
                )
            )
        )
        assertEquals(5, homeTeam.rerolls.size)
        assertEquals(1, homeTeam.rerolls.filterIsInstance<BrilliantCoachingReroll>().size)
        assertEquals(0, awayTeam.rerolls.filterIsInstance<BrilliantCoachingReroll>().size)
    }



    @Test
    fun iDontCare() {
        homeTeam["H1".playerId].state = PlayerState.PRONE
        assertEquals(1, awayTeam.turnData.foulActions)
        assertEquals(awayTeam, state.activeTeam)
        controller.rollForward(
            *activatePlayer("A6", PlayerStandardActionType.FOUL),
            PlayerSelected("H1".playerId),
            SmartMoveTo(13, 4),
            PlayerSelected("H1".playerId), // Start foul
            DiceRollResults(2.d6, 2.d6), // Roll double -> Sent off
            Confirm, // Argue the call
            3.d6 // Roll "I Don't Care"
        )
        assertEquals(homeTeam, state.activeTeam)
        assertEquals(PlayerState.BANNED, awayTeam["A6".playerId].state)
        assertEquals(DogOut, awayTeam["A6".playerId].location)
        assertFalse(awayTeam.coachBanned)
    }

    @Test
    fun wellIfYouPutItLikeThat() {
        homeTeam["H1".playerId].state = PlayerState.PRONE
        assertEquals(1, awayTeam.turnData.foulActions)
        assertEquals(awayTeam, state.activeTeam)
        controller.rollForward(
            *activatePlayer("A6", PlayerStandardActionType.FOUL),
            PlayerSelected("H1".playerId),
            SmartMoveTo(13, 4),
            PlayerSelected("H1".playerId), // Start foul
            DiceRollResults(2.d6, 2.d6), // Roll double -> Sent off
            Confirm, // Argue the call
            6.d6 // Roll "Well, When You Put It Like That..."
        )

        // Check that the player was allowed to remain on the field, but a turnover
        // was still triggered
        assertEquals(homeTeam, state.activeTeam)
        assertEquals(PlayerState.STANDING, awayTeam["A6".playerId].state)
        assertEquals(FieldCoordinate(13, 4), awayTeam["A6".playerId].location)
        assertFalse(awayTeam.coachBanned)
    }
}
