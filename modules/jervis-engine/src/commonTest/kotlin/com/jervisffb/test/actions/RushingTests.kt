package com.jervisffb.test.actions

import com.jervisffb.engine.actions.EndActionWhenReady
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.RerollOptionSelected
import com.jervisffb.engine.actions.SelectRerollOption
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.bb2020.procedures.actions.move.MoveAction
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.test.JervisGameTest
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.moveTo
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Test a player rushing as described on page 44 in the BB2020 Rulebook.
 *
 * Note, any skills that affect rushes are tested in their own test class.
 * This class only tests the basic functionality.
 */
class RushingTests: JervisGameTest() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun successfulRush() {
        controller.rollForward(
            PlayerSelected("A8".playerId),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            *moveTo(16, 13),
            *moveTo(17, 13),
            *moveTo(18, 13),
            *moveTo(19, 13),
            *moveTo(20, 13),
            *moveTo(21, 13),
            *moveTo(22, 13),
            *moveTo(23, 13), // 1st Rush
            2.d6,
            NoRerollSelected(),
            *moveTo(24, 13), // 2nd Rush
            1.d6,
        )
        val reroll = RerollOptionSelected(
            option = (controller.getAvailableActions().actions.last() as SelectRerollOption).options.first()
        )
        controller.rollForward(
            reroll,
            6.d6
        )
        // After rushing twice, no more moves are allowed
        val actions = controller.getAvailableActions().actions
        assertEquals(EndActionWhenReady, actions.single())
    }

    @Test
    fun failedRush() {
        controller.rollForward(
            PlayerSelected("A8".playerId),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            *moveTo(16, 13),
            *moveTo(17, 13),
            *moveTo(18, 13),
            *moveTo(19, 13),
            *moveTo(20, 13),
            *moveTo(21, 13),
            *moveTo(22, 13),
            *moveTo(23, 13), // Rush
            1.d6, // Fail Rush
            NoRerollSelected(),
        )
        assertEquals(PlayerState.FALLEN_OVER, state.getPlayerById("A8".playerId).state)
        assertEquals(TurnOver.STANDARD, state.turnOver)
    }

    @Test
    fun rushBeforeDodge() {
        val player = state.field[13, 6].player!!
        player.movesLeft = 0
        assertTrue(rules.isMarked(player))

        controller.rollForward(
            PlayerSelected(player.id),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            2.d6, // Rush
            NoRerollSelected(),
            3.d6, // Dodge
            NoRerollSelected(),
        )

        assertEquals(1, player.rushesLeft)
        assertEquals(0, player.movesLeft)
        assertEquals(FieldCoordinate(14, 5), player.location)
        assertEquals(MoveAction.SelectMoveType, controller.currentNode())
    }
}
