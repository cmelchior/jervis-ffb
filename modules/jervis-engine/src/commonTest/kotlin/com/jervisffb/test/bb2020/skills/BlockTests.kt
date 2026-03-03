package com.jervisffb.test.bb2020.skills

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.bb2020.skills.Block
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2020Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.ext.addNewSkill
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.standardBlock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Class testing usage of the [Block] skill
 */
class BlockTests: JervisGameBB2020Test() {

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
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 2.dblock),
        )
        assertEquals(FieldCoordinate(12, 5), defender.location)
        assertEquals(FieldCoordinate(13, 5), attacker.location)
        assertEquals(PlayerState.KNOCKED_DOWN, defender.state)
        assertEquals(PlayerState.STANDING, attacker.state)
        controller.rollForward(
            DiceRollResults(1.d6, 1.d6),
        )
        assertEquals(PlayerState.PRONE, defender.state)
        assertEquals(PlayerState.KNOCKED_DOWN, attacker.state)
        controller.rollForward(
            DiceRollResults(1.d6, 1.d6),
        )
        assertEquals(PlayerState.PRONE, defender.state)
        assertEquals(PlayerState.PRONE, attacker.state)
    }

    @Test
    fun bothDown_withBlock() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.extraSkills.addNewSkill(attacker, SkillType.BLOCK)
        val defender = state.getPlayerById("H1".playerId)
        defender.extraSkills.addNewSkill(defender, SkillType.BLOCK)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 2.dblock),
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
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 2.dblock),
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
