package com.jervisffb.test.pregame

import com.jervisffb.test.JervisGameTest
import com.jervisffb.engine.ext.d3
import com.jervisffb.engine.rules.bb2020.procedures.FanFactorRolls
import kotlin.test.Test
import kotlin.test.assertEquals

class FanFactorRollTests: JervisGameTest() {

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
