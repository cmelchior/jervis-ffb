package com.jervisffb.test.bb2025.tables

import com.jervisffb.engine.actions.BlockTypeSelected
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndSetup
import com.jervisffb.engine.actions.EndSetupWhenReady
import com.jervisffb.engine.actions.EndTurn
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.ForegoActivationSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.PlayersSelected
import com.jervisffb.engine.actions.RandomPlayersSelected
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.actions.SelectPlayerAction
import com.jervisffb.engine.actions.SelectPlayers
import com.jervisffb.engine.ext.d16
import com.jervisffb.engine.ext.d3
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.ext.playerNo
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.locations.DogOut
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.model.modifiers.KickoffStatModifier
import com.jervisffb.engine.model.modifiers.PlayerStatusEffectType
import com.jervisffb.engine.rules.bb2025.procedures.TeamTurn
import com.jervisffb.engine.rules.common.actions.BlockType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.procedures.Bounce
import com.jervisffb.engine.rules.common.procedures.tables.kickoff.SolidDefense
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.rules.common.tables.PrayerToNuffle
import com.jervisffb.engine.rules.common.tables.Weather
import com.jervisffb.engine.utils.singleInstanceOf
import com.jervisffb.engine.utils.singleInstanceOfOrNull
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.SmartMoveTo
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.defaultAwaySetup
import com.jervisffb.test.defaultHomeSetup
import com.jervisffb.test.defaultKickOffHomeTeam
import com.jervisffb.test.defaultPregame
import com.jervisffb.test.defaultSetup
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.moveTo
import com.jervisffb.test.skipTurns
import com.jervisffb.test.utils.SelectSingleBlockDieResult
import com.jervisffb.test.utils.SelectSkillReroll
import kotlin.collections.orEmpty
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * This class is testing all the results on the Kick-off Event Table.
 */
class KickOffEventTests: JervisGameBB2025Test() {

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
    fun timeOut_moveForward_lastChance() {
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
        assertEquals(6, state.kickingTeam.turnMarker)
        assertEquals(7, state.receivingTeam.turnMarker)
    }

