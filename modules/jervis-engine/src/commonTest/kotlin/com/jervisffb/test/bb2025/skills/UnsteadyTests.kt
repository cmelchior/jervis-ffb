package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectPlayerAction
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.PlayerNo
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.hasSkill
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.ext.rollForward
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Class testing usage of the Unstead*(Passive) [com.jervisffb.engine.rules.bb2025.skills.Unsteady]
 * skill.
 */
class UnsteadyTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        awayTeam[PlayerNo(1)].apply {
            addSkill(SkillType.UNSTEADY.id())
            strength = 4
        }
        startDefaultGame()
    }

    @Test
    fun canChooseSecureBallIfNotUnsteady() {
        controller.rollForward(
            PlayerSelected("A8".playerId),
        )
        assertFalse(awayTeam["A8".playerId].hasSkill(SkillType.UNSTEADY))
        assertTrue(
            controller
                .getAvailableActions()
                .get<SelectPlayerAction>()
                .actions.any { it.type == PlayerStandardActionType.SECURE_THE_BALL }
        )
        assertTrue(
            controller
                .getAvailableActions()
                .get<SelectPlayerAction>()
                .actions.any { it.type == PlayerStandardActionType.SECURE_THE_BALL }
        )
    }

    @Test
    fun cannotUseSecureTheBallIfUnsteady() {
        controller.rollForward(
            PlayerSelected("A1".playerId),
        )
        assertTrue(
            controller
                .getAvailableActions()
                .get<SelectPlayerAction>()
                .actions.none { it.type == PlayerStandardActionType.SECURE_THE_BALL }
        )
    }

    @Test
    fun cannotUseSecureTheBallIfUnsteadyAndProne() {
        awayTeam["A1".playerId].state = PlayerState.PRONE
        controller.rollForward(
            PlayerSelected("A1".playerId),
        )
        assertTrue(
            controller
                .getAvailableActions()
                .get<SelectPlayerAction>()
                .actions.none { it.type == PlayerStandardActionType.SECURE_THE_BALL }
        )
    }
}
