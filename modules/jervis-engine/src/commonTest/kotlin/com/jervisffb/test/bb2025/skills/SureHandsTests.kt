package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.SelectRerollOption
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.rules.bb2025.skills.SureHands
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.RegularTeamReroll
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.SmartMoveTo
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.utils.SelectSkillReroll
import com.jervisffb.test.utils.SelectTeamReroll
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Class testing usage of the [SureHands] skill.
 */
class SureHandsTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun worksOnPickup() {
        awayTeam["A10".playerId].addSkill(SkillType.SURE_HANDS)
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.MOVE),
            SmartMoveTo(17, 7),
            1.d6, // Pickup
            SelectSkillReroll(SkillType.SURE_HANDS),
            6.d6,
            EndAction
        )
        assertTrue(awayTeam["A10".playerId].hasBall())
        assertNull(state.activePlayer)
        assertEquals(awayTeam, state.activeTeam)
    }

    @Test
    fun worksOnSuccessfulPickup() {
        awayTeam["A10".playerId].addSkill(SkillType.SURE_HANDS)
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.MOVE),
            SmartMoveTo(17, 7),
            6.d6, // Pickup
            SelectSkillReroll(SkillType.SURE_HANDS),
            6.d6,
            EndAction
        )
        assertTrue(awayTeam["A10".playerId].hasBall())
        assertNull(state.activePlayer)
        assertEquals(awayTeam, state.activeTeam)
    }

    @Test
    fun doNotWorkOnSecureTheBall() {
        awayTeam["A10".playerId].addSkill(SkillType.SURE_HANDS)
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.SECURE_THE_BALL),
            SmartMoveTo(17, 7),
            1.d6, // Pickup
        )
        assertFalse(controller.getAvailableActions().get<SelectRerollOption>().options.any { it.getRerollSource(state) is SureHands })
        controller.rollForward(
            SelectTeamReroll<RegularTeamReroll>(),
            6.d6,
        )
        assertTrue(awayTeam["A10".playerId].hasBall())
        assertNull(state.activePlayer)
        assertEquals(awayTeam, state.activeTeam)
    }

    @Ignore
    @Test
    fun preventStripBall() {
        // TODO
    }
}
