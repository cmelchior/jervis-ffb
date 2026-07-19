package com.jervisffb.test.bb2025

import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.procedures.SetupTeam
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.followUp
import com.jervisffb.test.skipTurns
import com.jervisffb.test.standardBlock
import com.jervisffb.test.useApothecary
import com.jervisffb.test.utils.assertKnockedOut
import com.jervisffb.test.utils.assertReserves
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Class responsible for testing the "Recover Knocked-out Players" step of the
 * End of Drive sequence.
 *
 * We have hidden the order in which players are selected behind Automated
 * Actions, so for now, we only test the actual rolls
 */
class RecoverKnockedOutPlayersTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }


    @Test
    fun succeedOn4() {
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 6.dblock),
            DirectionSelected(Direction.LEFT),
            followUp(false),
            DiceRollResults(6.d6, 6.d6),
            DiceRollResults(4.d6, 5.d6), // Knocked Out
            useApothecary(false),
            *skipTurns(16)
        )

        val actions = controller.getAvailableActions()
        assertEquals(homeTeam, actions.team)
        defender.assertKnockedOut()

        controller.rollForward(
            PlayerSelected(defender.id),
            4.d6, // Recover roll
        )

        defender.assertReserves()
        assertEquals(SetupTeam.SelectPlayerOrEndSetup, controller.currentNode())
    }

    @Test
    fun below4Fails() {
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 6.dblock),
            DirectionSelected(Direction.LEFT),
            followUp(false),
            DiceRollResults(6.d6, 6.d6),
            DiceRollResults(4.d6, 5.d6), // Knocked Out
            useApothecary(false),
            *skipTurns(16)
        )

        val actions = controller.getAvailableActions()
        assertEquals(homeTeam, actions.team)
        defender.assertKnockedOut()

        controller.rollForward(
            PlayerSelected(defender.id),
            3.d6, // Recover roll
        )

        defender.assertKnockedOut()
        assertEquals(SetupTeam.SelectPlayerOrEndSetup, controller.currentNode())
    }
}
