package com.jervisffb.test.bb2025.actions

import com.jervisffb.engine.actions.EndActionWhenReady
import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.RerollOptionSelected
import com.jervisffb.engine.actions.SelectFieldLocation
import com.jervisffb.engine.actions.SelectMoveType
import com.jervisffb.engine.actions.SelectRerollOption
import com.jervisffb.engine.actions.TargetSquare
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.procedures.actions.move.MoveAction
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.moveTo
import com.jervisffb.test.rushRoll
import com.jervisffb.test.utils.assertCoordinates
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Test a player rushing as described on page 58 in the BB2025 Rulebook.
 *
 * Note, any skills that affect rushes are tested in their own test class.
 * This class only tests the basic functionality.
 */
class RushingTests: JervisGameBB2025Test() {

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
            *rushRoll(1.d6), // Fail Rush
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
        player.assertCoordinates(14, 5)
        assertEquals(MoveAction.SelectMoveType, controller.currentNode())
    }

    @Test
    fun rushNotAvailableAtTheBeginningOfMove() {
        controller.rollForward(
            PlayerSelected("A1".playerId),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.STANDARD),
        )
        val targets = controller.getAvailableActions().get<SelectFieldLocation>()
        assertTrue(targets.squares.none { it.type == TargetSquare.Type.RUSH })
    }
}
