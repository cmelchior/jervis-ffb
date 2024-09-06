package dk.ilios.jervis.tables

import dk.ilios.jervis.JervisGameTest
import dk.ilios.jervis.actions.DiceResults
import dk.ilios.jervis.actions.FieldSquareSelected
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.defaultKickOffHomeTeam
import dk.ilios.jervis.defaultPregame
import dk.ilios.jervis.defaultSetup
import dk.ilios.jervis.ext.d6
import dk.ilios.jervis.ext.d8
import dk.ilios.jervis.ext.playerId
import dk.ilios.jervis.ext.playerNo
import dk.ilios.jervis.model.BallState
import dk.ilios.jervis.procedures.FullGame
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

    fun highKick_acrossLoS() {
        TODO()
    }

    fun highKick_noValidPlayers() {
        TODO()
    }


    @Test
    @Ignore
    fun CheeringFans() {

    }

    @Test
    @Ignore
    fun BrilliantCoaching() {

    }

    @Test
    @Ignore
    fun ChangingWeather() {

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
