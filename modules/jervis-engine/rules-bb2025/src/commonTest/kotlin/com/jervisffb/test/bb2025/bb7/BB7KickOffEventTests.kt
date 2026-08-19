package com.jervisffb.test.bb2025.bb7

import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndSetup
import com.jervisffb.engine.actions.EndSetupWhenReady
import com.jervisffb.engine.actions.EndTurn
import com.jervisffb.engine.actions.PitchSquareSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.PlayersSelected
import com.jervisffb.engine.actions.RandomPlayersSelected
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.actions.SelectPlayers
import com.jervisffb.engine.bb2025.procedures.TeamTurn
import com.jervisffb.engine.common.procedures.Bounce
import com.jervisffb.engine.ext.d3
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.ext.playerNo
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.PlayerPitchState
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.utils.singleInstanceOfOrNull
import com.jervisffb.test.JervisGameBB72025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.defaultKickOffHomeTeam
import com.jervisffb.test.defaultPregame
import com.jervisffb.test.defaultSetup
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.moveTo
import com.jervisffb.test.utils.assertActive
import com.jervisffb.test.utils.assertStunned
import kotlin.collections.orEmpty
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * This class is testing all the results on the BB7 Kick-off Event Table that
 * behave differently from the BB11 behavior
 */
class BB7KickOffEventTests: JervisGameBB72025Test() {

    @Test
    fun timeOut_moveForward() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(1.d6, 2.d6), // Roll Time-out
                )
            )
        )
        assertEquals(1, homeTeam.turnMarker)
        assertEquals(2, awayTeam.turnMarker)
    }

    @Test
    fun timeOut_moveForward_lastChance() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup()
        )

        // Fake turn number after setup
        state.kickingTeam.turnMarker = 4
        state.receivingTeam.turnMarker = 4
        controller.rollForward(
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(1.d6, 2.d6), // Roll Time-out
                )
            )
        )
        assertEquals(5, state.kickingTeam.turnMarker)
        assertEquals(6, state.receivingTeam.turnMarker)
    }

    @Test
    fun timeOut_moveBack() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup()
        )

        // Fake turn number after setup
        state.kickingTeam.turnMarker = 5
        state.receivingTeam.turnMarker = 5
        controller.rollForward(
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(1.d6, 2.d6), // Roll Time-out
                )
            )
        )
        assertEquals(4, state.kickingTeam.turnMarker)
        assertEquals(5, state.receivingTeam.turnMarker)
    }

    @Test
    fun alertDefense() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(3.d6, 1.d6), // Roll Alert Defens
                    1.d3, // D3 + 1 players
                ),
                bounce = null
            )
        )
        // 7 players available to select + EndSetup
        val availableActions = controller.getAvailableActions().actions
        assertEquals(7, availableActions.singleInstanceOfOrNull<SelectPlayer>()?.players.orEmpty().size)
        assertTrue(availableActions.contains(EndSetupWhenReady))

        // Move 2 players to automatically end Alert Defense
        controller.rollForward(PlayerSelected("H1".playerId))
        assertEquals(8, controller.getAvailableActions().actionsCount) // Can move into all nearby squares
        controller.rollForward(
            PitchSquareSelected(7,2),
            EndSetup
        )
        assertEquals(Bounce.RollDirection, controller.currentNode())
    }

    @Test
    fun alertDefense_notEnoughPlayers() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(2.d6, 2.d6), // Roll Alert Defense
                ),
                bounce = null
            )
        )
        // Only standing open players can be selected, so make
        // everyone but 1 prone
        (1..6).forEach {
            homeTeam[it.playerNo].state = PlayerPitchState.PRONE
        }
        controller.rollForward(
            3.d3, // D3 + 1 players
        )
        // 1 player + EndSetup
        assertEquals(2, controller.getAvailableActions().actionsCount)
    }

    @Test
    fun alertDefense_automaticallyEndSetup() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(3.d6, 1.d6), // Roll Alert Defense
                    1.d3, // D3 + 1 players
                    PlayerSelected("H1".playerId),
                    PitchSquareSelected(7,2),
                    PlayerSelected("H2".playerId),
                    PitchSquareSelected(7,3),
                ),
            )
        )
        assertEquals(TeamTurn.SelectPlayerOrEndTurn, controller.currentNode())
    }

    @Test
    fun alertDefense_sameSquareDoesNotCountAsMoved() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(1.d6, 3.d6), // Roll Alert Defense
                    1.d3, // D3 + 1 players
                    PlayerSelected("H7".playerId),
                    PitchSquareSelected(6,8), // Same location
                    PlayerSelected("H6".playerId),
                    PitchSquareSelected(6,7), // Same location
                    EndSetup
                )
            )
        )
        assertEquals(TeamTurn.SelectPlayerOrEndTurn, controller.currentNode())
    }


    @Test
    fun quickSnapAmountOfPlayers() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(3.d6, 6.d6), // Roll Quick Snap
                    1.d3, // D3 + 1 players
                ),
                bounce = null
            )
        )
        // 7 players available to select + EndSetup
        val availableActions = controller.getAvailableActions().actions
        assertEquals(7, availableActions.singleInstanceOfOrNull<SelectPlayer>()?.players.orEmpty().size)
        assertTrue(availableActions.contains(EndSetupWhenReady))

        // Move 2 players which ends Quick Snap automatically
        controller.rollForward(PlayerSelected("A1".playerId))
        assertEquals(8, controller.getAvailableActions().actionsCount) // Can move into all nearby squares
        controller.rollForward(
            PitchSquareSelected(12,2),
            PlayerSelected("A2".playerId),
            PitchSquareSelected(12,3),
        )
        assertEquals(Bounce.RollDirection, controller.currentNode())
    }

    @Test
    fun chargeAmountOfPlayers() {
        val players = listOf("H4".playerId, "H5".playerId, "H6".playerId, "H7".playerId)
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(6.d6, 4.d6), // Roll Charge
                    3.d3, // How many players to activate. D3 + 1
                ),
                bounce = null
            )
        )
        val action = controller.getAvailableActions().get<SelectPlayers>()
        assertEquals(7, action.players.size)
        assertEquals(4, action.count)

        controller.rollForward(
            PlayersSelected(players)
        )

        players.forEach { id ->
            val player = homeTeam[id]
            val startCoordinates = player.coordinates
            controller.rollForward(
                *activatePlayer(player, PlayerStandardActionType.MOVE),
                *moveTo(startCoordinates.x + 1, startCoordinates.y),
                EndAction
            )
            assertEquals(startCoordinates.move(Direction.RIGHT, 1), player.coordinates)
        }
        controller.rollForward(
            EndTurn,
            2.d8 // Bounce
        )
        awayTeam.assertActive()
    }

    @Test
    fun pitchInvasionOnePlayerAffected() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(6.d6, 6.d6), // Roll Pitch Invasion
                    6.d6, // Home team rolls
                    5.d6, // Away team rolls
                    RandomPlayersSelected(listOf("A1".playerId)),
                    RandomPlayersSelected(listOf("H1".playerId)),
                )
            )
        )
        state.getPlayerById("A1".playerId).assertStunned()
        state.getPlayerById("H1".playerId).assertStunned()
    }
}
