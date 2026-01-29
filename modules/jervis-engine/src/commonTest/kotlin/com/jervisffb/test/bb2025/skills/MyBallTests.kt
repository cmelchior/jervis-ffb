package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectPlayerAction
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.rules.bb2025.skills.MyBall
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.ext.rollForward
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Class testing usage of the [MyBall] skill.
 */
class MyBallTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
        awayTeam["A1".playerId].addSkill(SkillType.MY_BALL)
    }

    @Test
    fun cannotDeclarePassAction() {
        controller.rollForward(
            PlayerSelected("A1".playerId),
        )
        assertTrue(controller.getAvailableActions().get<SelectPlayerAction>().actions.none { it.type == PlayerStandardActionType.PASS })
    }

    @Test
    fun cannotDeclareHandOffAction() {
        controller.rollForward(
            PlayerSelected("A1".playerId),
        )
        assertTrue(controller.getAvailableActions().get<SelectPlayerAction>().actions.none { it.type == PlayerStandardActionType.HAND_OFF })
    }

    @Ignore
    @Test
    fun cannotUseFumblerooskie() {
        // Add test when Fumblerooskie is implemented
    }
}