    @Test
    fun timeOut_moveBack() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup()
        )

        // Fake turn number after setup
        state.kickingTeam.turnMarker = 6
        state.receivingTeam.turnMarker = 6
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
        assertEquals(awayTeam, state.activeTeamOrThrow())
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
    fun solidDefense_doNotCountPlayersNotBeingMoved() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(2.d6, 2.d6), // Roll Solid Defense
                    1.d3, // D3 + 3 players
                    PlayerSelected("H6".playerId),
                    FieldSquareSelected(11, 1), // Same location, do not count
                    PlayerSelected("H7".playerId),
                    FieldSquareSelected(9, 6), // Move 1
                    PlayerSelected("H8".playerId),
                    FieldSquareSelected(9, 13), // Move 1
                    PlayerSelected("H9".playerId),
                    FieldSquareSelected(10, 13), // Move 1
                    PlayerSelected("H10".playerId),
                    FieldSquareSelected(8, 7), // Move 1
                ),
                bounce = null
            )
        )
        // At this point we moved 5 players, 1 to the same location, 4 to new locations.
        // The last four should continue to be moved
        val availablePlayers = controller.getAvailableActions().get<SelectPlayer>().players
        assertEquals(4, availablePlayers.size)
        assertFalse(availablePlayers.contains("H6".playerId))
        assertTrue(availablePlayers.contains("H7".playerId))
        assertTrue(availablePlayers.contains("H8".playerId))
        assertTrue(availablePlayers.contains("H9".playerId))
        assertTrue(availablePlayers.contains("H10".playerId))
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

    // While probably not intended, the current wording of the rules allows a player
    // moving under the ball on the kicking team side (if it deviated there)
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
        // Award player moved to the kicking side the ball
        controller.rollForward(PlayerSelected("A10".playerId))
        assertTrue(awayTeam["A10".playerId].hasBall())
    }

    @Test
    fun highKick_noValidPlayers() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
        )
        // Put all players prone so they are no longer Open
        awayTeam.forEach { it.state = PlayerState.PRONE }
        controller.rollForward(
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(21, 7),
                deviate = DiceRollResults(2.d8, 1.d6), // Move ball to [21,5]
                kickoffEvent = arrayOf(
                    DiceRollResults(1.d6, 4.d6), // Roll High Kick, cannot be used because no players are available
                ),
                bounce = 2.d8
            ),
        )
        assertEquals(BallState.ON_GROUND, state.getBall().state)
        assertEquals(awayTeam, state.activeTeam)
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
                ),
                bounce = null
            ),
        )
        assertEquals(BallState.SCATTERED, state.currentBall().state)
        controller.rollForward(
            DiceRollResults(2.d8, 2.d8, 2.d8), // Scatter 3 times
            2.d8 // Final bounce
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
        val availableActions = controller.getAvailableActions().actions
        assertEquals(6, availableActions.singleInstanceOfOrNull<SelectPlayer>()?.players.orEmpty().size)
        assertTrue(availableActions.contains(EndSetupWhenReady))

        // Move 3 players then end the Quick Snap
        controller.rollForward(PlayerSelected("A6".playerId))
        assertEquals(8, controller.getAvailableActions().actionsCount) // Can move into all nearby fields
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
        assertEquals(2, controller.getAvailableActions().actionsCount)
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
    fun charge() {

    }

    @Test
    fun dodgySnack_reduceStats() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(6.d6, 5.d6), // Roll Dodgy Snack
                    6.d6, // Home team rolls
                    3.d6, // Away team rolls
                    RandomPlayersSelected(listOf("A1".playerId)),
                    2.d6,
                )
            )
        )
        val player = state.getPlayerById("A1".playerId)
        assertTrue(player.location.isOnField(rules))
        assertTrue(player.statusEffects.any { it.type == PlayerStatusEffectType.DODGY_SNACK })
        assertTrue(player.armourModifiers.any { it == KickoffStatModifier.DODGY_SNACK_AV })
        assertTrue(player.moveModifiers.any { it == KickoffStatModifier.DODGY_SNACK_MA })
    }

    @Test
    fun dodgySnack_sick() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(6.d6, 5.d6), // Roll Dodgy Snack
                    6.d6, // Home team rolls
                    3.d6, // Away team rolls
                    RandomPlayersSelected(listOf("A1".playerId)),
                    1.d6,
                )
            )
        )
        val player = state.getPlayerById("A1".playerId)
        assertFalse(player.location.isOnField(rules))
        assertEquals(PlayerState.DODGY_SNACK, player.state)
        assertFalse(player.armourModifiers.any { it == KickoffStatModifier.DODGY_SNACK_AV })
        assertFalse(player.moveModifiers.any { it == KickoffStatModifier.DODGY_SNACK_MA })
    }

    @Test
    fun dodgySnack_expireAtEndOfDrive() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(6.d6, 5.d6), // Roll Dodgy Snack
                    6.d6, // Home team rolls
                    3.d6, // Away team rolls
                    RandomPlayersSelected(listOf("A1".playerId)),
                    2.d6,
                )
            ),
            *skipTurns(16)
        )
        val player = state.getPlayerById("A1".playerId)
        assertFalse(player.statusEffects.any { it.type == PlayerStatusEffectType.DODGY_SNACK })
        assertFalse(player.armourModifiers.any { it == KickoffStatModifier.DODGY_SNACK_AV })
        assertFalse(player.moveModifiers.any { it == KickoffStatModifier.DODGY_SNACK_MA })    }

    @Test
    fun dodgySnack_bothTeams() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(6.d6, 5.d6), // Roll Dodgy Snack
                    3.d6, // Home team rolls
                    3.d6, // Away team rolls
                    RandomPlayersSelected(listOf("A3".playerId)),
                    2.d6,
                    RandomPlayersSelected(listOf("H1".playerId)),
                    2.d6,
                )
            )
        )
        assertTrue(state.getPlayerById("A1".playerId).statusEffects.none { it.type == PlayerStatusEffectType.DODGY_SNACK })
        assertTrue(state.getPlayerById("H3".playerId).statusEffects.none { it.type == PlayerStatusEffectType.DODGY_SNACK })
    }

    @Test
    fun dodgySnack_noAvailablePlayers() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup()
        )
        // Fake it by moving all kickoff players back to Dogout after setup
        awayTeam.forEachIndexed { index, player ->
            player.state = PlayerState.RESERVE
            player.location = DogOut
        }
        controller.rollForward(
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(6.d6, 5.d6), // Roll Dodgy Snack
                    6.d6, // Home team rolls
                    1.d6, // Away team rolls
                )
            )
        )
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
                    RandomPlayersSelected(listOf("A1".playerId, "A2".playerId)),
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
    fun pitchInvasion_lessAvailablePlayersOnTheField() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup()
        )
        // Fake it, by moving all kickoff players except 1 back to Dogout after setup
        awayTeam.forEachIndexed { index, player ->
            if (index > 0) {
                player.state = PlayerState.RESERVE
                player.location = DogOut
            }
        }
        controller.rollForward(
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(6.d6, 6.d6), // Roll Officious Ref
                    6.d6, // Home team rolls
                    1.d6, // Away team rolls
                    3.d3, // 3 players affected, but only 1 is available
                    RandomPlayersSelected(listOf("A1".playerId)),
                )
            )
        )
        assertEquals(TeamTurn.SelectPlayerOrEndTurn, controller.currentNode())
        assertEquals(1, awayTeam.count { it.state == PlayerState.STUNNED })
        assertEquals(PlayerState.STUNNED, state.getPlayerById("A1".playerId).state)
    }

    @Test
    fun pitchInvasion_noPlayersOnField() {
        controller.rollForward(
            *defaultPregame(),
            *defaultHomeSetup(),
            *defaultAwaySetup(endSetup = false)
        )
        // Fake it by moving all kickoff players back to Dogout after setup
        homeTeam.forEach {
            it.state = PlayerState.RESERVE
            it.location = DogOut
        }
        controller.rollForward(
            EndSetup,
            *defaultKickOffHomeTeam(
                selectKicker = null,
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

    // Check that all selected players can move during a Charge!
    @Test
    fun charge_moveAction() {
        val players = listOf("H6".playerId, "H7".playerId, "H8".playerId, "H9".playerId)
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(6.d6, 4.d6), // Roll Charge
                    1.d3, // How many players to activate.
                    PlayersSelected(players),
                ),
                bounce = null
            )
        )
        players.forEach {
            val startCoordinates = homeTeam[it].coordinates
            controller.rollForward(
                *activatePlayer(it.value, PlayerStandardActionType.MOVE),
                *moveTo(startCoordinates.x, startCoordinates.y - 1),
                EndAction
            )
            assertEquals(startCoordinates.move(Direction.UP, 1), homeTeam[it].coordinates)
        }
        controller.rollForward(
            EndTurn,
            2.d8 // Bounce
        )
        assertEquals(awayTeam, state.activeTeam)
    }

    @Test
    fun charge_blitzAction() {
        val players = listOf("H6".playerId, "H7".playerId, "H8".playerId, "H9".playerId)
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(6.d6, 4.d6), // Roll Charge
                    1.d3, // How many players to activate.
                    PlayersSelected(players),
                    *activatePlayer("H6", PlayerStandardActionType.BLITZ),
                    PlayerSelected("A1".playerId),
                    SmartMoveTo(13, 4),
                    PlayerSelected("A1".playerId),
                    BlockTypeSelected(BlockType.STANDARD),
                    DiceRollResults(6.dblock, 6.dblock),
                    SelectSingleBlockDieResult(),
                    DirectionSelected(Direction.DOWN_RIGHT),
                    Cancel,
                    DiceRollResults(1.d6, 1.d6),
                    EndAction
                ),
                bounce = null
            )
        )
        assertEquals(homeTeam, state.activeTeam)
    }

    @Test
    fun charge_throwTeamMate() {
        val players = listOf("H6".playerId, "H7".playerId, "H8".playerId, "H9".playerId)
        homeTeam["H9".playerId].addSkill(SkillType.THROW_TEAMMATE)
        homeTeam["H10".playerId].let {
            it.strength = 2
            it.addSkill(SkillType.RIGHT_STUFF)
        }
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(6.d6, 4.d6), // Roll Charge
                    1.d3, // How many players to activate.
                    PlayersSelected(players),
                    *activatePlayer("H9", PlayerStandardActionType.THROW_TEAM_MATE),
                    SmartMoveTo(10, 7),
                    PlayerSelected("H10".playerId),
                    FieldSquareSelected(11, 7),
                    6.d6, // Throw
                    DiceRollResults(2.d8, 2.d8, 2.d8),  // Scatter
                    6.d6 // Landing
                ),
                bounce = null
            )
        )
        assertEquals(FieldCoordinate(11, 4), homeTeam["H10".playerId].coordinates)
        assertEquals(PlayerState.STANDING, homeTeam["H10".playerId].state)
        assertEquals(homeTeam, state.activeTeam)
    }

    @Test
    @Ignore
    fun charge_kickTeamMate() {
        // - [ ] 1 Kick Teammate
        TODO()
    }

    @Test
    fun charge_onlyListedActions() {
        val players = listOf("H6".playerId, "H7".playerId, "H8".playerId, "H9".playerId)
        homeTeam["H9".playerId].addSkill(SkillType.THROW_TEAMMATE)
        // homeTeam["H9".playerId].addSkill(SkillType.KICK_TEAMMATE)
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(6.d6, 4.d6), // Roll Charge
                    1.d3, // How many players to activate.
                    PlayersSelected(players),
                    PlayerSelected("H9".playerId),
                ),
                bounce = null
            )
        )
        controller.getAvailableActions().singleInstanceOf<SelectPlayerAction>().let {
            assertEquals(3, it.actions.size)
            assertTrue(it.actions.any { it.type == PlayerStandardActionType.MOVE })
            assertTrue(it.actions.any { it.type == PlayerStandardActionType.BLITZ })
            assertTrue(it.actions.any { it.type == PlayerStandardActionType.THROW_TEAM_MATE })
        }
    }

    @Test
    fun charge_endOnFallingOver() {
        val players = listOf("H6".playerId, "H7".playerId, "H8".playerId, "H9".playerId)
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(6.d6, 4.d6), // Roll Charge
                    1.d3, // How many players to activate.
                    PlayersSelected(players),
                    *activatePlayer("H9", PlayerStandardActionType.MOVE),
                    SmartMoveTo(12, 10),
                    *moveTo(13, 10),
                    1.d6, // Fail Dodge -> Turnover
                    DiceRollResults(2.d6, 2.d6)
                ),
            )
        )
        assertEquals(1, state.awayTeam.turnMarker)
        assertEquals(awayTeam, state.activeTeam)
    }

    @Test
    fun charge_endOnKnockedDown() {
        val players = listOf("H6".playerId, "H7".playerId, "H8".playerId, "H9".playerId)
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(6.d6, 4.d6), // Roll Charge
                    1.d3, // How many players to activate.
                    PlayersSelected(players),
                    *activatePlayer("H6", PlayerStandardActionType.BLITZ),
                    PlayerSelected("A1".playerId),
                    SmartMoveTo(13, 4),
                    PlayerSelected("A1".playerId),
                    BlockTypeSelected(BlockType.STANDARD),
                    DiceRollResults(1.dblock, 1.dblock),
                    SelectSingleBlockDieResult(), // Select Player Down!
                    DiceRollResults(1.d6, 1.d6),
                ),
            )
        )
        assertEquals(1, state.awayTeam.turnMarker)
        assertEquals(awayTeam, state.activeTeam)
    }

    // Dodge is listed as only working during a teams turn, but is expected to work during a Charge!
    // as they are listed as "activations work exactly like a team turn".
    @Test
    fun charge_canUseDodge() {
        val players = listOf("H6".playerId, "H7".playerId, "H8".playerId, "H9".playerId)
        homeTeam["H9".playerId].addSkill(SkillType.DODGE)
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(6.d6, 4.d6), // Roll Charge
                    1.d3, // How many players to activate.
                    PlayersSelected(players),
                    *activatePlayer("H9", PlayerStandardActionType.MOVE),
                    SmartMoveTo(12, 10),
                    *moveTo(13, 10),
                    1.d6, // Fail Dodge
                    SelectSkillReroll(SkillType.DODGE),
                    6.d6,
                    EndAction,
                    EndTurn
                ),
            )
        )
        assertEquals(FieldCoordinate(13, 10), homeTeam["H9".playerId].coordinates)
        assertEquals(PlayerState.STANDING, homeTeam["H9".playerId].state)
        assertEquals(1, state.awayTeam.turnMarker)
        assertEquals(awayTeam, state.activeTeam)
    }

    @Test
    fun charge_canForegoActivation() {
        val players = listOf("H6".playerId, "H7".playerId, "H8".playerId, "H9".playerId)
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(6.d6, 4.d6), // Roll Charge
                    1.d3, // How many players to activate.
                    PlayersSelected(players),
                ),
                bounce = null
            )
        )
        players.forEach {
            controller.rollForward(
                ForegoActivationSelected(it),
            )
        }
        controller.rollForward(
            EndTurn,
            2.d8 // Bounce
        )
        assertEquals(awayTeam, state.activeTeam)
    }

    @Test
    fun charge_stunnedPlayersAreStillStunnedInFirstTurn() {
        val players = listOf("H6".playerId, "H7".playerId, "H8".playerId, "H9".playerId)
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(6.d6, 4.d6), // Roll Charge
                    1.d3, // How many players to activate.
                    PlayersSelected(players),
                    *activatePlayer("H9", PlayerStandardActionType.MOVE),
                    SmartMoveTo(12, 10),
                    *moveTo(13, 10),
                    1.d6, // Fail Dodge -> Turnover
                    DiceRollResults(6.d6, 6.d6),
                    DiceRollResults(1.d6, 1.d6) // Stun
                ),
            )
        )
        assertEquals(PlayerState.STUNNED, homeTeam["H9".playerId].state)
        assertEquals(1, state.awayTeam.turnMarker)
        assertEquals(awayTeam, state.activeTeam)
    }

    @Test
    fun charge_onlyOpenPlayers() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(6.d6, 4.d6), // Roll Charge
                    1.d3, // How many players to activate.
                ),
                bounce = null
            )
        )
        controller.getAvailableActions().singleInstanceOf<SelectPlayers>().let {
            assertEquals(4, it.count)
            assertEquals(6, it.players.size)
            it.players.forEach {
                assertTrue(rules.isOpen(homeTeam[it]))
            }
        }
    }

    @Test
    fun charge_lessPlayersThanRolled() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup()
        )
        listOf("H6".playerId, "H7".playerId, "H8".playerId, "H9".playerId).forEach {
            homeTeam[it].state = PlayerState.PRONE
        }
        controller.rollForward(
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(6.d6, 4.d6), // Roll Charge
                    3.d3, // How many players to activate.
                ),
                bounce = null
            )
        )
        controller.getAvailableActions().singleInstanceOf<SelectPlayers>().let {
            assertEquals(2, it.count) // Rolled 6, but we only have 2 available
            assertEquals(2, it.players.size)
            it.players.forEach {
                assertTrue(rules.isOpen(homeTeam[it]))
            }
        }
    }

    @Test
    fun charge_skipIfNoAvailablePlayers() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup()
        )
        homeTeam.forEach {
            it.state = PlayerState.PRONE
        }
        controller.rollForward(
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(6.d6, 4.d6), // Roll Charge
                    1.d3, // How many players to activate.
                ),
                bounce = 2.d8
            )
        )
        assertEquals(1, awayTeam.turnMarker)
        assertEquals(awayTeam, state.activeTeam)
    }

    @Test
    fun charge_movePlayerUnderBallOnReceiverSide() {
        val players = listOf("H6".playerId, "H7".playerId, "H8".playerId, "H9".playerId)
        homeTeam["H9".playerId].addSkill(SkillType.DODGE)
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(14, 11),
                deviate = DiceRollResults(4.d8, 1.d6),
                kickoffEvent = arrayOf(
                    DiceRollResults(6.d6, 4.d6), // Roll Charge
                    1.d3, // How many players to activate.
                    PlayersSelected(players),
                    *activatePlayer("H9", PlayerStandardActionType.MOVE),
                    SmartMoveTo(13, 11),
                    EndAction,
                    EndTurn,
                    6.d6, // Catch
                ),
                bounce = null
            )
        )
        assertEquals(FieldCoordinate(13, 11), homeTeam["H9".playerId].coordinates)
        assertTrue(homeTeam["H9".playerId].hasBall())
        assertEquals(1, state.awayTeam.turnMarker)
        assertEquals(awayTeam, state.activeTeam)
    }
}
