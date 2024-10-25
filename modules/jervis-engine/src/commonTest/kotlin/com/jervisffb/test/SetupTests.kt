package com.jervisffb.test

import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.commands.SetPlayerState
import com.jervisffb.engine.GameController
import com.jervisffb.engine.ext.playerNo
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.StandardBB2020Rules
import com.jervisffb.engine.utils.createDefaultGameState
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * This class is testing various setups and whether [Rules.isValidSetup] works as intended.
 */
class SetupTests {

    private val rules = StandardBB2020Rules
    private lateinit var state: Game
    private lateinit var controller: GameController

    @BeforeTest
    fun setUp() {
        state = createDefaultGameState(rules)
        controller = GameController(rules, state)
    }

    // All players at the LoS with 2 one step back should be valid
    @Test
    fun valid_7_4_0() {
        state.homeTeam.apply {
            moveTo(this[1.playerNo], 12, 4)
            moveTo(this[2.playerNo], 12, 5)
            moveTo(this[3.playerNo], 12, 6)
            moveTo(this[4.playerNo], 12, 7)
            moveTo(this[5.playerNo], 12, 8)
            moveTo(this[6.playerNo], 12, 9)
            moveTo(this[7.playerNo], 12, 10)
            moveTo(this[8.playerNo], 11, 1)
            moveTo(this[9.playerNo], 11, 2)
            moveTo(this[10.playerNo], 11, 12)
            moveTo(this[11.playerNo], 11, 13)
        }
        assertTrue(rules.isValidSetup(state, state.homeTeam))
    }

    // 3 required players on LoS, all others are in the endzone.
    @Test
    fun valid_3_8_0() {
        state.awayTeam.apply {
            moveTo(this[1.playerNo], 13, 5)
            moveTo(this[2.playerNo], 13, 7)
            moveTo(this[3.playerNo], 13, 9)
            moveTo(this[4.playerNo], 25, 0)
            moveTo(this[5.playerNo], 25, 2)
            moveTo(this[6.playerNo], 25, 4)
            moveTo(this[7.playerNo], 25, 6)
            moveTo(this[8.playerNo], 25, 8)
            moveTo(this[9.playerNo], 25, 10)
            moveTo(this[10.playerNo], 25, 12)
            moveTo(this[11.playerNo], 25, 14)
        }
        assertTrue(rules.isValidSetup(state, state.awayTeam))
    }

    @Test
    fun valid_11_0_0() {
        state.awayTeam.apply {
            moveTo(this[1.playerNo], 13, 4)
            moveTo(this[2.playerNo], 13, 5)
            moveTo(this[3.playerNo], 13, 6)
            moveTo(this[4.playerNo], 13, 7)
            moveTo(this[5.playerNo], 13, 8)
            moveTo(this[6.playerNo], 13, 9)
            moveTo(this[7.playerNo], 13, 10)
            moveTo(this[8.playerNo], 13, 1)
            moveTo(this[9.playerNo], 13, 2)
            moveTo(this[10.playerNo], 13, 12)
            moveTo(this[11.playerNo], 13, 13)
        }
        assertTrue(rules.isValidSetup(state, state.awayTeam))
    }

    // If team has less than 3 players, they must all be on the LoS
    @Test
    fun valid_allPlayersOnLoSIfRequired() {
        repeat(10) { i ->
            state.homeTeam[(i + 1).playerNo].state = PlayerState.KNOCKED_OUT
        }
        state.homeTeam.apply {
            moveTo(this[11.playerNo], 12, 4)
            moveTo(this[12.playerNo], 12, 10)
        }
        assertTrue(rules.isValidSetup(state, state.homeTeam))
    }

    @Test
    fun valid_playersOnLoSInWideZone() {
        state.awayTeam.apply {
            moveTo(this[1.playerNo], 13, 4)
            moveTo(this[2.playerNo], 13, 5)
            moveTo(this[3.playerNo], 13, 6)
            moveTo(this[4.playerNo], 13, 7)
            moveTo(this[5.playerNo], 13, 8)
            moveTo(this[6.playerNo], 13, 9)
            moveTo(this[7.playerNo], 13, 10)
            moveTo(this[8.playerNo], 13, 1)
            moveTo(this[9.playerNo], 13, 2)
            moveTo(this[10.playerNo], 13, 12)
            moveTo(this[11.playerNo], 13, 13)
        }
        assertTrue(rules.isValidSetup(state, state.awayTeam))
    }

