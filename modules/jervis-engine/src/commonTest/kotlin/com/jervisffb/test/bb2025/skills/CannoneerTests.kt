package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PassTypeSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.model.modifiers.AccuracyModifier
import com.jervisffb.engine.rules.bb2025.skills.Cannoneer
import com.jervisffb.engine.rules.common.actions.PassType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.procedures.actions.pass.PassContext
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.moveTo
import com.jervisffb.test.pickup
import com.jervisffb.test.throwBall
import com.jervisffb.test.utils.hasSkill
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Class testing usage of the [Cannoneer] skill.
 */
class CannoneerTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        state.apply {
            awayTeam["A10".playerId].apply {
                addSkill(SkillType.CANNONEER.id())
                passing = 2
            }
        }
        startDefaultGame()
    }

    @Test
    fun useCannoneerOnLongPass() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            PassTypeSelected(PassType.STANDARD),
            FieldSquareSelected(8, 7),
            3.d6, // Throw, will only be an accurate throw because of the Cannoneer skill
            Confirm, // Use Cannoneer
            NoRerollSelected(),
        )
        assertTrue(state.getContext<PassContext>().passingModifiers.contains(AccuracyModifier.CANNONEER))
        controller.rollForward(
            Cancel, // Do not intercept
            4.d8 // Bounce
        )
        assertEquals(FieldCoordinate(7, 7), state.singleBall().location)
        assertEquals(homeTeam, state.activeTeam)
    }

    @Test
    fun useAccurateOnLongBomb() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            PassTypeSelected(PassType.STANDARD),
            FieldSquareSelected(5, 7),
            4.d6, // Throw, will only be an accurate throw because of the Cannoneer skill
            Confirm, // Use Cannoneer
            NoRerollSelected(),
        )
        assertTrue(state.getContext<PassContext>().passingModifiers.contains(AccuracyModifier.CANNONEER))
        controller.rollForward(
            Cancel, // Do not intercept
            4.d8 // Bounce
        )
        assertEquals(FieldCoordinate(4, 7), state.singleBall().location)
        assertEquals(homeTeam, state.activeTeam)
    }

    @Test
    fun doesNotWorkOnQuickPass() {
        assertTrue(awayTeam["A10".playerId].hasSkill<Cannoneer>())
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            PassTypeSelected(PassType.STANDARD),
            FieldSquareSelected(18, 7),
            *throwBall(6.d6) // Cannoneer is skipped
        )
        assertFalse(state.getContext<PassContext>().passingModifiers.contains(AccuracyModifier.CANNONEER))
        controller.rollForward(
            5.d8 // Bounce
        )
        assertEquals(FieldCoordinate(19, 7), state.singleBall().location)
        assertEquals(homeTeam, state.activeTeam)
    }

    @Test
    fun doNotUseCannoneer() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            PassTypeSelected(PassType.STANDARD),
            FieldSquareSelected(8, 7),
            4.d6, // Throw, will only be an accurate throw because of the Cannoneer skill
            Cancel, // Do not Cannoneer
            NoRerollSelected(),
        )
        assertFalse(state.getContext<PassContext>().passingModifiers.contains(AccuracyModifier.CANNONEER))
        controller.rollForward(
            Cancel, // Do not intercept
            2.d8 // Bounce
        )
        assertEquals(FieldCoordinate(8, 6), state.singleBall().location)
        assertEquals(homeTeam, state.activeTeam)
    }
}
