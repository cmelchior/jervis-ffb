package com.jervisffb.test.actions.move

import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.model.locations.OnFieldLocation
import com.jervisffb.engine.rules.PlayerStandardActionType
import com.jervisffb.engine.rules.bb2020.procedures.actions.move.MoveAction
import com.jervisffb.test.JervisGameTest
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.moveTo
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Test a player dodging as described on page 45 in the BB2020 Rulebook.
 *
 * Note, any skills that affect dodges are testing in their own test class.
 * This class only tests the basic functionality.
 */
class DodgeTests: JervisGameTest() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun noRoll_movingAwayFromNonMarkingPlayer() {
        state.field[12, 5].player!!.hasTackleZones = false
        state.field[12, 6].player!!.hasTackleZones = false

        val player = state.field[13, 5].player!!
        assertFalse(rules.isMarked(player))
        val markingPlayers = (player.location as OnFieldLocation).getSurroundingCoordinates(rules).any {
            state.field[it.x, it.y].player?.let {
                rules.canMark(it) && it.team != player.team
            } ?: false
        }
        assertFalse(markingPlayers)

        controller.rollForward(
            PlayerSelected(player.id),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            *moveTo(14, 5) // Requires no dodge
        )
        assertEquals(FieldCoordinate(14, 5), player.location)
    }

    @Test
    fun roll_movingAwayFromMarkingPlayer() {
        val player = state.field[13, 6].player!!
        val movesLeft = player.movesLeft
        assertTrue(rules.isMarked(player))
        controller.rollForward(
            PlayerSelected(player.id),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            3.d6,
            NoRerollSelected()
        )
        assertEquals(movesLeft - 1, player.movesLeft)
        assertEquals(FieldCoordinate(14, 5), player.location)
        assertEquals(MoveAction.SelectMoveType, controller.currentNode())
    }

    @Test
    fun failedRoll_turnOverInTargetSquare() {
        val player = state.field[13, 6].player!!
        assertTrue(rules.isMarked(player))
        controller.rollForward(
            PlayerSelected(player.id),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            2.d6, // Fail roll
            NoRerollSelected()
        )
        assertEquals(FieldCoordinate(14, 5), player.location)
        assertEquals(PlayerState.FALLEN_OVER, player.state)
        assertTrue(state.isTurnOver())
    }
}
