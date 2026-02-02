package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PassTypeSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.model.modifiers.AccuracyModifier
import com.jervisffb.engine.rules.bb2025.skills.Accurate
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
 * Class testing usage of the [Accurate] skill.
 */
class AccurateTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        state.apply {
            awayTeam["A10".playerId].apply {
                addSkill(SkillType.ACCURATE.id())
                passing = 4
            }
        }
        startDefaultGame()
    }

    @Test
    fun useAccurateOnQuickPass() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            PassTypeSelected(PassType.STANDARD),
            FieldSquareSelected(16, 6),
            3.d6, // Throw, will only be an accurate throw because of the Accurate skill
            Confirm, // Use Accurate
            NoRerollSelected(),
        )
        assertTrue(state.getContext<PassContext>().passingModifiers.contains(AccuracyModifier.ACCURATE))
        controller.rollForward(
            1.d8 // Bounce
        )
        assertEquals(FieldCoordinate(15, 5), state.singleBall().location)
        assertEquals(homeTeam, state.activeTeam)
    }

    @Test
    fun useAccurateOnShortPass() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            PassTypeSelected(PassType.STANDARD),
            FieldSquareSelected(23, 7),
            4.d6, // Throw, will only be an accurate throw because of the Accurate skill
            Confirm, // Use Accurate
            NoRerollSelected(),
        )
        assertTrue(state.getContext<PassContext>().passingModifiers.contains(AccuracyModifier.ACCURATE))
        controller.rollForward(
            1.d8 // Bounce
        )
        assertEquals(FieldCoordinate(22, 6), state.singleBall().location)
        assertEquals(homeTeam, state.activeTeam)
    }

    @Test
    fun doesNotWorkOnLongPass() {
        assertTrue(awayTeam["A10".playerId].hasSkill<Accurate>())
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            PassTypeSelected(PassType.STANDARD),
            FieldSquareSelected(25, 7),
            *throwBall(6.d6), // Accurate is skipped
        )
        assertFalse(state.getContext<PassContext>().passingModifiers.contains(AccuracyModifier.ACCURATE))
        controller.rollForward(
            2.d8 // Bounce
        )
        assertEquals(FieldCoordinate(25, 6), state.singleBall().location)
        assertEquals(homeTeam, state.activeTeam)
    }

    @Test
    fun doNotUseAccurate() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            PassTypeSelected(PassType.STANDARD),
            FieldSquareSelected(16, 6),
            3.d6, // Throw, will only be an accurate throw because of the Accurate skill
            Cancel, // Do not use Accurate
            NoRerollSelected(),
        )
        assertFalse(state.getContext<PassContext>().passingModifiers.contains(AccuracyModifier.ACCURATE))
        controller.rollForward(
            DiceRollResults(2.d8, 2.d8, 4.d8), // Scatter
            7.d8 // Bounce
        )
        assertEquals(FieldCoordinate(15, 5), state.singleBall().location)
        assertEquals(homeTeam, state.activeTeam)
    }
}
