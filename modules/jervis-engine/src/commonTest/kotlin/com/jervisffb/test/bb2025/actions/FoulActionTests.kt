package com.jervisffb.test.bb2025.actions

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.Availability
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.locations.DogOut
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.SmartMoveTo
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.moveTo
import com.jervisffb.test.utils.firstInstanceOf
import com.jervisffb.test.utils.firstInstanceOfOrNull
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Class responsible for testing the Foul Action as described on page 69 in the
 * BB2025 rulebook.
 *
 * Argue the call is tested in [com.jervisffb.test.bb2025.tables.ArgueTheCallTests]
 * Turnovers are tested in [com.jervisffb.test.bb2025.TurnOverTests]
 */
class FoulActionTests: JervisGameBB2025Test() {

    @Test
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun selectPronePlayer() {
        homeTeam["H1".playerId].state = PlayerState.PRONE
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.FOUL),
        )
        val targets = controller.getAvailableActions().firstInstanceOf<SelectPlayer>()
        assertEquals(1, targets.size)
        assertEquals("H1".playerId, targets.players.first())
    }

    @Test
    fun selectStunnedPlayer() {
        homeTeam["H1".playerId].state = PlayerState.STUNNED
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.FOUL),
        )
        val targets = controller.getAvailableActions().firstInstanceOf<SelectPlayer>()
        assertEquals(1, targets.size)
        assertEquals("H1".playerId, targets.players.first())
    }

    @Test
    fun cannotSelectTargetOnTheDistance() {
        homeTeam["H5".playerId].state = PlayerState.STUNNED
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.FOUL),
        )
        val targets = controller.getAvailableActions().firstInstanceOfOrNull<SelectPlayer>()
        assertNull(targets)
    }

    @Test
    fun canFoulAllEligibleTargets() {
        homeTeam["H1".playerId].state = PlayerState.PRONE
        homeTeam["H2".playerId].state = PlayerState.STUNNED
        assertEquals(1, awayTeam.turnData.foulActions)
        controller.rollForward(
            *activatePlayer("A6", PlayerStandardActionType.FOUL),
            SmartMoveTo(11, 5),
        )

        val targets = controller.getAvailableActions().firstInstanceOf<SelectPlayer>()
        assertEquals(2, targets.size)
        assertTrue(targets.players.contains("H1".playerId))
        assertTrue(targets.players.contains("H2".playerId))
    }

    @Test
    fun canCancelActionBeforeMovingOrFouling() {
        homeTeam["H1".playerId].state = PlayerState.STUNNED
        assertEquals(1, awayTeam.turnData.foulActions)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.FOUL),
        )
        assertEquals(awayTeam["A1".playerId], state.activePlayer)
        controller.rollForward(
            EndAction
        )
        assertEquals(1, awayTeam.turnData.foulActions)
        assertNull(state.activePlayer)
        assertEquals(Availability.AVAILABLE, awayTeam["A1".playerId].available)
    }

    @Test
    fun useActionWhenMovingButNotFouling() {
        homeTeam["H1".playerId].state = PlayerState.STUNNED
        assertEquals(1, awayTeam.turnData.foulActions)
        controller.rollForward(
            *activatePlayer("A6", PlayerStandardActionType.FOUL),
            SmartMoveTo(13, 4),
            EndAction // Move next to the prone player, but do not foul and just end the action
        )
        assertEquals(0, awayTeam.turnData.foulActions)
        assertEquals(Availability.HAS_ACTIVATED, awayTeam["A6".playerId].available)
    }

    @Test
    fun offensiveAssists() {
        homeTeam["H1".playerId].state = PlayerState.PRONE
        homeTeam["H2".playerId].state = PlayerState.STUNNED
        assertEquals(1, awayTeam.turnData.foulActions)
        controller.rollForward(
            *activatePlayer("A6", PlayerStandardActionType.FOUL),
            SmartMoveTo(13, 4),
            PlayerSelected("H1".playerId), // Start foul, A1 can assist
            DiceRollResults(5.d6, 3.d6), // 8 + 1 = Armor break
            DiceRollResults(1.d6, 2.d6), // Stunned
        )
        assertEquals(PlayerState.STUNNED, homeTeam["H1".playerId].state)
    }

    @Test
    fun defensiveAssists() {
        awayTeam["A1".playerId].state = PlayerState.PRONE
        awayTeam["A2".playerId].state = PlayerState.PRONE
        awayTeam["A3".playerId].state = PlayerState.PRONE
        homeTeam["H2".playerId].state = PlayerState.PRONE
        assertEquals(1, awayTeam.turnData.foulActions)
        controller.rollForward(
            *activatePlayer("A6", PlayerStandardActionType.FOUL),
            SmartMoveTo(11, 5),
            PlayerSelected("H2".playerId), // Start foul, H1 can assist
            DiceRollResults(5.d6, 4.d6), // 9 - 1 = Fail armour break
        )
        assertNull(state.activePlayer)
        assertEquals(PlayerState.PRONE, homeTeam["H2".playerId].state)
    }

    @Test
    fun sentOffDoubleArmourRoll() {
        homeTeam["H1".playerId].state = PlayerState.PRONE
        assertEquals(1, awayTeam.turnData.foulActions)
        assertEquals(awayTeam, state.activeTeam)
        controller.rollForward(
            *activatePlayer("A6", PlayerStandardActionType.FOUL),
            SmartMoveTo(13, 4),
            PlayerSelected("H1".playerId), // Start foul
            DiceRollResults(2.d6, 2.d6), // Roll double -> Sent off
            Cancel // Do not argue the call
        )
        assertEquals(PlayerState.PRONE, homeTeam["H1".playerId].state)
        assertEquals(homeTeam, state.activeTeam)
        assertEquals(PlayerState.BANNED, awayTeam["A6".playerId].state)
        assertEquals(DogOut, awayTeam["A6".playerId].location)
    }

    @Test
    fun sentOffDoubleInjuryRoll() {
        homeTeam["H1".playerId].state = PlayerState.PRONE
        assertEquals(1, awayTeam.turnData.foulActions)
        assertEquals(awayTeam, state.activeTeam)
        controller.rollForward(
            *activatePlayer("A6", PlayerStandardActionType.FOUL),
            SmartMoveTo(13, 4),
            PlayerSelected("H1".playerId), // Start foul
            DiceRollResults(5.d6, 6.d6), // Break armour
            DiceRollResults(2.d6, 2.d6), // Roll double on injury
            Cancel // Do not argue the call
        )
        assertEquals(PlayerState.STUNNED, homeTeam["H1".playerId].state)
        assertEquals(homeTeam, state.activeTeam)
        assertEquals(PlayerState.BANNED, awayTeam["A6".playerId].state)
        assertEquals(DogOut, awayTeam["A6".playerId].location)
    }

    @Test
    fun actionEndsAfterFoul() {
        homeTeam["H1".playerId].state = PlayerState.PRONE
        assertEquals(1, awayTeam.turnData.foulActions)
        controller.rollForward(
            *activatePlayer("A6", PlayerStandardActionType.FOUL),
        )
        val player = awayTeam["A6".playerId]
        assertEquals(player, state.activePlayer)
        assertEquals(Availability.IS_ACTIVE, player.available)
        controller.rollForward(
            SmartMoveTo(13, 4),
            PlayerSelected("H1".playerId),
            DiceRollResults(5.d6, 6.d6), // 8 + 1 = Armor break
            DiceRollResults(1.d6, 2.d6), // Stunned -> Foul ends
        )
        assertEquals(0, awayTeam.turnData.foulActions)
        assertNull(state.activePlayer)
        assertEquals(Availability.HAS_ACTIVATED, player.available)
    }

    @Test
    fun foulDoesNotRequireMove() {
        homeTeam["H1".playerId].state = PlayerState.PRONE
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.FOUL),
            PlayerSelected("H1".playerId), // Start foul
            DiceRollResults(1.d6, 2.d6), // Armour roll
        )
        assertEquals(0, awayTeam.turnData.foulActions)
        assertNull(state.activePlayer)
    }

    @Test
    fun ballBounceIfBanned() {
        homeTeam["H1".playerId].state = PlayerState.PRONE
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.FOUL),
            *moveTo(17, 7),
            4.d6, // Pickup
            NoRerollSelected(),
            SmartMoveTo(13, 4),
            PlayerSelected("H1".playerId),
            DiceRollResults(1.d6, 1.d6), // Armour roll fail
            Cancel, // Do not argue the call
            5.d8, // Ball bounce
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        assertEquals(FieldCoordinate(14, 4), state.singleBall().location)
        assertFalse(awayTeam["A10".playerId].hasBall())
    }
}
