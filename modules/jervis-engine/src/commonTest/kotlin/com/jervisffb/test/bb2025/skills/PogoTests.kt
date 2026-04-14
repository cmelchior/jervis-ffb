package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PitchSquareSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectMoveType
import com.jervisffb.engine.actions.SelectPitchLocation
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.context.PogoRollContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.bb2025.skills.Pogo
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.rerolls.RegularTeamReroll
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.rushRoll
import com.jervisffb.test.utils.TeamRerollSelected
import com.jervisffb.test.utils.assertCoordinates
import com.jervisffb.test.utils.assertFallenOver
import com.jervisffb.test.utils.assertStanding
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Class testing usage of the [Pogo] skill.
 */

class PogoTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun canPogoOverAllAdjacentSquares() {
        awayTeam["A1".playerId].addSkill(SkillType.POGO_STICK)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.POGO),
        )
        val availableCoordinates = controller.getAvailableActions().get<SelectPitchLocation>().squares.map { it.coordinate }
        val expectedCoordinates = setOf(
            PitchCoordinate(13, 3),
            PitchCoordinate(14, 3),
            PitchCoordinate(15, 3),
            PitchCoordinate(15, 4),
            PitchCoordinate(15, 5),
            PitchCoordinate(15, 6),
            PitchCoordinate(15, 7),
            PitchCoordinate(14, 7),
            PitchCoordinate(11, 7),
            PitchCoordinate(11, 6),
            PitchCoordinate(11, 5),
            PitchCoordinate(11, 4),
            PitchCoordinate(11, 3),
            PitchCoordinate(12, 3),
        )
        assertEquals(expectedCoordinates.size, availableCoordinates.size)
        availableCoordinates.forEach {
            assertTrue(expectedCoordinates.contains(it), "Failed to find coordinate $it")
        }
    }

    @Test
    fun noModifiersWhenLeavingSquare() {
        val pogoingPlayer = state.getPlayerById("A1".playerId)
        pogoingPlayer.addSkill(SkillType.POGO_STICK)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.MOVE)
        )
        assertEquals(6, pogoingPlayer.movesLeft)
        assertEquals(2, pogoingPlayer.rushesLeft)
        controller.rollForward(
            MoveTypeSelected(MoveType.POGO),
            PitchSquareSelected(11, 4),
            2.d6, // 2 Marks from leaving
        )
        assertTrue(state.getContext<PogoRollContext>().modifiers.none { it.modifier < 0 })
        assertFalse(state.getContext<PogoRollContext>().isSuccess)
        controller.rollForward(
            TeamRerollSelected<RegularTeamReroll>(),
            3.d6,
        )
        assertEquals(4, pogoingPlayer.movesLeft)
        assertEquals(2, pogoingPlayer.rushesLeft)
        pogoingPlayer.assertStanding()
        pogoingPlayer.assertCoordinates(11, 4)
    }

    @Test
    fun noModifiersWhenEnteringSquare() {
        state.getPlayerById("H1".playerId).state = PlayerState.PRONE
        state.getPlayerById("H2".playerId).state = PlayerState.PRONE
        val pogoingPlayer = state.getPlayerById("A1".playerId)
        pogoingPlayer.addSkill(SkillType.POGO_STICK)
        controller.rollForward(
            PlayerSelected(pogoingPlayer.id),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.POGO),
            PitchSquareSelected(11, 6),
            2.d6, // 1 Mark when entering
        )
        assertTrue(state.getContext<PogoRollContext>().modifiers.none { it.modifier < 0 })
        assertFalse(state.getContext<PogoRollContext>().isSuccess)
        controller.rollForward(
            TeamRerollSelected<RegularTeamReroll>(),
            3.d6,
        )
        pogoingPlayer.assertStanding()
        pogoingPlayer.assertCoordinates(11, 6)
    }

    @Test
    fun fallOverOnFailedPogo() {
        val pogoingPlayer = state.getPlayerById("A1".playerId)
        pogoingPlayer.addSkill(SkillType.POGO_STICK)
        controller.rollForward(
            PlayerSelected(pogoingPlayer.id),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.POGO),
            PitchSquareSelected(11, 5),
            2.d6,
            NoRerollSelected(),
        )
        pogoingPlayer.assertFallenOver()
        pogoingPlayer.assertCoordinates(11, 5)
    }

    @Test
    fun fallOverInStartingSquareWhenRollingOne() {
        val pogoingPlayer = state.getPlayerById("A1".playerId)
        pogoingPlayer.addSkill(SkillType.POGO_STICK)
        controller.rollForward(
            PlayerSelected(pogoingPlayer.id),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.POGO),
            PitchSquareSelected(11, 5),
            1.d6,
            NoRerollSelected(),
        )
        pogoingPlayer.assertFallenOver()
        pogoingPlayer.assertCoordinates(13, 5)
    }

    @Test
    fun rushToPogo() {
        val pogoingPlayer = state.getPlayerById("A1".playerId)
        pogoingPlayer.addSkill(SkillType.POGO_STICK)

        // Adjust move so player needs 1 rush to pogo
        pogoingPlayer.movesLeft = 1
        pogoingPlayer.rushesLeft = 2

        controller.rollForward(
            PlayerSelected(pogoingPlayer.id),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.POGO),
            PitchSquareSelected(11, 5),
            *rushRoll(2.d6),
            3.d6, // Pogo
            NoRerollSelected(),
        )
        assertEquals(0, pogoingPlayer.movesLeft)
        assertEquals(1, pogoingPlayer.rushesLeft)
        pogoingPlayer.assertStanding()
        pogoingPlayer.assertCoordinates(11, 5)

    }

    @Test
    fun rushTwiceToPogo() {
        val pogoingPlayer = state.getPlayerById("A1".playerId)
        pogoingPlayer.addSkill(SkillType.POGO_STICK)

        // Adjust move so player needs 2 rush to pogo
        pogoingPlayer.movesLeft = 0
        pogoingPlayer.rushesLeft = 2

        controller.rollForward(
            PlayerSelected(pogoingPlayer.id),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.POGO),
            PitchSquareSelected(11, 5),
            *rushRoll(2.d6),
            *rushRoll(2.d6),
            3.d6, // Pogo
            NoRerollSelected(),
        )
        assertEquals(0, pogoingPlayer.movesLeft)
        assertEquals(0, pogoingPlayer.rushesLeft)
        pogoingPlayer.assertStanding()
        pogoingPlayer.assertCoordinates(11, 5)
    }

    @Test
    fun fallOverInStartingSquareWhenFailingFirstRush() {
        val pogoingPlayer = state.getPlayerById("A1".playerId)
        pogoingPlayer.addSkill(SkillType.POGO_STICK)

        // Adjust move so player needs 2 rush to pogo
        pogoingPlayer.movesLeft = 0
        pogoingPlayer.rushesLeft = 2

        controller.rollForward(
            PlayerSelected(pogoingPlayer.id),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.POGO),
            PitchSquareSelected(11, 5),
            *rushRoll(1.d6),
        )
        pogoingPlayer.assertFallenOver()
        pogoingPlayer.assertCoordinates(13, 5)
    }

    @Test
    fun fallOverInStartingSquareWhenFailingSecondRush() {
        val pogoingPlayer = state.getPlayerById("A1".playerId)
        pogoingPlayer.addSkill(SkillType.POGO_STICK)

        // Adjust move so player needs 2 rush to pogo
        pogoingPlayer.movesLeft = 0
        pogoingPlayer.rushesLeft = 2

        controller.rollForward(
            PlayerSelected(pogoingPlayer.id),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.POGO),
            PitchSquareSelected(11, 5),
        )

        controller.rollForward(
            *rushRoll(2.d6),
            *rushRoll(1.d6),
        )
        pogoingPlayer.assertFallenOver()
        pogoingPlayer.assertCoordinates(13, 5)
    }

    @Test
    fun cannotPogoIfNotEnoughMove() {
        val pogoingPlayer = state.getPlayerById("A1".playerId)
        pogoingPlayer.addSkill(SkillType.POGO_STICK)
        controller.rollForward(
            PlayerSelected(pogoingPlayer.id),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
        )
        pogoingPlayer.movesLeft = 0
        pogoingPlayer.rushesLeft = 1
        val moveTypes = controller.getAvailableActions()
            .filterIsInstance<SelectMoveType>()
            .first()
            .types
        assertFalse(moveTypes.contains(MoveType.POGO))
    }
}
