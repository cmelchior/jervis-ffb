package dk.ilios.jervis.pregame

import dk.ilios.jervis.GameFlowTests
import dk.ilios.jervis.ext.d3
import dk.ilios.jervis.procedures.FanFactorRolls
import kotlin.test.Test
import kotlin.test.assertEquals

class FanFactorRollTests: GameFlowTests() {

    @Test
    fun rollingForFanFactor() {
        controller.startTestMode(FanFactorRolls)
        controller.rollForward(
            1.d3, // Home team roll
            2.d3, // Away team roll
        )
        assertEquals(2, state.homeTeam.fanFactor)
        assertEquals(1, state.homeTeam.dedicatedFans)
        assertEquals(4, state.awayTeam.fanFactor)
        assertEquals(2, state.awayTeam.dedicatedFans)
    }

}
