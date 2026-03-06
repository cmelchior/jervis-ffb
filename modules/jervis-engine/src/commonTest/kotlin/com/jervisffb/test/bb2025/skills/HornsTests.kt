package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.BlockTypeSelected
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.rules.bb2025.skills.Horns
import com.jervisffb.engine.rules.common.actions.BlockType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.standardBlock
import com.jervisffb.test.utils.SelectSingleBlockDieResult
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Class testing usage of the [Horns] skill.
 */
class HornsTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun addStrengthOnBlitz() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.addSkill(SkillType.HORNS)
        val defender = state.getPlayerById("H1".playerId)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLITZ),
        )
        assertEquals(3, attacker.strength)
        controller.rollForward(
            PlayerSelected(defender),
            PlayerSelected(defender),
            BlockTypeSelected(BlockType.STANDARD),
            DiceRollResults(6.dblock, 6.dblock),
            NoRerollSelected(),
            SelectSingleBlockDieResult(index = 1),
            DirectionSelected(Direction.LEFT),
            Cancel, // Do not follow up
        )
        assertEquals(4, attacker.strength)
        controller.rollForward(
            DiceRollResults(1.d6, 1.d6), // Armour roll
            EndAction
        )
        assertEquals(3, attacker.strength)
        assertNull(state.activePlayer)
    }

    @Test
    fun doNotAddStrengthOnBlock() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.addSkill(SkillType.HORNS)
        val defender = state.getPlayerById("H1".playerId)
        assertEquals(3, attacker.strength)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 6.dblock),
            DirectionSelected(Direction.DOWN_LEFT),
            Cancel,
        )
        assertEquals(3, attacker.strength)
        controller.rollForward(
            DiceRollResults(1.d6, 1.d6)
        )
        assertEquals(3, attacker.strength)
        assertNull(state.activePlayer)
    }
}
