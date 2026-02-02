package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.PassTypeSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.bb2025.skills.ExtraArms
import com.jervisffb.engine.rules.common.actions.PassType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.procedures.actions.pass.PassContext
import com.jervisffb.engine.rules.common.procedures.actions.pass.PassingType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.SmartMoveTo
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.moveTo
import com.jervisffb.test.pickup
import com.jervisffb.test.throwBall
import com.jervisffb.test.utils.SelectSkillReroll
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Class testing usage of the [ExtraArms] skill.
 */
class ExtraArmsTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun chooseNotToUseExtraArms() {
        awayTeam["A7".playerId].addSkill(SkillType.EXTRA_ARMS)
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            PassTypeSelected(PassType.STANDARD),
            FieldSquareSelected(15, 1),
            *throwBall(6.d6),
            Cancel, // Do not use Extra Arms
            2.d6, // Catch fails
            SelectSkillReroll(SkillType.CATCH),
            3.d6
        )
        assertTrue(awayTeam["A7".playerId].hasBall())
    }

    @Test
    fun workOnCatch() {
        awayTeam["A7".playerId].addSkill(SkillType.EXTRA_ARMS)
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            PassTypeSelected(PassType.STANDARD),
            FieldSquareSelected(15, 1),
            *throwBall(6.d6),
            Confirm, // Do use Extra Arms
            1.d6, // Catch fails
            SelectSkillReroll(SkillType.CATCH),
            2.d6
        )
        assertTrue(awayTeam["A7".playerId].hasBall())
    }

    @Test
    fun workOnPickup() {
        val player = awayTeam["A10".playerId]
        player.addSkill(SkillType.EXTRA_ARMS)
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.MOVE),
            *moveTo(17, 7),
            Confirm, // Use Extra Arms
            *pickup(2.d6),
        )
        assertTrue(player.hasBall())
    }

    @Test
    fun workOnSecureTheBall() {
        val player = awayTeam["A10".playerId]
        player.addSkill(SkillType.EXTRA_ARMS)
        assertEquals(3, player.agility)
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.SECURE_THE_BALL),
            *moveTo(17, 7),
            Confirm, // Use Extra Arms
            *pickup(2.d6),
        )
        assertTrue(player.hasBall())
    }

    @Test
    fun workOnInterception() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            SmartMoveTo(12, 3),
            PassTypeSelected(PassType.STANDARD),
            FieldSquareSelected(13, 9),
            *throwBall(6.d6),
        )
        assertEquals(PassingType.ACCURATE, state.getContext<PassContext>().passingResult)
        homeTeam["H2".playerId].apply {
            agility = 1
            addSkill(SkillType.EXTRA_ARMS)
        }
        awayTeam["A1".playerId].hasTackleZones = false // Remove mark
        awayTeam["A2".playerId].hasTackleZones = false // Remove mark
        awayTeam["A3".playerId].hasTackleZones = false // Remove mark
        controller.rollForward(
            PlayerSelected("H2".playerId), // Select Interceptor
            Confirm, // Use Extra Arms
            3.d6, // Intercept (with -3 (accurate) + 1 extra arms modifier) - Will succeed
        )
        assertTrue(homeTeam["H2".playerId].hasBall())
    }
}
