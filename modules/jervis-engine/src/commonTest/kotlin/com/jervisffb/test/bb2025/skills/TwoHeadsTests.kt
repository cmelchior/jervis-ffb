package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.context.DodgeRollContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.model.modifiers.DodgeRollModifier
import com.jervisffb.engine.rules.bb2025.skills.TwoHeads
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.moveTo
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Class testing usage of the [TwoHeads] skill.
 */
class TwoHeadsTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        state.apply {
            awayTeam["A1".playerId].apply {
                addSkill(SkillType.TWO_HEADS.id())
                agility = 4
            }
        }
        startDefaultGame()
    }

    @Test
    fun useTwoHeadsOnSingleDodge() {
        val player = awayTeam["A1".playerId]
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            3.d6, // Dodge roll, only successful if using Two Heads
            Confirm, // Use Two Heads
        )
        assertTrue(state.getContext<DodgeRollContext>().rollModifiers.contains(DodgeRollModifier.TWO_HEADS))
        controller.rollForward(
            NoRerollSelected(),
            EndAction,
        )
        assertEquals(FieldCoordinate(14, 5), player.location)
        assertEquals(PlayerState.STANDING, player.state)
    }

    @Test
    fun useTwoHeadsOnMultipleDodges() {
        val player = awayTeam["A1".playerId]
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE),
            *moveTo(12, 4),
            4.d6, // Dodge roll
            Confirm, // Use Two Heads,
            NoRerollSelected(),
            *moveTo(11, 5),
            5.d6,
            Confirm,
            NoRerollSelected(),
            EndAction
        )
        assertEquals(FieldCoordinate(11, 5), player.location)
        assertEquals(PlayerState.STANDING, player.state)
    }

    @Test
    fun doNotUseTwoHeadsOnDodge() {
        val player = awayTeam["A1".playerId]
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            3.d6, // Dodge roll, only successful if using Two Heads
            Cancel, // Do not use Two Heads
        )
        assertFalse(state.getContext<DodgeRollContext>().rollModifiers.contains(DodgeRollModifier.TWO_HEADS))
        controller.rollForward(
            NoRerollSelected(),
            DiceRollResults(1.d6, 1.d6), // Armour roll
        )
        assertEquals(FieldCoordinate(14, 5), player.location)
        assertEquals(PlayerState.PRONE, player.state)
    }
}
