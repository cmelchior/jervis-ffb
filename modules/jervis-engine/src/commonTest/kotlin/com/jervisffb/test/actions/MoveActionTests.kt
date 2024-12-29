package com.jervisffb.test.actions

import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndActionWhenReady
import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectFieldLocation
import com.jervisffb.engine.actions.TargetSquare
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.Availability
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.PlayerStandardActionType
import com.jervisffb.test.JervisGameTest
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.moveTo
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Class responsible for testing the Move Action. On its own as well as a
 * part of other actions like: Blitz, Foul, Hand-Off, Pass, Throw Team-Mate.
 */
class MoveActionTests: JervisGameTest() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    private fun startMoveAction() {
        controller.rollForward(
            PlayerSelected("A8".playerId),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
        )
    }

    @Test
    fun endActionBeforeMovingDoesNotMarkPlayerAsActivated() {
        startMoveAction()
        val movingPlayer = state.getPlayerById("A8".playerId)
        assertEquals(Int.MAX_VALUE, state.activeTeam.turnData.moveActions)
        assertEquals(Availability.IS_ACTIVE, movingPlayer.available)
        controller.handleAction(EndAction)
        assertEquals(Availability.AVAILABLE, movingPlayer.available)
        assertEquals(Int.MAX_VALUE, state.activeTeam.turnData.moveActions)
    }

    @Test
    fun endActionWhileProneDoesNotMarkPlayerAsActivated() {
        val movingPlayer = state.getPlayerById("A8".playerId)
        movingPlayer.state = PlayerState.PRONE
        startMoveAction()
        assertEquals(Int.MAX_VALUE, state.activeTeam.turnData.moveActions)
        assertEquals(Availability.IS_ACTIVE, movingPlayer.available)
        controller.handleAction(EndAction)
        assertEquals(Availability.AVAILABLE, movingPlayer.available)
        assertEquals(Int.MAX_VALUE, state.activeTeam.turnData.moveActions)
    }

    @Test
    fun movedPlayerIsActivated() {
        val movingPlayer = state.getPlayerById("A8".playerId)
        startMoveAction()
        controller.rollForward(*moveTo(14, 14))
        controller.handleAction(EndAction)
        assertEquals(Availability.HAS_ACTIVATED, movingPlayer.available)
        assertEquals(Int.MAX_VALUE - 1, state.activeTeam.turnData.moveActions)
    }

    @Test
    fun movingSpendsOneMovement() {
        val movingPlayer = state.getPlayerById("A8".playerId)
        val startingMove = movingPlayer.movesLeft
        startMoveAction()
        controller.rollForward(*moveTo(14, 14))
        assertEquals(startingMove - 1, movingPlayer.movesLeft)
    }

    @Test
    fun canOnlyMoveToNearbySquares() {
        controller.rollForward(
            PlayerSelected("A8".playerId),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.STANDARD),
        )

        val availableTargets = controller.getAvailableActions().actions
            .filterIsInstance<SelectFieldLocation>()
            .first().squares
            .filter { it.type == TargetSquare.Type.MOVE }
            .map { it.coordinate }
            .toSet()

        // Player starts at (15, 13). (14, 13) contains another player
        val expectedTargets = setOf(
            FieldCoordinate(14, 12),
            FieldCoordinate(14, 14),
            FieldCoordinate(15, 12),
            FieldCoordinate(15, 14),
            FieldCoordinate(16, 12),
            FieldCoordinate(16, 13),
            FieldCoordinate(16, 14),
        )

        assertTrue(availableTargets.containsAll(expectedTargets))
        assertTrue(expectedTargets.containsAll(availableTargets))
    }

    // Action doesn't end when no more "normal move" is left, only when all rushes are also used.
    @Test
    fun actionEndsWhenNoRushesLeft() {
        val movingPlayer = state.getPlayerById("A8".playerId)
        movingPlayer.movesLeft = 3
        val startingMove = movingPlayer.movesLeft
        startMoveAction()

        controller.rollForward(
            *moveTo(14, 14),
            *moveTo(15, 14),
            *moveTo(16, 14)
        )
        assertEquals(0, movingPlayer.movesLeft)
        assertTrue(controller.getAvailableActions().isValid(MoveTypeSelected(MoveType.STANDARD)))
        controller.rollForward(
            *moveTo(17, 14),
            6.d6, // Rush
            NoRerollSelected(),
            *moveTo(18, 14),
            6.d6, // Rush
            NoRerollSelected(),
        )

        assertEquals(EndActionWhenReady, controller.getAvailableActions().actions.single())
    }
}
