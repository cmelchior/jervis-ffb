package com.jervisffb.test.bb2025.actions

import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.RerollOptionSelected
import com.jervisffb.engine.actions.SelectFieldLocation
import com.jervisffb.engine.actions.SelectMoveType
import com.jervisffb.engine.actions.SelectRerollOption
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.context.JumpRollContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.RegularTeamReroll
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.ext.undoActions
import com.jervisffb.test.jump
import com.jervisffb.test.rushRoll
import com.jervisffb.test.utils.SelectTeamReroll
import com.jervisffb.test.utils.assertCoordinates
import com.jervisffb.test.utils.assertStanding
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Test a player standing up as described on page 45 in the BB2020 Rulebook.
 *
 * Note, any skills that affect dodges are testing in their own test class.
 * This class only tests the basic functionality.
 */
class JumpTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    // Check all player states we should be able to jump over
    @Test
    fun jumpOverPlayer() {
        val entries = PlayerState.entries
        for (i in entries.indices) {
            val playerState = entries[i]
            val canJump = when (playerState) {
                PlayerState.PRONE,
                PlayerState.STUNNED,
                PlayerState.STUNNED_OWN_TURN -> true

                else -> false
            }
            if (!canJump) continue

            state.getPlayerById("H1".playerId).state = playerState
            val jumpingPlayer = state.getPlayerById("A1".playerId)
            controller.rollForward(
                *activatePlayer(jumpingPlayer, PlayerStandardActionType.MOVE),
                MoveTypeSelected(MoveType.JUMP),
                FieldSquareSelected(11, 5),
                *jump(4.d6),
            )
            jumpingPlayer.assertStanding()
            jumpingPlayer.assertCoordinates(11, 5)
            controller.undoActions(6)
        }
    }

    @Test
    fun legalSquares_straightJump() {
        state.getPlayerById("H1".playerId).state = PlayerState.PRONE
        val jumpingPlayer = state.getPlayerById("A1".playerId)
        controller.rollForward(
            *activatePlayer(jumpingPlayer, PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.JUMP),
        )

        val targetCoordinates = controller.getAvailableActions().actions
            .filterIsInstance<SelectFieldLocation>()
            .first().squares
            .map { it.coordinate }
            .toSet()

        val expectedCoordinates = setOf(
            FieldCoordinate(11, 4),
            FieldCoordinate(11, 5),
            FieldCoordinate(11, 6)
        )
        assertTrue(expectedCoordinates.containsAll(targetCoordinates))
        assertTrue(targetCoordinates.containsAll(targetCoordinates))
    }

    @Test
    fun legalSquares_diagonalJump() {
        state.getPlayerById("H1".playerId).state = PlayerState.PRONE
        val jumpingPlayer = state.getPlayerById("A2".playerId)
        controller.rollForward(
            *activatePlayer(jumpingPlayer, PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.JUMP),
        )

        val targetCoordinates = controller.getAvailableActions().actions
            .filterIsInstance<SelectFieldLocation>()
            .first().squares
            .map { it.coordinate }
            .toSet()

        val expectedCoordinates = setOf(
            FieldCoordinate(12, 4),
            FieldCoordinate(11, 4),
            FieldCoordinate(11, 5)
        )
        assertTrue(expectedCoordinates.containsAll(targetCoordinates))
        assertTrue(targetCoordinates.containsAll(targetCoordinates))
    }

    @Test
    fun modifiersWhenLeavingSquare() {
        state.getPlayerById("H1".playerId).state = PlayerState.PRONE
        val jumpingPlayer = state.getPlayerById("A2".playerId)
        controller.rollForward(
            *activatePlayer(jumpingPlayer, PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.JUMP),
            FieldSquareSelected(11, 4),
            4.d6, // 2 Modifiers from leaving, no to enter, so should fail
        )
        assertFalse(state.getContext<JumpRollContext>().isSuccess)
        controller.rollForward(
            SelectTeamReroll<RegularTeamReroll>(),
            5.d6,
        )
        jumpingPlayer.assertStanding()
        jumpingPlayer.assertCoordinates(11, 4)
    }

    @Test
    fun modifiersWhenEnteringSquare() {
        state.getPlayerById("H1".playerId).state = PlayerState.PRONE
        state.getPlayerById("H2".playerId).state = PlayerState.PRONE
        val jumpingPlayer = state.getPlayerById("A1".playerId)
        controller.rollForward(
            *activatePlayer(jumpingPlayer, PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.JUMP),
            FieldSquareSelected(11, 6),
            4.d6, // 1 Marked Modifiers from entering, so a 4 will fail
        )
        val reroll = RerollOptionSelected(
            option = (controller.getAvailableActions().actions.last() as SelectRerollOption).options.first()
        )
        controller.rollForward(
            reroll,
            5.d6,
        )
        jumpingPlayer.assertStanding()
        jumpingPlayer.assertCoordinates(11, 6)
    }

    @Test
    fun useLargestModifier() {
        state.getPlayerById("H1".playerId).state = PlayerState.PRONE
        state.getPlayerById("H2".playerId).state = PlayerState.PRONE
        val jumpingPlayer = state.getPlayerById("A2".playerId)
        controller.rollForward(
            *activatePlayer(jumpingPlayer, PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.JUMP),
            FieldSquareSelected(11, 7),
            4.d6, // 1 Marked Modifiers from leaving, 2 from entering
        )
        assertFalse(state.getContext<JumpRollContext>().isSuccess)
        controller.rollForward(
            SelectTeamReroll<RegularTeamReroll>(),
            5.d6,
        )
        jumpingPlayer.assertStanding()
        jumpingPlayer.assertCoordinates(11, 7)
    }

    @Test
    fun fallOverOnFailedJump() {
        state.getPlayerById("H1".playerId).state = PlayerState.PRONE
        val jumpingPlayer = state.getPlayerById("A1".playerId)
        controller.rollForward(
            *activatePlayer(jumpingPlayer, PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.JUMP),
            FieldSquareSelected(11, 5),
            *jump(3.d6), // 1 Marked Modifiers from leaving/entering
        )
        assertEquals(PlayerState.FALLEN_OVER, jumpingPlayer.state)
        jumpingPlayer.assertCoordinates(11, 5)
    }

    @Test
    fun fallOverInStartingSquareWhenRollingOne() {
        state.getPlayerById("H1".playerId).state = PlayerState.PRONE
        val jumpingPlayer = state.getPlayerById("A1".playerId)
        controller.rollForward(
            *activatePlayer(jumpingPlayer, PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.JUMP),
            FieldSquareSelected(11, 5),
            *jump(1.d6),
        )
        assertEquals(PlayerState.FALLEN_OVER, jumpingPlayer.state)
        jumpingPlayer.assertCoordinates(13, 5)
    }

    @Test
    fun rushToJump() {
        state.getPlayerById("H1".playerId).state = PlayerState.PRONE
        val jumpingPlayer = state.getPlayerById("A1".playerId)

        // Adjust move so jumping player needs 1 rush to jump
        jumpingPlayer.movesLeft = 1
        jumpingPlayer.rushesLeft = 2

        controller.rollForward(
            *activatePlayer(jumpingPlayer, PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.JUMP),
            FieldSquareSelected(11, 5),
            *rushRoll(2.d6),
            4.d6, // Jump
            NoRerollSelected(),
        )
        jumpingPlayer.assertStanding()
        jumpingPlayer.assertCoordinates(11, 5)

    }

    @Test
    fun rushTwiceToJump() {
        state.getPlayerById("H1".playerId).state = PlayerState.PRONE
        val jumpingPlayer = state.getPlayerById("A1".playerId)

        // Adjust move so jumping player needs 2 rush to jump
        jumpingPlayer.movesLeft = 0
        jumpingPlayer.rushesLeft = 2

        controller.rollForward(
            *activatePlayer(jumpingPlayer, PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.JUMP),
            FieldSquareSelected(11, 5),
            *rushRoll(2.d6),
            *rushRoll(2.d6),
            4.d6, // Jump
            NoRerollSelected(),
        )
        jumpingPlayer.assertStanding()
        jumpingPlayer.assertCoordinates(11, 5)
    }

    // According to Designer's Commentary, failing the first Rush will leave
    // you in the starting square (since the middle square is taken up by a
    // prone player)
    @Test
    fun fallOverInStartingSquareWhenFailingFirstRush() {
        state.getPlayerById("H1".playerId).state = PlayerState.PRONE
        val jumpingPlayer = state.getPlayerById("A1".playerId)

        // Adjust move so jumping player needs 2 rush to jump
        jumpingPlayer.movesLeft = 0
        jumpingPlayer.rushesLeft = 2

        controller.rollForward(
            *activatePlayer(jumpingPlayer, PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.JUMP),
            FieldSquareSelected(11, 5),
            *rushRoll(1.d6),
        )
        assertEquals(PlayerState.FALLEN_OVER, jumpingPlayer.state)
        jumpingPlayer.assertCoordinates(13, 5)
    }

    @Test
    fun fallOverInStartingSquareWhenFailingSecondRush() {
        state.getPlayerById("H1".playerId).state = PlayerState.PRONE
        val jumpingPlayer = state.getPlayerById("A1".playerId)

        // Adjust move so jumping player needs 2 rush to jump
        jumpingPlayer.movesLeft = 0
        jumpingPlayer.rushesLeft = 2

        controller.rollForward(
            *activatePlayer(jumpingPlayer, PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.JUMP),
            FieldSquareSelected(11, 5),
            *rushRoll(2.d6),
            *rushRoll(1.d6)
        )
        assertEquals(PlayerState.FALLEN_OVER, jumpingPlayer.state)
        jumpingPlayer.assertCoordinates(13, 5)
    }

    @Test
    fun cannotJumpOverStandingPlayer() {
        val jumpingPlayer = state.getPlayerById("A1".playerId)
        controller.rollForward(
            PlayerSelected(jumpingPlayer.id),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
        )

        val moveTypes = controller.getAvailableActions()
            .filterIsInstance<SelectMoveType>()
            .first()
            .types

        assertFalse(moveTypes.contains(MoveType.JUMP))
    }

    @Test
    fun cannotJumpIfNotEnoughMove() {
        state.getPlayerById("H1".playerId).state = PlayerState.PRONE
        val jumpingPlayer = state.getPlayerById("A1".playerId)
        controller.rollForward(
            PlayerSelected(jumpingPlayer.id),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
        )
        jumpingPlayer.movesLeft = 0
        jumpingPlayer.rushesLeft = 1
        val moveTypes = controller.getAvailableActions()
            .filterIsInstance<SelectMoveType>()
            .first()
            .types
        assertFalse(moveTypes.contains(MoveType.JUMP))
    }
}
