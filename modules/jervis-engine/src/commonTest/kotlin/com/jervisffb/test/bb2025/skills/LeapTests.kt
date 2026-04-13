package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectFieldLocation
import com.jervisffb.engine.actions.SelectMoveType
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.context.LeapRollContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.bb2025.skills.Leap
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
import com.jervisffb.test.utils.putProne
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Class testing usage of the [Leap] skill.
 */

class LeapTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun canLeapOverAllAdjacentSquares() {
        awayTeam["A1".playerId].addSkill(SkillType.LEAP)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.LEAP),
        )
        val availableCoordinates = controller.getAvailableActions().get<SelectFieldLocation>().squares.map { it.coordinate }
        val expectedCoordinates = setOf(
            FieldCoordinate(13, 3),
            FieldCoordinate(14, 3),
            FieldCoordinate(15, 3),
            FieldCoordinate(15, 4),
            FieldCoordinate(15, 5),
            FieldCoordinate(15, 6),
            FieldCoordinate(15, 7),
            FieldCoordinate(14, 7),
            FieldCoordinate(11, 7),
            FieldCoordinate(11, 6),
            FieldCoordinate(11, 5),
            FieldCoordinate(11, 4),
            FieldCoordinate(11, 3),
            FieldCoordinate(12, 3),
        )
        assertEquals(expectedCoordinates.size, availableCoordinates.size)
        availableCoordinates.forEach {
            assertTrue(expectedCoordinates.contains(it), "Failed to find coordinate $it")
        }
    }

    @Test
    fun modifiersWhenLeavingSquare() {
        val leapingPlayer = state.getPlayerById("A1".playerId)
        leapingPlayer.addSkill(SkillType.LEAP)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.MOVE)
        )
        assertEquals(6, leapingPlayer.movesLeft)
        assertEquals(2, leapingPlayer.rushesLeft)
        controller.rollForward(
            MoveTypeSelected(MoveType.LEAP),
            FieldSquareSelected(11, 4),
            Cancel, // Do not use Leap modifier
            4.d6, // 2 Modifiers from leaving, no to enter, so should fail
        )
        assertFalse(state.getContext<LeapRollContext>().isSuccess)
        controller.rollForward(
            TeamRerollSelected<RegularTeamReroll>(),
            5.d6,
        )
        assertEquals(4, leapingPlayer.movesLeft)
        assertEquals(2, leapingPlayer.rushesLeft)
        leapingPlayer.assertStanding()
        leapingPlayer.assertCoordinates(11, 4)
    }

    @Test
    fun modifiersWhenEnteringSquare() {
        state.getPlayerById("H1".playerId).state = PlayerState.PRONE
        state.getPlayerById("H2".playerId).state = PlayerState.PRONE
        val leapingPlayer = state.getPlayerById("A1".playerId)
        leapingPlayer.addSkill(SkillType.LEAP)
        controller.rollForward(
            PlayerSelected(leapingPlayer.id),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.LEAP),
            FieldSquareSelected(11, 6),
            3.d6, // -1 Marked Modifiers from entering, so a 3 will fail
        )
        assertFalse(state.getContext<LeapRollContext>().isSuccess)
        controller.rollForward(
            TeamRerollSelected<RegularTeamReroll>(),
            4.d6,
        )
        leapingPlayer.assertStanding()
        leapingPlayer.assertCoordinates(11, 6)
    }

    @Test
    fun useLargestModifier() {
        state.getPlayerById("H1".playerId).state = PlayerState.PRONE
        state.getPlayerById("H2".playerId).state = PlayerState.PRONE
        val leapingPlayer = state.getPlayerById("A2".playerId)
        leapingPlayer.addSkill(SkillType.LEAP)
        controller.rollForward(
            PlayerSelected(leapingPlayer.id),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.LEAP),
            FieldSquareSelected(11, 7),
            Cancel, // Do not use Leap modifier
            4.d6, // 1 Marked Modifiers from leaving, 2 from entering
        )
        assertFalse(state.getContext<LeapRollContext>().isSuccess)
        controller.rollForward(
            TeamRerollSelected<RegularTeamReroll>(),
            6.d6,
        )
        leapingPlayer.assertStanding()
        leapingPlayer.assertCoordinates(11, 7)
    }

    @Test
    fun fallOverOnFailedLeap() {
        val leapingPlayer = state.getPlayerById("A1".playerId)
        leapingPlayer.addSkill(SkillType.LEAP)
        controller.rollForward(
            PlayerSelected(leapingPlayer.id),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.LEAP),
            FieldSquareSelected(11, 5),
            Cancel, // Do not use Leap modifier
            3.d6, // -2 Marked Modifiers from leaving/entering
            NoRerollSelected(),
        )
        leapingPlayer.assertFallenOver()
        leapingPlayer.assertCoordinates(11, 5)
    }

    @Test
    fun fallOverInStartingSquareWhenRollingOne() {
        val leapingPlayer = state.getPlayerById("A1".playerId)
        leapingPlayer.addSkill(SkillType.LEAP)
        controller.rollForward(
            PlayerSelected(leapingPlayer.id),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.LEAP),
            FieldSquareSelected(11, 5),
            Cancel, // Do not use Leap modifier
            1.d6,
            NoRerollSelected(),
        )
        leapingPlayer.assertFallenOver()
        leapingPlayer.assertCoordinates(13, 5)
    }

    @Test
    fun rushToLeap() {
        val leapingPlayer = state.getPlayerById("A1".playerId)
        leapingPlayer.addSkill(SkillType.LEAP)

        // Adjust move so player needs 1 rush to leap
        leapingPlayer.movesLeft = 1
        leapingPlayer.rushesLeft = 2

        controller.rollForward(
            PlayerSelected(leapingPlayer.id),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.LEAP),
            FieldSquareSelected(11, 5),
            *rushRoll(2.d6),
            Cancel, // Do not use Leap modifier
            5.d6, // Leap
            NoRerollSelected(),
        )
        assertEquals(0, leapingPlayer.movesLeft)
        assertEquals(1, leapingPlayer.rushesLeft)
        leapingPlayer.assertStanding()
        leapingPlayer.assertCoordinates(11, 5)

    }

    @Test
    fun rushTwiceToLeap() {
        val leapingPlayer = state.getPlayerById("A1".playerId)
        leapingPlayer.addSkill(SkillType.LEAP)

        // Adjust move so player needs 2 rush to leap
        leapingPlayer.movesLeft = 0
        leapingPlayer.rushesLeft = 2

        controller.rollForward(
            PlayerSelected(leapingPlayer.id),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.LEAP),
            FieldSquareSelected(11, 5),
            *rushRoll(2.d6),
            *rushRoll(2.d6),
            Cancel, // Do not use Leap modifier
            5.d6, // Leap
            NoRerollSelected(),
        )
        assertEquals(0, leapingPlayer.movesLeft)
        assertEquals(0, leapingPlayer.rushesLeft)
        leapingPlayer.assertStanding()
        leapingPlayer.assertCoordinates(11, 5)
    }

    @Test
    fun fallOverInStartingSquareWhenFailingFirstRush() {
        val leapingPlayer = state.getPlayerById("A1".playerId)
        leapingPlayer.addSkill(SkillType.LEAP)

        // Adjust move so player needs 2 rush to leap
        leapingPlayer.movesLeft = 0
        leapingPlayer.rushesLeft = 2

        controller.rollForward(
            PlayerSelected(leapingPlayer.id),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.LEAP),
            FieldSquareSelected(11, 5),
            *rushRoll(1.d6),
        )
        leapingPlayer.assertFallenOver()
        leapingPlayer.assertCoordinates(13, 5)
    }

    @Test
    fun fallOverInStartingSquareWhenFailingSecondRush() {
        val leapingPlayer = state.getPlayerById("A1".playerId)
        leapingPlayer.addSkill(SkillType.LEAP)

        // Adjust move so player needs 2 rush to leap
        leapingPlayer.movesLeft = 0
        leapingPlayer.rushesLeft = 2

        controller.rollForward(
            PlayerSelected(leapingPlayer.id),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.LEAP),
            FieldSquareSelected(11, 5),
        )

        controller.rollForward(
            *rushRoll(2.d6),
            *rushRoll(1.d6),
        )
        leapingPlayer.assertFallenOver()
        leapingPlayer.assertCoordinates(13, 5)
    }

    @Test
    fun cannotLeapIfNotEnoughMove() {
        val leapingPlayer = state.getPlayerById("A1".playerId)
        leapingPlayer.addSkill(SkillType.LEAP)
        controller.rollForward(
            PlayerSelected(leapingPlayer.id),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
        )
        leapingPlayer.movesLeft = 0
        leapingPlayer.rushesLeft = 1
        val moveTypes = controller.getAvailableActions()
            .filterIsInstance<SelectMoveType>()
            .first()
            .types
        assertFalse(moveTypes.contains(MoveType.LEAP))
    }

    @Test
    fun leapModifierIsOnlyAvailableIfEnoughNegativeModifiers() {
        val leapingPlayer = state.getPlayerById("A1".playerId)
        leapingPlayer.addSkill(SkillType.LEAP)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.LEAP),
            FieldSquareSelected(11, 4),
            Confirm, // Use Leap modifier
            3.d6, // 2 Modifiers from leaving
        )
        assertFalse(state.getContext<LeapRollContext>().isSuccess)
        controller.rollForward(
            TeamRerollSelected<RegularTeamReroll>(),
            4.d6,
        )
        leapingPlayer.assertStanding()
        leapingPlayer.assertCoordinates(11, 4)

    }

    @Test
    fun skipLeapModifierIfOnlyOneMark() {
        homeTeam["H2".playerId].putProne()
        val leapingPlayer = state.getPlayerById("A1".playerId)
        leapingPlayer.addSkill(SkillType.LEAP)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.LEAP),
            FieldSquareSelected(11, 4),
            3.d6, // 1 Modifiers from leaving/entering. Leap modifier cannot be applied
        )
        assertFalse(state.getContext<LeapRollContext>().isSuccess)
        controller.rollForward(
            TeamRerollSelected<RegularTeamReroll>(),
            4.d6,
        )
        leapingPlayer.assertStanding()
        leapingPlayer.assertCoordinates(11, 4)
    }
}
