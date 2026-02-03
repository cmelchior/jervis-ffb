package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.PassTypeSelected
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.rules.bb2025.skills.SafePass
import com.jervisffb.engine.rules.common.actions.PassType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.moveTo
import com.jervisffb.test.pickup
import com.jervisffb.test.throwBall
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Class testing usage of the [SafePass] skill.
 */
class SafePassTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        awayTeam["A10".playerId].addSkill(SkillType.SAFE_PASS.id())
        startDefaultGame()
    }

    @Test
    fun worksOnPass() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            PassTypeSelected(PassType.STANDARD),
            FieldSquareSelected(18, 7),
            *throwBall(1.d6),
            Confirm // Use Safe Pass
        )
        assertTrue(awayTeam["A10".playerId].hasBall())
        assertNull(state.activePlayer)
        assertEquals(awayTeam, state.activeTeam)
    }

    @Test
    fun onlyWorksOnNaturalOne() {
        val player = awayTeam["A1".playerId]
        player.addSkill(SkillType.SAFE_PASS)
        SetBallState.carried(state.singleBall(), player).execute(state)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.PASS),
            PassTypeSelected(PassType.STANDARD),
            FieldSquareSelected(25, 7),
            *throwBall(2.d6),
            2.d8 // Bounce from fumble
        )
        assertFalse(awayTeam["A1".playerId].hasBall())
        assertNull(state.activePlayer)
        assertEquals(homeTeam, state.activeTeam)
    }

    @Test
    fun worksOnHailMaryPass() {
        awayTeam["A10".playerId].addSkill(SkillType.HAIL_MARY_PASS)
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            PassTypeSelected(PassType.HAIL_MARY_PASS),
            FieldSquareSelected(0, 7),
            *throwBall(1.d6),
            Confirm // Use Safe Pass
        )
        assertTrue(awayTeam["A10".playerId].hasBall())
        assertNull(state.activePlayer)
        assertEquals(awayTeam, state.activeTeam)
    }
}
