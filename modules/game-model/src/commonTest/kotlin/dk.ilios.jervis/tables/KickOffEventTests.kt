package dk.ilios.jervis.tables

import dk.ilios.jervis.JervisGameTest
import dk.ilios.jervis.actions.DiceResults
import dk.ilios.jervis.defaultKickOffHomeTeam
import dk.ilios.jervis.defaultPregame
import dk.ilios.jervis.defaultSetup
import dk.ilios.jervis.ext.d6
import dk.ilios.jervis.procedures.FullGame
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

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
    @Ignore
    fun HighKick() {

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
