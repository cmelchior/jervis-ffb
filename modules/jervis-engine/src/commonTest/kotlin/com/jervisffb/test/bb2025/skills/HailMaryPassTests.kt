package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.PassTypeSelected
import com.jervisffb.engine.actions.SelectFieldLocation
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.model.modifiers.AccuracyModifier
import com.jervisffb.engine.rules.bb2025.skills.HailMaryPass
import com.jervisffb.engine.rules.common.actions.PassType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.procedures.actions.pass.PassContext
import com.jervisffb.engine.rules.common.procedures.actions.pass.PassingType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.rules.common.tables.Range
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.moveTo
import com.jervisffb.test.pickup
import com.jervisffb.test.throwBall
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Class testing usage of the [HailMaryPass] skill.
 */
class HailMaryPassTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        state.apply {
            awayTeam["A10".playerId].apply {
                addSkill(SkillType.HAIL_MARY_PASS.id())
                passing = 2
            }
        }
        startDefaultGame()
    }

    @Test
    fun canSelectSquaresBeyondLongBombRange() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            PassTypeSelected(PassType.HAIL_MARY_PASS),
        )
        assertEquals(
            Range.OUT_OF_RANGE,
            rules.rangeRuler.measure(awayTeam["A10".playerId], FieldCoordinate(0, 0))
        )
        assertTrue(controller.getAvailableActions().get<SelectFieldLocation>().squares.any { it.x == 0 && it.y == 0 })
    }

    @Test
    fun cannotInterceptHailMaryPass() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            PassTypeSelected(PassType.HAIL_MARY_PASS),
            FieldSquareSelected(0, 7), // Will pass over LoS with players from Home Team
            *throwBall(6.d6),
            DiceRollResults(2.d8, 5.d8, 7.d8),
            4.d8 // Bounce
        )
        assertEquals(FieldCoordinate(0, 7), state.singleBall().coordinates)
        assertEquals(homeTeam, state.activeTeam)
    }

    @Test
    fun quickPassDistanceCountAsLongBomb() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            PassTypeSelected(PassType.HAIL_MARY_PASS),
            FieldSquareSelected(18, 7),
        )
        assertEquals(Range.QUICK_PASS, rules.rangeRuler.measure(awayTeam["A10".playerId], FieldCoordinate(18, 7)))
        state.getContext<PassContext>().let { context ->
            assertEquals(Range.LONG_BOMB, context.range)
            assertContains(context.passingModifiers, AccuracyModifier.LONG_BOMB)
        }
        controller.rollForward(
            *throwBall(6.d6),
            DiceRollResults(3.d8, 3.d8, 3.d8),
            3.d8 // Bounce
        )
        assertEquals(FieldCoordinate(22, 3), state.singleBall().coordinates)
        assertEquals(homeTeam, state.activeTeam)
    }

    @Test
    fun beyondLongBombDistanceCountAsLongBomb() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            PassTypeSelected(PassType.HAIL_MARY_PASS),
            FieldSquareSelected(0, 0),
        )
        assertEquals(Range.OUT_OF_RANGE, rules.rangeRuler.measure(awayTeam["A10".playerId], FieldCoordinate(0, 0)))
        state.getContext<PassContext>().let { context ->
            assertEquals(Range.LONG_BOMB, context.range)
            assertContains(context.passingModifiers, AccuracyModifier.LONG_BOMB)
        }
        controller.rollForward(
            *throwBall(6.d6),
            DiceRollResults(7.d8, 7.d8, 7.d8),
            7.d8 // Bounce
        )
        assertEquals(FieldCoordinate(0, 4), state.singleBall().coordinates)
        assertEquals(homeTeam, state.activeTeam)
    }

    @Test
    fun canNeverBeAccurate() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            PassTypeSelected(PassType.HAIL_MARY_PASS),
            FieldSquareSelected(18, 7),
            *throwBall(6.d6),
        )
        val context = state.getContext<PassContext>()
        assertEquals(PassingType.INACCURATE, context.passingResult)
    }

    @Ignore
    @Test
    fun alsoWorksOnBombs() {
        // Wait for Bombadier support
    }
}
