package com.jervisffb.test.skills

import com.jervisffb.engine.actions.BlockTypeSelected
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.BlockType
import com.jervisffb.engine.rules.PlayerStandardActionType
import com.jervisffb.engine.rules.bb2020.skills.Block
import com.jervisffb.engine.rules.bb2020.skills.SkillType
import com.jervisffb.test.JervisGameTest
import com.jervisffb.test.utils.SelectSingleDieResult
import com.jervisffb.test.ext.addNewSkill
import com.jervisffb.test.ext.rollForward
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Class testing usage of the [Block] skill
 */
class BlockTests: JervisGameTest() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun bothDown_withoutBlock() {
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        controller.rollForward(
            PlayerSelected(attacker.id),
            PlayerActionSelected(PlayerStandardActionType.BLOCK),
            PlayerSelected(defender.id),
            BlockTypeSelected(BlockType.STANDARD),
            2.dblock,
            NoRerollSelected(),
            SelectSingleDieResult(),
        )
        assertEquals(FieldCoordinate(13, 5), attacker.location)
        assertEquals(PlayerState.KNOCKED_DOWN, attacker.state)
        assertEquals(FieldCoordinate(12, 5), defender.location)
        assertEquals(PlayerState.KNOCKED_DOWN, defender.state)
    }

    @Test
    fun bothDown_withBlock() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.extraSkills.addNewSkill(attacker, SkillType.BLOCK)
        val defender = state.getPlayerById("H1".playerId)
        defender.extraSkills.addNewSkill(defender, SkillType.BLOCK)
        controller.rollForward(
            PlayerSelected(attacker.id),
            PlayerActionSelected(PlayerStandardActionType.BLOCK),
            PlayerSelected(defender.id),
            BlockTypeSelected(BlockType.STANDARD),
            2.dblock,
            NoRerollSelected(),
            SelectSingleDieResult(),
            Confirm, // Defender uses block
            Confirm, // Attacker uses block
        )
        assertEquals(FieldCoordinate(13, 5), attacker.location)
        assertEquals(PlayerState.STANDING, attacker.state)
        assertEquals(FieldCoordinate(12, 5), defender.location)
        assertEquals(PlayerState.STANDING, defender.state)
    }

    @Test
    fun bothDown_usingBlockIsOptional() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.extraSkills.addNewSkill(attacker, SkillType.BLOCK)
        val defender = state.getPlayerById("H1".playerId)
        defender.extraSkills.addNewSkill(defender, SkillType.BLOCK)
        controller.rollForward(
            PlayerSelected(attacker.id),
            PlayerActionSelected(PlayerStandardActionType.BLOCK),
            PlayerSelected(defender.id),
            BlockTypeSelected(BlockType.STANDARD),
            2.dblock,
            NoRerollSelected(),
            SelectSingleDieResult(),
            Confirm, // Defender uses block
            Cancel, // Attacker doesn't use block
        )
        assertTrue(state.isTurnOver())
        assertEquals(FieldCoordinate(13, 5), attacker.location)
        assertEquals(PlayerState.KNOCKED_DOWN, attacker.state)
        assertEquals(FieldCoordinate(12, 5), defender.location)
        assertEquals(PlayerState.STANDING, defender.state)
    }
}
