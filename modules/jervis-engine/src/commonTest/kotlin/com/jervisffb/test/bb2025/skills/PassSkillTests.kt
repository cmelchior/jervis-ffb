package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.PassTypeSelected
import com.jervisffb.engine.actions.SelectRerollOption
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.getSkill
import com.jervisffb.engine.rules.bb2025.skills.Cannoneer
import com.jervisffb.engine.rules.bb2025.skills.Pass
import com.jervisffb.engine.rules.common.actions.PassType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.singleInstanceOf
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.SmartMoveTo
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.catch
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.moveTo
import com.jervisffb.test.pickup
import com.jervisffb.test.utils.SelectSkillReroll
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Class testing usage of the [Cannoneer] skill.
 */
class PassSkillTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        state.apply {
            awayTeam["A10".playerId].apply {
                addSkill(SkillType.PASS.id())
                passing = 2
            }
        }
        startDefaultGame()
    }

    @Test
    fun availableForPass() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            SmartMoveTo(14, 4),
            PassTypeSelected(PassType.STANDARD),
            FieldSquareSelected(14, 1),
            4.d6
        )
        assertTrue(
            controller.getAvailableActions().singleInstanceOf<SelectRerollOption>().options.any { it.getRerollSource(state) is Pass }
        )
        controller.rollForward(
            SelectSkillReroll(SkillType.PASS),
            6.d6,
        )
        assertEquals(true, awayTeam["A10".playerId].getSkill<Pass>().rerollUsed)
        controller.rollForward(
            *catch(6.d6)
        )
        assertTrue(awayTeam["A6".playerId].hasBall())
    }


    @Test
    fun availableForHailMaryPass() {
        awayTeam["A10".playerId].addSkill(SkillType.HAIL_MARY_PASS)
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            SmartMoveTo(14, 4),
            PassTypeSelected(PassType.HAIL_MARY_PASS),
            FieldSquareSelected(14, 1),
            4.d6
        )
        assertTrue(
            controller.getAvailableActions().singleInstanceOf<SelectRerollOption>().options.any { it.getRerollSource(state) is Pass }
        )
        controller.rollForward(
            SelectSkillReroll(SkillType.PASS),
            6.d6,
        )
        assertEquals(true, awayTeam["A10".playerId].getSkill<Pass>().rerollUsed)
        controller.rollForward(
            DiceRollResults(2.d8, 8.d8, 4.d8),
            *catch(6.d6)
        )
        assertTrue(awayTeam["A6".playerId].hasBall())
    }
}
