package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.BlockTypeSelected
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.modifiers.StatModifier
import com.jervisffb.engine.rules.bb2025.skills.Dauntless
import com.jervisffb.engine.rules.common.actions.BlockType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.sum
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.dauntlessRoll
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.standardBlock
import com.jervisffb.test.utils.SelectSingleBlockDieResult
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Class testing usage of the [Dauntless] skill.
 */
class DauntlessTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun workOnBlock() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.apply {
            strength = 1
            baseStrength = 1
            strengthModifiers.clear()
            addSkill(SkillType.DAUNTLESS)
        }
        val defender = state.getPlayerById("H1".playerId)
        assertEquals(1, attacker.strength)
        assertEquals(3, defender.strength)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            PlayerSelected(defender),
            *dauntlessRoll(3.d6),
        )
        assertEquals(2, attacker.strengthModifiers.first().modifier)
        assertEquals(3, attacker.strength)
        controller.rollForward(
            DiceRollResults(4.dblock),
            NoRerollSelected(),
            SelectSingleBlockDieResult(),
            DirectionSelected(Direction.LEFT),
            Cancel, // Do not follow up
        )
        assertNull(state.activePlayer)
        assertEquals(1, attacker.strength)
    }

    @Test
    fun workOnBlitz() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.apply {
            strength = 1
            baseStrength = 1
            strengthModifiers.clear()
            addSkill(SkillType.DAUNTLESS)
        }
        val defender = state.getPlayerById("H1".playerId)
        assertEquals(1, attacker.strength)
        assertEquals(3, defender.strength)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLITZ),
            PlayerSelected(defender),
            PlayerSelected(defender),
            BlockTypeSelected(BlockType.STANDARD),
            *dauntlessRoll(3.d6),
        )
        assertEquals(2, attacker.strengthModifiers.first().modifier)
        assertEquals(3, attacker.strength)
        controller.rollForward(
            DiceRollResults(4.dblock),
            NoRerollSelected(),
            SelectSingleBlockDieResult(),
            DirectionSelected(Direction.LEFT),
            Cancel, // Do not follow up
        )
        // Strength is removed after the "block" part of the Blitz
        assertEquals(attacker, state.activePlayer)
        assertEquals(1, attacker.strength)
    }

    @Test
    fun dauntlessRolledBeforeOtherModifiers() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.apply {
            strength = 1
            baseStrength = 1
            strengthModifiers.clear()
            addSkill(SkillType.DAUNTLESS)
            addSkill(SkillType.HORNS)
        }
        val defender = state.getPlayerById("H1".playerId)
        assertEquals(1, attacker.strength)
        assertEquals(3, defender.strength)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLITZ),
            PlayerSelected(defender),
            PlayerSelected(defender),
            BlockTypeSelected(BlockType.STANDARD),
            *dauntlessRoll(3.d6),
        )
        assertEquals(3, attacker.strengthModifiers.sum()) // +2 Dauntless + 1 Horns
        assertEquals(4, attacker.strength)
        controller.rollForward(
            DiceRollResults(1.dblock, 4.dblock),
            NoRerollSelected(),
            SelectSingleBlockDieResult(index = 1),
            DirectionSelected(Direction.LEFT),
            Cancel, // Do not follow up
        )
        // Strength is removed after the "block" part of the Blitz
        assertEquals(attacker, state.activePlayer)
        assertEquals(1, attacker.strength)
    }

    @Test
    fun notUsedIfEqualStrengthOrStronger() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.addSkill(SkillType.DAUNTLESS)
        val defender = state.getPlayerById("H1".playerId)
        assertEquals(3, attacker.strength)
        assertEquals(3, defender.strength)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 3.dblock),
            DirectionSelected(Direction.LEFT),
            Cancel,
        )
        assertNull(state.activePlayer)
    }

    @Test
    fun mustBeatOpponentStrengthToWork() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.apply {
            strength = 1
            baseStrength = 1
            strengthModifiers.clear()
            addSkill(SkillType.DAUNTLESS)
        }
        val defender = state.getPlayerById("H1".playerId)
        assertEquals(1, attacker.strength)
        assertEquals(3, defender.strength)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            PlayerSelected(defender),
            *dauntlessRoll(2.d6),
        )
        assertTrue(attacker.strengthModifiers.isEmpty())
        assertEquals(1, attacker.strength)
        controller.rollForward(
            DiceRollResults(1.dblock, 1.dblock, 4.dblock),
            NoRerollSelected(),
            SelectSingleBlockDieResult(index = 2),
            DirectionSelected(Direction.LEFT),
            Cancel, // Do not follow up
        )
        assertNull(state.activePlayer)
        assertEquals(1, attacker.strength)
    }

    @Test
    fun ignoreTemporaryModifiers() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.apply {
            baseStrength = 2
            strengthModifiers.add(StatModifier(
                type = StatModifier.Type.ST,
                modifier = 2,
                description = "Temporary modifier",
                expiresAt = Duration.END_OF_GAME
            ))
            rules.updatePlayerStat(attacker, StatModifier.Type.ST)
            addSkill(SkillType.DAUNTLESS)
        }
        val defender = state.getPlayerById("H1".playerId)
        defender.apply {
            baseStrength = 6
            strengthModifiers.add(StatModifier(
                type = StatModifier.Type.ST,
                modifier = -2,
                description = "Temporary modifier",
                expiresAt = Duration.END_OF_GAME
            ))
            rules.updatePlayerStat(defender, StatModifier.Type.ST)
        }
        assertEquals(4, attacker.strength)
        assertEquals(4, defender.strength)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            PlayerSelected(defender),
            *dauntlessRoll(5.d6), // Ignoring temp modifiers, the diff is 4
        )
        assertEquals(8, attacker.strength)
        assertEquals(4, defender.strength)
        controller.rollForward(
            DiceRollResults(1.dblock, 4.dblock),
            NoRerollSelected(),
            SelectSingleBlockDieResult(index = 1),
            DirectionSelected(Direction.LEFT),
            Cancel, // Do not follow up
        )
        assertNull(state.activePlayer)
        assertEquals(4, attacker.strength)
    }
}
