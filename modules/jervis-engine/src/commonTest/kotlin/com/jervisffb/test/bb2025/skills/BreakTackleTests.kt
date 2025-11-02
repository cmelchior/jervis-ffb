package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.PlayerNo
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.context.DodgeRollContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.model.modifiers.BreakTackleModifier
import com.jervisffb.engine.rules.bb2025.skills.BreakTackle
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.RegularTeamReroll
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.utils.SelectTeamReroll
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Class testing usage of the [BreakTackle] skill
 */
class BreakTackleTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        state.apply {
            // Should be on LoS
            awayTeam[PlayerNo(1)].apply {
                addSkill(SkillType.BREAK_TACKLE.id())
                strength = 3
                agility = 4
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
            DiceRollResults(3.d6), // Dodge roll, is not enough
            Confirm // Use Break Tackle
        )
        val context = state.getContext<DodgeRollContext>()
        assertEquals(1, context.rollModifiers.size)
        assertEquals(BreakTackleModifier(player.strength, rules.baseVersion), context.rollModifiers.first())
        assertTrue(context.isSuccess)
        assertTrue(player.getSkill(SkillType.BREAK_TACKLE).used)
        controller.rollForward(
            NoRerollSelected(),
        )
        assertEquals(FieldCoordinate(14, 5), player.location)
        assertEquals(PlayerState.STANDING, player.state)
        assertTrue(player.getSkill(SkillType.BREAK_TACKLE).used)
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
            DiceRollResults(2.d6), // Dodge roll, is not enough
            Confirm, // Use Break Tackle, still not enough
            SelectTeamReroll<RegularTeamReroll>(),
            DiceRollResults(3.d6), // Dodge roll, should now succeed (but only with Break Tackle)
        )
        assertEquals(FieldCoordinate(14, 5), player.location)
        assertEquals(PlayerState.STANDING, player.state)
        assertTrue(player.getSkill(SkillType.BREAK_TACKLE).used)
    }

    @Test
    fun breakTackleModifierForS3() {
        assertEquals(1, BreakTackleModifier(3, rules.baseVersion).modifier)
    }

    @Test
    fun breakTackleModifierForS4() {
        assertEquals(2, BreakTackleModifier(4, rules.baseVersion).modifier)
    }

    @Test
    fun breakTackleModifierForS5() {
        assertEquals(3, BreakTackleModifier(5, rules.baseVersion).modifier)
    }
}
