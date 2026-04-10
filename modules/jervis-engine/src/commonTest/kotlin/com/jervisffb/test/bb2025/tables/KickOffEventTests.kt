package com.jervisffb.test.bb2025.tables

import com.jervisffb.engine.actions.BlockTypeSelected
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndSetup
import com.jervisffb.engine.actions.EndSetupWhenReady
import com.jervisffb.engine.actions.EndTurn
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.RandomPlayersSelected
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.actions.SelectPlayer
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
import com.jervisffb.engine.model.modifiers.TeamFeatureType
import com.jervisffb.engine.rules.bb2025.procedures.TeamTurn
import com.jervisffb.engine.rules.common.actions.BlockType
import com.jervisffb.engine.rules.common.actions.PlayerSpecialActionType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.procedures.Bounce
import com.jervisffb.engine.rules.common.procedures.tables.kickoff.SolidDefense
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.rules.common.tables.Weather
import com.jervisffb.engine.utils.singleInstanceOfOrNull
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.defaultAwaySetup
import com.jervisffb.test.defaultHomeSetup
import com.jervisffb.test.defaultKickOffHomeTeam
import com.jervisffb.test.defaultPregame
import com.jervisffb.test.defaultSetup
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.skipTurns
import com.jervisffb.test.utils.SelectSingleBlockDieResult
import com.jervisffb.test.utils.assertCoordinates
import com.jervisffb.test.utils.assertStunned
import kotlin.collections.orEmpty
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * This class is testing all the results on the Kick-off Event Table, execept
 * for Charge! which is tested in [ChargeTests].
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
        state.getPlayerById("H6".playerId).assertCoordinates(0, 5)
        state.getPlayerById("H7".playerId).assertCoordinates(0, 6)
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
        assertEquals(player.location == state.getBall().coordinates, true)
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
        player.assertCoordinates(11, 7)
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
            ),
        )
        assertTrue(homeTeam.hasFeature(TeamFeatureType.CHEERING_FANS_OFFENSIVE_ASSIST))
        assertTrue(awayTeam.hasFeature(TeamFeatureType.CHEERING_FANS_OFFENSIVE_ASSIST))
    }

    @Test
    fun cheeringFans_homeWins() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(1.d6, 5.d6), // Roll Cheering Fans
                    4.d6, // Home team roll
                    2.d6, // Away team roll
                ),
            ),
        )
        assertTrue(homeTeam.hasFeature(TeamFeatureType.CHEERING_FANS_OFFENSIVE_ASSIST))
        assertFalse(awayTeam.hasFeature(TeamFeatureType.CHEERING_FANS_OFFENSIVE_ASSIST))
    }

    @Test
    fun cheeringFans_awayWins() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(1.d6, 5.d6), // Roll Cheering Fans
                    3.d6, // Home team roll
                    6.d6, // Away team roll
                ),
            ),
        )
        assertFalse(homeTeam.hasFeature(TeamFeatureType.CHEERING_FANS_OFFENSIVE_ASSIST))
        assertTrue(awayTeam.hasFeature(TeamFeatureType.CHEERING_FANS_OFFENSIVE_ASSIST))
    }

    @Test
    fun cheeringFans_benefitExpiresAfterFirstTurn() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(1.d6, 5.d6), // Roll Cheering Fans
                    3.d6, // Home team roll
                    6.d6, // Away team roll
                ),
            ),
        )
        assertFalse(homeTeam.hasFeature(TeamFeatureType.CHEERING_FANS_OFFENSIVE_ASSIST))
        assertTrue(awayTeam.hasFeature(TeamFeatureType.CHEERING_FANS_OFFENSIVE_ASSIST))
        controller.rollForward(
            EndTurn
        )
        assertEquals(homeTeam, state.activeTeam)
        assertFalse(homeTeam.hasFeature(TeamFeatureType.CHEERING_FANS_OFFENSIVE_ASSIST))
        assertFalse(awayTeam.hasFeature(TeamFeatureType.CHEERING_FANS_OFFENSIVE_ASSIST))
    }

    @Test
    fun cheeringFans_benefitExpiresAfterFirstTeamTurn() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(1.d6, 5.d6), // Roll Cheering Fans
                    6.d6, // Home team roll
                    1.d6, // Away team roll
                ),
            ),
        )
        assertTrue(homeTeam.hasFeature(TeamFeatureType.CHEERING_FANS_OFFENSIVE_ASSIST))
        assertFalse(awayTeam.hasFeature(TeamFeatureType.CHEERING_FANS_OFFENSIVE_ASSIST))
        assertEquals(awayTeam, state.activeTeam)
        controller.rollForward(
            EndTurn
        )
        assertEquals(homeTeam, state.activeTeam)
        assertTrue(homeTeam.hasFeature(TeamFeatureType.CHEERING_FANS_OFFENSIVE_ASSIST))
        assertFalse(awayTeam.hasFeature(TeamFeatureType.CHEERING_FANS_OFFENSIVE_ASSIST))
        controller.rollForward(
            EndTurn
        )
        assertEquals(awayTeam, state.activeTeam)
        assertFalse(homeTeam.hasFeature(TeamFeatureType.CHEERING_FANS_OFFENSIVE_ASSIST))
        assertFalse(awayTeam.hasFeature(TeamFeatureType.CHEERING_FANS_OFFENSIVE_ASSIST))
    }

    @Test
    fun cheeringFans_usedOnFirstBlock() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(1.d6, 5.d6), // Roll Cheering Fans
                    1.d6, // Home team roll
                    6.d6, // Away team roll
                ),
            ),
        )
        val attacker = awayTeam["A1".playerId]
        val defender = homeTeam["H1".playerId]
        assertTrue(awayTeam.hasFeature(TeamFeatureType.CHEERING_FANS_OFFENSIVE_ASSIST))
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            PlayerSelected(defender),
        )
        val diceAction = controller.getAvailableActions().get<RollDice>()
        assertTrue(diceAction.dice.all { it == Dice.BLOCK })
        assertEquals(2, diceAction.dice.size)
        controller.rollForward(
            DiceRollResults(3.dblock, 6.dblock),
            NoRerollSelected(),
            SelectSingleBlockDieResult(index = 1),
            DirectionSelected(Direction.LEFT),
            Cancel, // Do not follow up
            DiceRollResults(1.d6, 1.d6),
        )
        assertNull(state.activePlayer)
        assertFalse(awayTeam.hasFeature(TeamFeatureType.CHEERING_FANS_OFFENSIVE_ASSIST))
    }

    @Test
    fun cheeringFans_usedOnFirstBlitz() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(1.d6, 5.d6), // Roll Cheering Fans
                    1.d6, // Home team roll
                    6.d6, // Away team roll
                ),
            ),
        )
        val attacker = awayTeam["A1".playerId]
        val defender = homeTeam["H1".playerId]
        assertTrue(awayTeam.hasFeature(TeamFeatureType.CHEERING_FANS_OFFENSIVE_ASSIST))
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLITZ),
            PlayerSelected(defender),
            PlayerSelected(defender),
            BlockTypeSelected(BlockType.STANDARD),
        )
        val diceAction = controller.getAvailableActions().get<RollDice>()
        assertTrue(diceAction.dice.all { it == Dice.BLOCK })
        assertEquals(2, diceAction.dice.size)
        controller.rollForward(
            DiceRollResults(3.dblock, 6.dblock),
            NoRerollSelected(),
            SelectSingleBlockDieResult(index = 1),
            DirectionSelected(Direction.LEFT),
            Cancel, // Do not follow up
            DiceRollResults(1.d6, 1.d6),
            EndAction
        )
        assertNull(state.activePlayer)
        assertFalse(awayTeam.hasFeature(TeamFeatureType.CHEERING_FANS_OFFENSIVE_ASSIST))
    }

    @Test
    fun cheeringFans_notUsedOnSpecialActions() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(1.d6, 5.d6), // Roll Cheering Fans
                    1.d6, // Home team roll
                    6.d6, // Away team roll
                ),
            ),
        )
        val attacker = awayTeam["A1".playerId]
        attacker.addSkill(SkillType.STAB)
        val defender = homeTeam["H1".playerId]
        assertTrue(awayTeam.hasFeature(TeamFeatureType.CHEERING_FANS_OFFENSIVE_ASSIST))
        controller.rollForward(
            *activatePlayer(attacker, PlayerSpecialActionType.STAB),
            PlayerSelected(defender),
            DiceRollResults(3.d6, 2.d6),
        )
        assertNull(state.activePlayer)
        assertTrue(awayTeam.hasFeature(TeamFeatureType.CHEERING_FANS_OFFENSIVE_ASSIST))
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
        assertEquals(FieldCoordinate(18, 3), state.getBall().coordinates)
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
        assertEquals(FieldCoordinate(14, 5), state.getBall().coordinates)
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
        state.getPlayerById("A1".playerId).assertStunned()
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
        state.getPlayerById("A1".playerId).assertStunned()
        state.getPlayerById("A3".playerId).assertStunned()
        state.getPlayerById("H1".playerId).assertStunned()
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
        state.getPlayerById("A1".playerId).assertStunned()
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
}
