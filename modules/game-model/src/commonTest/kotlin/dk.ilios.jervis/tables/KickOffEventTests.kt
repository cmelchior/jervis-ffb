package dk.ilios.jervis.tables

import dk.ilios.jervis.JervisGameTest
import dk.ilios.jervis.actions.DiceResults
import dk.ilios.jervis.actions.FieldSquareSelected
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.defaultKickOffHomeTeam
import dk.ilios.jervis.defaultPregame
import dk.ilios.jervis.defaultSetup
import dk.ilios.jervis.ext.d16
import dk.ilios.jervis.ext.d6
import dk.ilios.jervis.ext.d8
import dk.ilios.jervis.ext.playerId
import dk.ilios.jervis.ext.playerNo
import dk.ilios.jervis.model.BallState
import dk.ilios.jervis.model.FieldCoordinate
import dk.ilios.jervis.procedures.Bounce
import dk.ilios.jervis.procedures.FullGame
import dk.ilios.jervis.rules.tables.PrayerToNuffle
import dk.ilios.jervis.rules.tables.Weather
import dk.ilios.jervis.skipTurns
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
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceResults(1.d6, 1.d6),
                )
            )
        )
        assertEquals(1, homeTeam.bribes.size)
        assertEquals(1, awayTeam.bribes.size)
    }

    @Test
    fun timeOut_moveForward() {
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceResults(1.d6, 2.d6), // Roll Time-out
                )
            )
        )
        assertEquals(1, homeTeam.turnData.turnMarker)
        assertEquals(2, awayTeam.turnData.turnMarker)
    }

    @Test
    fun timeOut_moveBack() {
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup()
        )

        // Fake turn number after setup
        state.kickingTeam.turnData.turnMarker = 8
        state.receivingTeam.turnData.turnMarker = 7
        controller.rollForward(
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceResults(1.d6, 2.d6), // Roll Time-out
                )
            )
        )
        assertEquals(7, state.kickingTeam.turnData.turnMarker)
        assertEquals(7, state.receivingTeam.turnData.turnMarker)
    }


    @Test
    @Ignore
    fun SolidDefense() {
        TODO()
    }

    @Test
    fun highKick() {
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceResults(1.d6, 4.d6), // Roll High Kick
                    PlayerSelected("A10".playerId),
                ),
                bounce = null
            )
        )

        val player = state.receivingTeam[10.playerNo]
        assertEquals(player.location == state.ball.location, true)
        assertFalse(player.hasBall())
        assertEquals(BallState.DEVIATING, state.ball.state)
    }

    @Test
    fun highKick_onPlayer() {
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(13, 6),
                deviate = DiceResults(2.d8, 1.d6), // Move ball to [13,5] which is occupied
                kickoffEvent = arrayOf(
                    DiceResults(1.d6, 4.d6), // Roll High Kick
                    6.d6, // Player at [13,5] catches the ball
                ),
                bounce = null
            )
        )

        val player = state.getPlayerById("A1".playerId)
        assertTrue(player.hasBall())
        assertEquals(BallState.CARRIED, state.ball.state)
    }

    @Test
    fun highKick_acrossLoS() {
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(13, 7),
                deviate = DiceResults(4.d8, 2.d6), // Move ball to [11,7], behind opponent LoS
                kickoffEvent = arrayOf(
                    DiceResults(1.d6, 4.d6), // Roll High Kick
                    PlayerSelected("A10".playerId),
                ),
                bounce = null
            )
        )

        val player = state.getPlayerById("A10".playerId)
        assertFalse(player.hasBall())
        assertEquals(FieldCoordinate(11, 7), player.location.coordinate)
        assertEquals(BallState.DEVIATING, state.ball.state)
    }

    @Test
    fun highKick_noValidPlayers() {
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(13, 0),
                deviate = DiceResults(2.d8, 1.d6), // Move ball to [11,7], behind opponent LoS
                kickoffEvent = arrayOf(
                    DiceResults(1.d6, 4.d6), // Roll High Kick, cannot be used
                ),
                bounce = null
            ),
        )
        assertEquals(BallState.OUT_OF_BOUNDS, state.ball.state)
        controller.rollForward(
            PlayerSelected("A2".playerId) // Touchback
        )
        val player = state.getPlayerById("A2".playerId)
        assertTrue(player.hasBall())
        assertEquals(BallState.CARRIED, state.ball.state)
    }


    @Test
    fun cheeringFans_equalRoll() {
        homeTeam.tempCheerleaders = 0
        awayTeam.tempCheerleaders = 1
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceResults(1.d6, 5.d6), // Roll Cheering Fans
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
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceResults(1.d6, 5.d6), // Roll Cheering Fans
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
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceResults(2.d6, 5.d6), // Roll Brilliant Coaching
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
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceResults(2.d6, 5.d6), // Roll Brilliant Coaching
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
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceResults(2.d6, 5.d6), // Roll Brilliant Coaching
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
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceResults(4.d6, 4.d6), // Roll Changing Weather
                    DiceResults(1.d6, 1.d6), // Roll Sweltering Heat
                ),
            ),
        )
        assertEquals(Weather.SWELTERING_HEAT, state.weather)
    }

    @Test
    fun changingWeather_scatter() {
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceResults(4.d6, 4.d6), // Roll Changing Weather
                    DiceResults(3.d6, 4.d6), // Roll Perfect Conditions
                    DiceResults(2.d8, 2.d8, 2.d8) // Scatter 3 times up
                ),
                bounce = 2.d8 // Final bounce up
            ),
        )
        assertEquals(Weather.PERFECT_CONDITIONS, state.weather)
        assertEquals(BallState.ON_GROUND, state.ball.state)
        assertEquals(FieldCoordinate(18, 3), state.ball.location)
    }

    @Test
    fun changingWeather_scatterBackToReceiverField() {
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(13, 7),
                deviate = DiceResults(4.d8, 1.d6), // Deviate to [12,7] on Kickers sid
                kickoffEvent = arrayOf(
                    DiceResults(4.d6, 4.d6), // Roll Changing Weather
                    DiceResults(3.d6, 4.d6), // Roll Perfect Conditions
                    DiceResults(2.d8, 2.d8, 2.d8), // Scatter 3 times up
                    PlayerSelected("A1".playerId), // Touchback -> give to A1
                ),
                bounce = null
            ),
        )
        assertEquals(Weather.PERFECT_CONDITIONS, state.weather)
        assertEquals(BallState.CARRIED, state.ball.state)
        assertTrue(awayTeam[1.playerNo].hasBall())
    }

    @Test
    fun changingWeather_scatterBackToKickerField() {
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(13, 7),
                deviate = DiceResults(4.d8, 1.d6), // Deviate to [12,7] on Kickers side
                kickoffEvent = arrayOf(
                    DiceResults(4.d6, 4.d6), // Roll Changing Weather
                    DiceResults(3.d6, 4.d6), // Roll Perfect Conditions
                    DiceResults(5.d8, 5.d8, 2.d8), // Scatter 3 times up to the right, back to receivers side [14, 6]
                ),
                bounce = 2.d8 // Bounce to [14, 5]
            ),
        )
        assertEquals(Weather.PERFECT_CONDITIONS, state.weather)
        assertEquals(BallState.ON_GROUND, state.ball.state)
        assertEquals(FieldCoordinate(14, 5), state.ball.location)
    }

    @Test
    fun changingWeather_perfectWeatherWhenOutOfBounds() {
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(13, 0),
                deviate = DiceResults(2.d8, 1.d6), // Deviate out of bounds with exit at [13, 0]
                kickoffEvent = arrayOf(
                    DiceResults(4.d6, 4.d6), // Roll Changing Weather
                    DiceResults(3.d6, 4.d6), // Roll Perfect Conditions
                    PlayerSelected("A5".playerId)
                ),
                bounce = null
            ),
        )
        assertEquals(Weather.PERFECT_CONDITIONS, state.weather)
        assertEquals(BallState.CARRIED, state.ball.state)
        assertTrue(awayTeam[5.playerNo].hasBall())
    }



    @Test
    @Ignore
    fun QuickSnap() {

    }

    @Test
    @Ignore
    fun Blitz() {

    }

    @Test
    @Ignore
    fun OfficiousRef() {

    }

    @Test
    @Ignore
    fun PitchInvasion() {

    }

}