    @Test
    fun invalid_tooManyPlayersInAwayTopWideZone() {
        state.awayTeam.apply {
            moveTo(this[1.playerNo], 13, 4)
            moveTo(this[2.playerNo], 13, 5)
            moveTo(this[3.playerNo], 13, 6)
            moveTo(this[4.playerNo], 13, 7)
            moveTo(this[5.playerNo], 13, 8)
            moveTo(this[6.playerNo], 13, 9)
            moveTo(this[7.playerNo], 14, 0)
            moveTo(this[8.playerNo], 14, 1)
            moveTo(this[9.playerNo], 14, 2)
            moveTo(this[10.playerNo], 14, 12)
            moveTo(this[11.playerNo], 14, 13)
        }
        assertFalse(rules.isValidSetup(state, state.awayTeam))
    }

    @Test
    fun invalid_tooManyPlayersInAwayBottomWideZone() {
        state.awayTeam.apply {
            moveTo(this[1.playerNo], 13, 4)
            moveTo(this[2.playerNo], 13, 5)
            moveTo(this[3.playerNo], 13, 6)
            moveTo(this[4.playerNo], 13, 7)
            moveTo(this[5.playerNo], 13, 8)
            moveTo(this[6.playerNo], 13, 9)
            moveTo(this[7.playerNo], 13, 10)
            moveTo(this[8.playerNo], 14, 1)
            moveTo(this[9.playerNo], 14, 11)
            moveTo(this[10.playerNo], 14, 12)
            moveTo(this[11.playerNo], 14, 13)
        }
        assertFalse(rules.isValidSetup(state, state.awayTeam))
    }

    @Test
    fun invalid_tooManyPlayersInHomeTopWideZone() {
        state.homeTeam.apply {
            moveTo(this[1.playerNo], 12, 4)
            moveTo(this[2.playerNo], 12, 5)
            moveTo(this[3.playerNo], 12, 6)
            moveTo(this[4.playerNo], 12, 7)
            moveTo(this[5.playerNo], 12, 8)
            moveTo(this[6.playerNo], 12, 9)
            moveTo(this[7.playerNo], 11, 0)
            moveTo(this[8.playerNo], 11, 1)
            moveTo(this[9.playerNo], 11, 2)
            moveTo(this[10.playerNo], 11, 12)
            moveTo(this[11.playerNo], 11, 13)
        }
        assertFalse(rules.isValidSetup(state, state.homeTeam))
    }

    @Test
    fun invalid_tooManyPlayersInHomeBottomWideZone() {
        state.homeTeam.apply {
            moveTo(this[1.playerNo], 12, 4)
            moveTo(this[2.playerNo], 12, 5)
            moveTo(this[3.playerNo], 12, 6)
            moveTo(this[4.playerNo], 12, 7)
            moveTo(this[5.playerNo], 12, 8)
            moveTo(this[6.playerNo], 12, 9)
            moveTo(this[7.playerNo], 12, 10)
            moveTo(this[8.playerNo], 11, 1)
            moveTo(this[9.playerNo], 11, 11)
            moveTo(this[10.playerNo], 11, 12)
            moveTo(this[11.playerNo], 11, 13)
        }
        assertFalse(rules.isValidSetup(state, state.homeTeam))
    }


    // 3 players are required on the LoS
    @Test
    fun invalid_toFewPlayersOnLoS() {
        state.homeTeam.apply {
            moveTo(this[1.playerNo], 11, 4)
            moveTo(this[2.playerNo], 11, 5)
            moveTo(this[3.playerNo], 11, 6)
            moveTo(this[4.playerNo], 12, 7)
            moveTo(this[5.playerNo], 11, 8)
            moveTo(this[6.playerNo], 11, 9)
            moveTo(this[7.playerNo], 11, 10)
            moveTo(this[8.playerNo], 11, 1)
            moveTo(this[9.playerNo], 11, 2)
            moveTo(this[10.playerNo], 11, 12)
            moveTo(this[11.playerNo], 11, 13)
        }
        assertFalse(rules.isValidSetup(state, state.homeTeam))
    }

    @Test
    fun invalid_missingPlayers() {
        // Only 10 players on the field with 10 being available
        state.homeTeam.apply {
            moveTo(this[1.playerNo], 12, 4)
            moveTo(this[2.playerNo], 12, 5)
            moveTo(this[3.playerNo], 12, 6)
            moveTo(this[4.playerNo], 12, 7)
            moveTo(this[5.playerNo], 12, 8)
            moveTo(this[6.playerNo], 12, 9)
            moveTo(this[7.playerNo], 12, 10)
            moveTo(this[8.playerNo], 11, 1)
            moveTo(this[9.playerNo], 11, 2)
            moveTo(this[10.playerNo], 11, 12)
        }
        assertFalse(rules.isValidSetup(state, state.homeTeam))
    }

    private fun moveTo(player: Player, x: Int, y: Int) {
        SetPlayerState(player, PlayerState.STANDING).execute(state)
        SetPlayerLocation(player, FieldCoordinate(x, y)).execute(state)
    }
}
