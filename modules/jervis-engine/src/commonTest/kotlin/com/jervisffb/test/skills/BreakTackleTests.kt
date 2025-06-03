package com.jervisffb.test.skills

import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.PlayerNo
import com.jervisffb.engine.model.context.DodgeRollContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.model.modifiers.BreakTackleModifier
import com.jervisffb.engine.rules.PlayerStandardActionType
import com.jervisffb.engine.rules.bb2020.skills.BreakTackle
import com.jervisffb.engine.rules.bb2020.skills.RegularTeamReroll
import com.jervisffb.engine.rules.bb2020.skills.SkillType
import com.jervisffb.test.JervisGameTest
import com.jervisffb.test.SelectTeamReroll
import com.jervisffb.test.ext.rollForward
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


/**
 * Class testing usage of the [BreakTackle] skill
 */
class BreakTackleTests: JervisGameTest() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        state.apply {
            // Should be on LoS
            awayTeam[PlayerNo(1)].apply {
                addSkill(SkillType.BREAK_TACKLE.id())
                strength = 4
            }
        }
        startDefaultGame()
    }

    @Test
    fun useBreakTackleOnDodge() {
        val player = state.getPlayerById("A1".playerId)
        controller.rollForward(
            // Dodge A1 away using Break Tackle
            PlayerSelected(player),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.STANDARD),
            FieldSquareSelected(FieldCoordinate(14, 5)),
            DiceRollResults(2.d6), // Dodge roll, is not enough
            Confirm // Use Break Tackle
        )
        val context = state.getContext<DodgeRollContext>()
        assertEquals(1, context.rollModifiers.size)
        assertEquals(BreakTackleModifier(player.strength), context.rollModifiers.first())
        assertTrue(context.isSuccess)
        assertTrue(player.getSkill<BreakTackle>().used)
    }

    @Test
    fun breakTackleAlsoAppliesToReroll() {
        val player = state.getPlayerById("A1".playerId)
        controller.rollForward(
            // Dodge A1 away using Break Tackle
            PlayerSelected(player),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.STANDARD),
            FieldSquareSelected(FieldCoordinate(14, 5)),
            DiceRollResults(1.d6), // Dodge roll, is not enough
            Confirm, // Use Break Tackle, still not enough
            SelectTeamReroll<RegularTeamReroll>(),
            DiceRollResults(2.d6), // Dodge roll, should now succeed
        )

        val context = state.getContext<DodgeRollContext>()
        assertEquals(1, context.rollModifiers.size)
        assertEquals(BreakTackleModifier(player.strength), context.rollModifiers.first())
        assertTrue(player.getSkill<BreakTackle>().used)
        assertTrue(context.isSuccess)
    }

    @Test
    fun breakTackleModifierForS4() {
        assertEquals(1, BreakTackleModifier(4).modifier)
    }

    @Test
    fun breakTackleModifierForS5() {
        assertEquals(2, BreakTackleModifier(5).modifier)
    }
}
