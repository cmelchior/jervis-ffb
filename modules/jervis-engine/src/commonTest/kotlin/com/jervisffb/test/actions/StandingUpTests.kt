package com.jervisffb.test.actions

import com.jervisffb.engine.GameController
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.Undo
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.PlayerStandardActionType
import com.jervisffb.test.JervisGameTest
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.moveTo
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Test a player standing up as described on page 44 in the BB2020 Rulebook.
 *
 * Note, any skills that affect dodges are testing in their own test class.
 * This class only tests the basic functionality.
 */
class StandingUpTests: JervisGameTest() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
        val player = state.getPlayerById("A10".playerId)
        player.state = PlayerState.PRONE
        player.hasTackleZones = false
    }

    @Test
    fun standingUpAndMove() {
        val player = state.getPlayerById("A10".playerId)
        controller.rollForward(
            PlayerSelected(player.id),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
        )
        assertEquals(7, player.movesLeft)
        with(controller.getAvailableActions()) {
            assertEquals(2, actionsCount)
            assertTrue(contains(EndAction))
            assertTrue(contains(MoveType.STAND_UP))
        }
        controller.rollForward(MoveTypeSelected(MoveType.STAND_UP))
        assertEquals(4, player.movesLeft)
        controller.rollForward(*moveTo(17, 7))
        assertEquals(3, player.movesLeft)
        assertEquals(player, state.activePlayer)
    }

    @Test
    fun standingUpAndEndAction() {
        val player = state.getPlayerById("A10".playerId)
        controller.rollForward(
            PlayerSelected(player.id),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
        )
        assertEquals(7, player.movesLeft)
        controller.rollForward(
            MoveTypeSelected(MoveType.STAND_UP),
            EndAction
        )
        assertEquals(4, player.movesLeft)
        assertEquals(FieldCoordinate(16, 7), player.location)
        assertEquals(Int.MAX_VALUE - 1, player.team.turnData.moveActions)
        assertFalse(player.isActive)
    }

    @Test
    fun standingUpWithMoveTwoRequiresRoll() {
        val player = state.getPlayerById("A10".playerId)
        player.baseMove = 2
        player.movesLeft = 2
        controller.rollForward(
            PlayerSelected(player.id),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.STAND_UP),
            4.d6,
            NoRerollSelected()
        )
        assertEquals(0, player.movesLeft)
        assertEquals(PlayerState.STANDING, player.state)
    }

    @Test
    fun failingStandingUpRollEndsAction() {
        // Put down opponent player (so we have a target for foul actions
        state.getPlayerById("H1".playerId).let {
            it.state = PlayerState.PRONE
            it.hasTackleZones = false
        }

        // Force player to roll to stand up
        val player = state.getPlayerById("A10".playerId)
        player.baseMove = 2
        player.movesLeft = 2

        // Just check normal actions for now
        PlayerStandardActionType.entries.forEach { action ->
            var startAction: ((GameController) -> Unit)? = null
            lateinit var checkActionUsed: () -> Unit
            lateinit var rollBack: () -> Int
            when (action) {
                PlayerStandardActionType.MOVE -> {
                    startAction = { controller ->
                        controller.rollForward(PlayerActionSelected(PlayerStandardActionType.MOVE))
                    }
                    checkActionUsed = { assertEquals(Int.MAX_VALUE - 1, player.team.turnData.moveActions) }
                    rollBack = { 5 }
                }
                PlayerStandardActionType.PASS -> {
                    startAction = { controller ->
                        controller.rollForward(PlayerActionSelected(PlayerStandardActionType.PASS))
                    }
                    checkActionUsed = { assertEquals(0, player.team.turnData.passActions) }
                    rollBack = { 5 }
                }
                PlayerStandardActionType.HAND_OFF -> {
                    startAction = { controller ->
                        controller.rollForward(PlayerActionSelected(PlayerStandardActionType.HAND_OFF))
                    }
                    checkActionUsed = { assertEquals(0, player.team.turnData.handOffActions) }
                    rollBack = { 5 }
                }
                PlayerStandardActionType.BLOCK -> {
                    // Player cannot block while prone (requires Jump Up)
                    startAction = null
                }
                PlayerStandardActionType.BLITZ -> {
                    startAction = { controller ->
                        controller.rollForward(
                            PlayerActionSelected(PlayerStandardActionType.BLITZ),
                            PlayerSelected("H2".playerId),
                        )
                    }
                    checkActionUsed = { assertEquals(0, player.team.turnData.blitzActions) }
                    rollBack = { 6 }
                }
                PlayerStandardActionType.FOUL -> {
                    startAction = { controller ->
                        controller.rollForward(
                            PlayerActionSelected(PlayerStandardActionType.FOUL),
                            PlayerSelected("H1".playerId),
                        )
                    }
                    checkActionUsed = { assertEquals(0, player.team.turnData.foulActions) }
                    rollBack = { 6 }
                }
                PlayerStandardActionType.THROW_TEAM_MATE,
                PlayerStandardActionType.SPECIAL -> {
                    // Skip for now
                    startAction = null
                }
            }

            if (startAction != null) {
                try {
                    controller.rollForward(PlayerSelected(player.id))
                    startAction(controller)
                    controller.rollForward(
                        MoveTypeSelected(MoveType.STAND_UP),
                        3.d6, // Fail Standing Up Roll
                        NoRerollSelected()
                    )
                    checkActionUsed()
                    for (i in 0 until rollBack()) {
                        controller.processAction(Undo)
                    }
                } catch (ex: Throwable) {
                    fail("$action failed: $ex")
                }
            }
        }
    }
}
