package com.jervisffb.test.tables

import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.EndSetup
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.RandomPlayersSelected
import com.jervisffb.engine.ext.d16
import com.jervisffb.engine.ext.d3
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.ext.playerNo
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.locations.DogOut
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.bb2020.procedures.Bounce
import com.jervisffb.engine.rules.bb2020.procedures.TeamTurn
import com.jervisffb.engine.rules.bb2020.procedures.tables.kickoff.SolidDefense
import com.jervisffb.engine.rules.bb2020.tables.PrayerToNuffle
import com.jervisffb.engine.rules.bb2020.tables.Weather
import com.jervisffb.test.JervisGameTest
import com.jervisffb.test.defaultKickOffHomeTeam
import com.jervisffb.test.defaultPregame
import com.jervisffb.test.defaultSetup
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.skipTurns
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * This class is testing all the results on the Kick-off Event Table.
 */
class KickOffEventTests: JervisGameTest() {

    @Test
    fun getTheRef() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(1.d6, 1.d6),
                )
            )
        )
        assertEquals(1, homeTeam.bribes.size)
        assertEquals(1, awayTeam.bribes.size)
    }

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
    fun timeOut_moveBack() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup()
        )

        // Fake turn number after setup
        state.kickingTeam.turnMarker = 8
        state.receivingTeam.turnMarker = 7
        controller.rollForward(
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(1.d6, 2.d6), // Roll Time-out
                )
            )
        )
        assertEquals(7, state.kickingTeam.turnMarker)
        assertEquals(7, state.receivingTeam.turnMarker)
    }


    @Test
    fun solidDefense() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(2.d6, 2.d6), // Roll Solid Defense
                    1.d3, // D3 + 3 players
                    PlayerSelected("H6".playerId),
                    FieldSquareSelected(0, 5),
                    PlayerSelected("H7".playerId),
                    FieldSquareSelected(0, 6),
                    EndSetup, // Will be valid
                ),
            )
        )
        assertEquals(FieldCoordinate(0, 5), state.getPlayerById("H6".playerId).location)
        assertEquals(FieldCoordinate(0, 6), state.getPlayerById("H7".playerId).location)
        assertEquals(awayTeam, state.activeTeam)
    }

    @Test
    fun solidDefense_invalid() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(2.d6, 2.d6), // Roll Solid Defense
                    1.d3, // D3 + 3 players
                    PlayerSelected("H6".playerId),
                    FieldSquareSelected(0, 0),
                    PlayerSelected("H7".playerId),
                    FieldSquareSelected(0, 1),
                    PlayerSelected("H8".playerId),
                    FieldSquareSelected(0, 2),
                    PlayerSelected("H9".playerId),
                    FieldSquareSelected(0, 3),
                    EndSetup, // Will be invalid
                ),
                bounce = null
            )
        )
        assertEquals(SolidDefense.InformOfInvalidSetup, controller.currentNode())
    }



    @Test
    fun solidDefense_lessPlayersThanRolled() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(2.d6, 2.d6), // Roll Solid Defense
                ),
                bounce = null
            )
        )
        // Only standing open players can be selected, so make
        // everyone but 1 prone
        (1..10).forEach {
            homeTeam[it.playerNo].state = PlayerState.PRONE
        }
        controller.rollForward(
            1.d3, // D3 + 3 players
        )
        // 1 player + EndSetup
        assertEquals(2, controller.getAvailableActions().actions.size)
    }

    @Test
    fun highKick() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(1.d6, 4.d6), // Roll High Kick
                    PlayerSelected("A10".playerId),
                ),
                bounce = null
            )
        )

        val player = state.receivingTeam[10.playerNo]
        assertEquals(player.location == state.getBall().location, true)
        assertFalse(player.hasBall())
        assertEquals(BallState.DEVIATING, state.getBall().state)
    }

    @Test
    fun highKick_onPlayer() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(13, 6),
                deviate = DiceRollResults(2.d8, 1.d6), // Move ball to [13,5] which is occupied
                kickoffEvent = arrayOf(
                    DiceRollResults(1.d6, 4.d6), // Roll High Kick
                    6.d6, // Player at [13,5] catches the ball
                ),
                bounce = null
            )
        )

        val player = state.getPlayerById("A1".playerId)
        assertTrue(player.hasBall())
        assertEquals(BallState.CARRIED, state.getBall().state)
    }

    @Test
    fun highKick_acrossLoS() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(13, 7),
                deviate = DiceRollResults(4.d8, 2.d6), // Move ball to [11,7], behind opponent LoS
                kickoffEvent = arrayOf(
                    DiceRollResults(1.d6, 4.d6), // Roll High Kick
                    PlayerSelected("A10".playerId),
                ),
                bounce = null
            )
        )

        val player = state.getPlayerById("A10".playerId)
        assertFalse(player.hasBall())
        assertEquals(FieldCoordinate(11, 7), player.location)
        assertEquals(BallState.DEVIATING, state.getBall().state)
    }

    @Test
    fun highKick_noValidPlayers() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(13, 0),
                deviate = DiceRollResults(2.d8, 1.d6), // Move ball to [11,7], behind opponent LoS
                kickoffEvent = arrayOf(
                    DiceRollResults(1.d6, 4.d6), // Roll High Kick, cannot be used
                ),
                bounce = null
            ),
        )
        assertEquals(BallState.OUT_OF_BOUNDS, state.getBall().state)
        controller.rollForward(
            PlayerSelected("A2".playerId) // Touchback
        )
        val player = state.getPlayerById("A2".playerId)
        assertTrue(player.hasBall())
        assertEquals(BallState.CARRIED, state.getBall().state)
    }


    @Test
    fun cheeringFans_equalRoll() {
        homeTeam.tempCheerleaders = 0
        awayTeam.tempCheerleaders = 1
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(1.d6, 5.d6), // Roll Cheering Fans
                    2.d6, // Home team roll
                    1.d6, // Away team roll, should be the same value
                ),
                bounce = null
            ),
        )
        assertEquals(Bounce.RollDirection, controller.currentProcedure()?.currentNode())
    }

    @Test
    fun cheeringFans_homeWins() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(1.d6, 5.d6), // Roll Cheering Fans
                    3.d6, // Home team roll
                    2.d6, // Away team roll
                    2.d16 // Prayers To Nuffle: Friends with the ref
                ),
                bounce = null
            ),
        )
        assertTrue(homeTeam.activePrayersToNuffle.contains(PrayerToNuffle.FRIENDS_WITH_THE_REF))
        assertFalse(awayTeam.activePrayersToNuffle.contains(PrayerToNuffle.FRIENDS_WITH_THE_REF))
    }

    @Test
    fun brilliantCoaching_noRerollGiven() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(2.d6, 5.d6), // Roll Brilliant Coaching
                    3.d6, // Home team roll
                    3.d6, // Away team roll
                ),
                bounce = null
            ),
        )
        assertEquals(4, homeTeam.availableRerolls.size)
        assertEquals(4, awayTeam.availableRerolls.size)
    }

    @Test
    fun brilliantCoaching_awayTeamWins() {
        homeTeam.tempAssistantCoaches = 0
        awayTeam.tempAssistantCoaches = 1
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(2.d6, 5.d6), // Roll Brilliant Coaching
                    3.d6, // Home team roll
                    3.d6, // Away team roll - Wins
                ),
                bounce = null
            ),
        )
        assertEquals(4, homeTeam.availableRerolls.size)
        assertEquals(5, awayTeam.availableRerolls.size)
    }

    @Test
    fun brilliantCoaching_rerollExpire() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(2.d6, 5.d6), // Roll Brilliant Coaching
                    2.d6, // Home team roll - Wins
                    1.d6, // Away team roll
                ),
            ),
        )
        assertEquals(5, homeTeam.availableRerolls.size)
        assertEquals(4, awayTeam.availableRerolls.size)

        controller.rollForward(*skipTurns(16)) // End the drive (and half)
        assertEquals(4, homeTeam.availableRerolls.size)
        assertEquals(4, awayTeam.availableRerolls.size)
    }


    @Test
    fun changingWeather() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(4.d6, 4.d6), // Roll Changing Weather
                    DiceRollResults(1.d6, 1.d6), // Roll Sweltering Heat
                ),
            ),
        )
        assertEquals(Weather.SWELTERING_HEAT, state.weather)
    }

    @Test
    fun changingWeather_scatter() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(4.d6, 4.d6), // Roll Changing Weather
                    DiceRollResults(3.d6, 4.d6), // Roll Perfect Conditions
                    DiceRollResults(2.d8, 2.d8, 2.d8) // Scatter 3 times up
                ),
                bounce = 2.d8 // Final bounce up
            ),
        )
        assertEquals(Weather.PERFECT_CONDITIONS, state.weather)
        assertEquals(BallState.ON_GROUND, state.getBall().state)
        assertEquals(FieldCoordinate(18, 3), state.getBall().location)
    }

    @Test
    fun changingWeather_scatterBackToReceiverField() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(13, 7),
                deviate = DiceRollResults(4.d8, 1.d6), // Deviate to [12,7] on Kickers sid
                kickoffEvent = arrayOf(
                    DiceRollResults(4.d6, 4.d6), // Roll Changing Weather
                    DiceRollResults(3.d6, 4.d6), // Roll Perfect Conditions
                    DiceRollResults(2.d8, 2.d8, 2.d8), // Scatter 3 times up
                    PlayerSelected("A1".playerId), // Touchback -> give to A1
                ),
                bounce = null
            ),
        )
        assertEquals(Weather.PERFECT_CONDITIONS, state.weather)
        assertEquals(BallState.CARRIED, state.getBall().state)
        assertTrue(awayTeam[1.playerNo].hasBall())
    }

    @Test
    fun changingWeather_scatterBackToKickerField() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(13, 7),
                deviate = DiceRollResults(4.d8, 1.d6), // Deviate to [12,7] on Kickers side
                kickoffEvent = arrayOf(
                    DiceRollResults(4.d6, 4.d6), // Roll Changing Weather
                    DiceRollResults(3.d6, 4.d6), // Roll Perfect Conditions
                    DiceRollResults(5.d8, 5.d8, 2.d8), // Scatter 3 times up to the right, back to receivers side [14, 6]
                ),
                bounce = 2.d8 // Bounce to [14, 5]
            ),
        )
        assertEquals(Weather.PERFECT_CONDITIONS, state.weather)
        assertEquals(BallState.ON_GROUND, state.getBall().state)
        assertEquals(FieldCoordinate(14, 5), state.getBall().location)
    }

    @Test
    fun changingWeather_perfectWeatherWhenOutOfBounds() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(13, 0),
                deviate = DiceRollResults(2.d8, 1.d6), // Deviate out of bounds with exit at [13, 0]
                kickoffEvent = arrayOf(
                    DiceRollResults(4.d6, 4.d6), // Roll Changing Weather
                    DiceRollResults(3.d6, 4.d6), // Roll Perfect Conditions
                    PlayerSelected("A5".playerId)
                ),
                bounce = null
            ),
        )
        assertEquals(Weather.PERFECT_CONDITIONS, state.weather)
        assertEquals(BallState.CARRIED, state.getBall().state)
        assertTrue(awayTeam[5.playerNo].hasBall())
    }



    @Test
    fun quickSnap() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(3.d6, 6.d6), // Roll Quick Snap
                    1.d3, // D3 + 3 players
                ),
                bounce = null
            )
        )
        // 6 players available to select + EndSetup
        assertEquals(6 + 1, controller.getAvailableActions().actions.size)

        // Move 3 players then end the Quick Snap
        controller.rollForward(PlayerSelected("A6".playerId))
        assertEquals(8, controller.getAvailableActions().size) // Can move into all nearby fields
        controller.rollForward(
            FieldSquareSelected(14,0),
            PlayerSelected("A7".playerId),
            FieldSquareSelected(14,1),
            PlayerSelected("A9".playerId),
            FieldSquareSelected(14,14),
            EndSetup
        )
        assertEquals(Bounce.RollDirection, controller.currentNode())
    }

    @Test
    fun quickSnap_notEnoughPlayers() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(4.d6, 5.d6), // Roll Quick Snap
                ),
                bounce = null
            )
        )
        // Only standing open players can be selected, so make
        // everyone but 1 prone
        (1..10).forEach {
            awayTeam[it.playerNo].state = PlayerState.PRONE
        }
        controller.rollForward(
            1.d3, // D3 + 3 players
        )
        // 1 player + EndSetup
        assertEquals(2, controller.getAvailableActions().size)
    }

    @Test
    fun quickSnap_automaticallyEndSetup() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(3.d6, 6.d6), // Roll Quick Snap
                    1.d3, // D3 + 3 players
                    PlayerSelected("A6".playerId),
                    FieldSquareSelected(14,0),
                    PlayerSelected("A7".playerId),
                    FieldSquareSelected(14,1),
                    PlayerSelected("A9".playerId),
                    FieldSquareSelected(14,14),
                    PlayerSelected("A8".playerId),
                    FieldSquareSelected(14,13),
                ),
            )
        )
        assertEquals(TeamTurn.SelectPlayerOrEndTurn, controller.currentNode())
    }

    @Test
    fun quickSnap_sameFieldDoesNotCountAsMoved() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(3.d6, 6.d6), // Roll Quick Snap
                    1.d3, // D3 + 3 players
                    PlayerSelected("A6".playerId),
                    FieldSquareSelected(14,1), // Same location
                    PlayerSelected("A6".playerId),
                    FieldSquareSelected(14,1), // Same location
                    EndSetup
                )
            )
        )
        assertEquals(TeamTurn.SelectPlayerOrEndTurn, controller.currentNode())
    }

    @Test
    @Ignore
    fun Blitz() {

    }

    @Test
    fun officiousRef() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(5.d6, 6.d6), // Roll Officious Ref
                    6.d6, // Home team rolls
                    3.d6, // Away team rolls
                    RandomPlayersSelected(listOf("A1".playerId)),
                    2.d6 // Roll for the Ref
                )
            )
        )
        assertEquals(PlayerState.STUNNED, state.getPlayerById("A1".playerId).state)
        assertFalse(state.getPlayerById("A1".playerId).hasTackleZones)
    }

    @Test
    fun officiousRef_bothTeams() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(5.d6, 6.d6), // Roll Officious Ref
                    6.d6, // Home team rolls
                    4.d6, // Away team rolls
                    RandomPlayersSelected(listOf("A1".playerId)),
                    1.d6, // Roll for the Ref
                    RandomPlayersSelected(listOf("H1".playerId)),
                    2.d6 // Roll for the Ref
                )
            )
        )
        val awayPlayer = state.getPlayerById("A1".playerId)
        val homePlayer = state.getPlayerById("H1".playerId)
        assertEquals(PlayerState.BANNED, awayPlayer.state)
        assertEquals(DogOut, awayPlayer.location)
        assertEquals(PlayerState.STUNNED, homePlayer.state)
    }

    @Test
    fun officiousRef_noPlayersOnField() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup()
        )

        // Fake it, by moving all kickoff players back to Dogout after setup
        homeTeam.forEach {
            it.state = PlayerState.RESERVE
            it.location = DogOut
        }
        controller.rollForward(
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(5.d6, 6.d6), // Roll Officious Ref
                    6.d6, // Home team rolls
                    4.d6, // Away team rolls
                    RandomPlayersSelected(listOf("A1".playerId)),
                    1.d6, // Roll for the Ref
                    // Skip rolls for home players as none are eligible
                )
            )
        )
        val awayPlayer = state.getPlayerById("A1".playerId)
        assertEquals(PlayerState.BANNED, awayPlayer.state)
        assertEquals(TeamTurn.SelectPlayerOrEndTurn, controller.currentNode())
    }


    @Test
    fun pitchInvasion() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(6.d6, 6.d6), // Roll Pitch Invasion
                    6.d6, // Home team rolls
                    3.d6, // Away team rolls
                    2.d3, // Affected players on Receiving team
                    RandomPlayersSelected(listOf("A1".playerId, "A2".playerId, "A3".playerId)),
                )
            )
        )
        assertEquals(PlayerState.STUNNED, state.getPlayerById("A1".playerId).state)
        assertFalse(state.getPlayerById("A1".playerId).hasBall())
    }

    @Test
    fun pitchInvasion_bothTeams() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(6.d6, 6.d6), // Roll Pitch Invasion
                    6.d6, // Home team rolls
                    4.d6, // Away team rolls
                    2.d3, // Affected players on Receiving team
                    RandomPlayersSelected(listOf("A1".playerId, "A3".playerId)),
                    1.d3, // Affected players on Kicking team
                    RandomPlayersSelected(listOf("H1".playerId)),
                )
            )
        )
        assertEquals(2, awayTeam.count { it.state == PlayerState.STUNNED })
        assertEquals(1, homeTeam.count { it.state == PlayerState.STUNNED })
        assertEquals(PlayerState.STUNNED, state.getPlayerById("A1".playerId).state)
        assertEquals(PlayerState.STUNNED, state.getPlayerById("A3".playerId).state)
        assertEquals(PlayerState.STUNNED, state.getPlayerById("H1".playerId).state)
    }

    @Test
    fun pitchInvasion_noPlayersOnField() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup()
        )
        // Fake it, by moving all kickoff players back to Dogout after setup
        homeTeam.forEach {
            it.state = PlayerState.RESERVE
            it.location = DogOut
        }
        controller.rollForward(
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(6.d6, 6.d6), // Roll Officious Ref
                    1.d6, // Home team rolls
                    6.d6, // Away team rolls
                    3.d3 // 3 players affected, but none are available
                )
            )
        )
        assertEquals(TeamTurn.SelectPlayerOrEndTurn, controller.currentNode())
    }
}
