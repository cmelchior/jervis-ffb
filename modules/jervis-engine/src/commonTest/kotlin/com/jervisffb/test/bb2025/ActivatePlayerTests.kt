package com.jervisffb.test.bb2025

import com.jervisffb.engine.actions.EndTurn
import com.jervisffb.engine.actions.ForegoActivationSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectForgoActivation
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.ext.playerNo
import com.jervisffb.engine.model.Availability
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.ext.rollForward
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Class responsible for testing the high-level proprieties of activating
 * a player.
 *
 * In most cases, canceling an action before a player has moved is allowed,
 * but this is tested under each action.
 *
 * @see com.jervisffb.test.bb2025.actions.MoveActionTests
 * @see com.jervisffb.test.bb2025.actions.BlockActionTests
 * @see com.jervisffb.test.bb2025.actions.BlitzActionTests
 * @see com.jervisffb.test.bb2025.actions.FoulActionTests
 * @see com.jervisffb.test.bb2025.actions.PassActionTests
 * @see com.jervisffb.test.bb2025.actions.HandOffActionTests
 * @see com.jervisffb.test.bb2025.actions.ThrowTeamMateActionTests
 */
class ActivatePlayerTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun activatePlayerBeforeSelectingAction() {
        val player = awayTeam[1.playerNo]
        assertEquals(Availability.AVAILABLE, player.available)
        controller.rollForward(PlayerSelected(player.id))
        assertEquals(Availability.IS_ACTIVE, player.available)
        assertEquals(player, state.activePlayer)
    }

    @Test
    fun playerIsActiveDuringAction() {
        val player = awayTeam[1.playerNo]
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.MOVE)
        )
        assertEquals(Availability.IS_ACTIVE, player.available)
    }

    @Test
    fun canForegoActivation() {
        val action = controller.getAvailableActions().getOrNull<SelectForgoActivation>()
        assertNotNull(action)
        assertEquals(11, action.players.size)
        controller.rollForward(
            ForegoActivationSelected("A1".playerId)
        )
        assertEquals(Availability.HAS_ACTIVATED, awayTeam["A1".playerId].available)
    }

    @Test
    fun canEndTurnEvenIfAllPlayersAreNotActivated() {
        // All players will automatically forego their activation, but we cannot test
        // this better unless we factor in Stalling.
        controller.rollForward(
            EndTurn
        )
        assertEquals(homeTeam, state.activeTeam)
    }
}
