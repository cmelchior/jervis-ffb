package com.jervisffb.test

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndSetup
import com.jervisffb.engine.actions.EndTurn
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.ext.d3
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.ext.playerNo
import com.jervisffb.engine.model.Availability
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.rules.PlayerStandardActionType
import com.jervisffb.test.ext.rollForward
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Class responsible for testing game progress, i.e., moving turn markers,
 * correctly switching half's, moving into overtime and when teams and players
 * activate.
 */
class GameProgressTests: JervisGameTest() {

    private fun setupGame() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam()
        )
    }

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
        setupGame()
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

    @Test
    fun activatePlayerBeforeSelectingAction() {
        setupGame()
        val player = awayTeam[1.playerNo]
        assertEquals(Availability.AVAILABLE, player.available)
        controller.rollForward(PlayerSelected(player.id))
        assertEquals(Availability.IS_ACTIVE, player.available)
        assertEquals(player, state.activePlayer)
    }

    @Test
    fun playerIsActiveDuringAction() {
        setupGame()
        val player = awayTeam[1.playerNo]
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.MOVE)
        )
        assertEquals(Availability.IS_ACTIVE, player.available)
    }

    @Test
    fun undoMoveActivation() {
        setupGame()
        val player = awayTeam[10.playerNo]
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.MOVE),
            EndAction
        )
        assertEquals(Availability.AVAILABLE, player.available)
    }

    @Test
    fun cannotUndoMoveActivation() {
        setupGame()
        val player = awayTeam[10.playerNo]
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.MOVE),
            SmartMoveTo(15, 7),
            EndAction
        )
        assertEquals(Availability.HAS_ACTIVATED, player.available)
    }

    @Test
    fun undoBlockActivation() {
        setupGame()
        val player = awayTeam[1.playerNo]
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            PlayerSelected("H1".playerId),
            EndAction
        )
        assertEquals(Availability.AVAILABLE, player.available)
    }

    @Test
    fun cannotUndoBlockActivation() {
        setupGame()
        val player = awayTeam[1.playerNo]
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 4.dblock),
            DirectionSelected(Direction.LEFT),
            Cancel,
        )
        assertEquals(Availability.HAS_ACTIVATED, player.available)
    }


    @Test
    fun undoBlitzActivation() {
        setupGame()
        val player = awayTeam[1.playerNo]
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLITZ),
            PlayerSelected("H1".playerId),
            EndAction
        )
        assertEquals(Availability.AVAILABLE, player.available)
    }

    @Test
    fun cannotUndoBlitzActivation() {
        setupGame()
        val player = awayTeam[10.playerNo]
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.BLITZ),
            PlayerSelected("H1".playerId),
            *moveTo(15, 7),
            EndAction
        )
        assertEquals(Availability.HAS_ACTIVATED, player.available)
    }

    @Test
    fun undoPassActivation() {
        setupGame()
        val player = awayTeam[1.playerNo]
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.PASS),
            EndAction
        )
        assertEquals(Availability.AVAILABLE, player.available)
    }

    @Test
    fun cannotUndoPassActivation() {
        setupGame()
        val player = awayTeam[10.playerNo]
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(15, 7),
            EndAction
        )
        assertEquals(Availability.HAS_ACTIVATED, player.available)
    }

    @Test
    fun undoHandOffActivation() {
        setupGame()
        val player = awayTeam[10.playerNo]
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.HAND_OFF),
            EndAction
        )
        assertEquals(Availability.AVAILABLE, player.available)
    }

    @Test
    fun cannotUndoHandOffActivation() {
        setupGame()
        val player = awayTeam[10.playerNo]
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.HAND_OFF),
            *moveTo(15, 7),
            EndAction
        )
        assertEquals(Availability.HAS_ACTIVATED, player.available)
    }

    @Test
    fun undoFoulActivation() {
        setupGame()
        homeTeam["H1".playerId].state = PlayerState.PRONE
        val player = awayTeam[10.playerNo]
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.FOUL),
            PlayerSelected("H1".playerId),
            EndAction
        )
        assertEquals(Availability.AVAILABLE, player.available)
    }

    @Test
    fun cannotUndoFoulActivation() {
        setupGame()
        homeTeam["H1".playerId].state = PlayerState.PRONE
        val player = awayTeam[10.playerNo]
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.FOUL),
            PlayerSelected("H1".playerId),
            *moveTo(15, 7),
            EndAction
        )
        assertEquals(Availability.HAS_ACTIVATED, player.available)
    }

    @Ignore
    @Test
    fun undoThrowTeamMateActivation() {
        TODO()
    }

    @Ignore
    @Test
    fun cannotUndoThrowTeamMateActivation() {
        TODO()
    }

}
