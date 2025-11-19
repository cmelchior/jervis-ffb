package com.jervisffb.test.bb2025

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndSetup
import com.jervisffb.engine.actions.EndTurn
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.ext.d3
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.ext.playerNo
import com.jervisffb.engine.model.Availability
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.SmartMoveTo
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.defaultKickOffAwayTeam
import com.jervisffb.test.defaultKickOffHomeTeam
import com.jervisffb.test.defaultPregame
import com.jervisffb.test.defaultSetup
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.moveTo
import com.jervisffb.test.skipTurns
import com.jervisffb.test.standardBlock
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

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

    private fun setupGame() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam()
        )
    }

    @Test
    fun activatePlayerBeforeSelectingAction() {
        setupGame()
        val player = awayTeam[1.playerNo]
        assertEquals(Availability.AVAILABLE, player.available)
        controller.rollForward(PlayerSelected(player.id))
        assertEquals(Availability.IS_ACTIVE, player.available)
        assertEquals(player, state.activePlayer)
    }

    @Test
    fun playerIsActiveDuringAction() {
        setupGame()
        val player = awayTeam[1.playerNo]
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.MOVE)
        )
        assertEquals(Availability.IS_ACTIVE, player.available)
    }
}
