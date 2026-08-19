package com.jervisffb.test.bb2025.bb7

import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.test.JervisGameBB72025Test
import com.jervisffb.test.SmartMoveTo
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.utils.assertActive
import com.jervisffb.test.utils.assertBanned
import com.jervisffb.test.utils.putProne
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse

/**
 * This class test differences in rolling for Argue the Call compared
 * to normal standard BB2025.
 */
class BB7ArgueTheCallTests: JervisGameBB72025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun iDontCare() {
        val target = homeTeam["H1".playerId].also {
            it.putProne()
        }
        val fouler = awayTeam["A1".playerId]
        controller.rollForward(
            *activatePlayer(fouler, PlayerStandardActionType.FOUL),
            SmartMoveTo(7, 2),
            PlayerSelected(target), // Start foul
            DiceRollResults(2.d6, 2.d6), // Roll double -> Sent off
            Confirm, // Argue the call
            1.d6 // Roll "I Don't Care". It would have been "You are Outta Here" in normal BB2025
        )
        homeTeam.assertActive()
        fouler.assertBanned()
        assertFalse(awayTeam.coachBanned)
    }
}

