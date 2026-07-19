package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.rules.bb2025.skills.Claws
import com.jervisffb.engine.rules.common.actions.PlayerSpecialActionType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.blitzBlock
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.followUp
import com.jervisffb.test.standardBlock
import com.jervisffb.test.useApothecary
import com.jervisffb.test.utils.assertKnockedOut
import com.jervisffb.test.utils.assertNoActivePlayer
import com.jervisffb.test.utils.assertProne
import com.jervisffb.test.utils.assertStanding
import com.jervisffb.test.utils.assertStunned
import com.jervisffb.test.utils.makeDistracted
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Class testing usage of the [Claws] skill.
 *
 * See page 127 in the BB2025 rulebook.
 */
class ClawsTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun workDuringBlitzAction() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.addSkill(SkillType.CLAWS)
        val defender = state.getPlayerById("H1".playerId)
        assertEquals(9, defender.armorValue)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLITZ),
            PlayerSelected(defender.id), // Select target of blitz
            *blitzBlock(defender, 6.dblock),
            DirectionSelected(Direction.DOWN_LEFT),
            followUp(false),
            DiceRollResults(6.d6, 2.d6),
            Confirm, // Use Claws
            DiceRollResults(1.d6, 1.d6),
            EndAction
        )
        state.assertNoActivePlayer()
        defender.assertStunned()
    }

    @Test
    fun workDuringBlockAction() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.addSkill(SkillType.CLAWS)
        val defender = state.getPlayerById("H1".playerId)
        assertEquals(9, defender.armorValue)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 6.dblock),
            DirectionSelected(Direction.DOWN_LEFT),
            followUp(false),
            DiceRollResults(6.d6, 2.d6),
            Confirm, // Use Claws
            DiceRollResults(1.d6, 1.d6),
        )
        state.assertNoActivePlayer()
        defender.assertStunned()
    }

    @Test
    fun workOnBothDown() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.addSkill(SkillType.CLAWS)
        val defender = state.getPlayerById("H1".playerId)
        defender.addSkill(SkillType.CLAWS)
        assertEquals(9, defender.armorValue)
        assertEquals(9, attacker.armorValue)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 2.dblock),
            DiceRollResults(6.d6, 2.d6),
            Confirm, // Use Claws on Defender
            DiceRollResults(6.d6, 2.d6),
            useApothecary(false),
            DiceRollResults(6.d6, 2.d6),
            Confirm, // Use Claws on Attacker
            DiceRollResults(1.d6, 1.d6),
        )
        state.assertNoActivePlayer()
        defender.assertKnockedOut()
        attacker.assertStunned()
    }

    @Test
    fun workOnPow() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.addSkill(SkillType.CLAWS)
        val defender = state.getPlayerById("H1".playerId)
        assertEquals(9, defender.armorValue)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 6.dblock),
            DirectionSelected(Direction.LEFT),
            followUp(false),
            DiceRollResults(6.d6, 2.d6),
            Confirm, // Use Claws on Defender
            DiceRollResults(6.d6, 2.d6),
            useApothecary(false),
        )
        state.assertNoActivePlayer()
        defender.assertKnockedOut()
    }

    @Test
    fun doesNotWorkOnStab() {
        val attacker = state.getPlayerById("A1".playerId).apply {
            addSkill(SkillType.CLAWS)
            addSkill(SkillType.STAB)
        }
        val defender = state.getPlayerById("H1".playerId)
        assertEquals(9, defender.armorValue)
        controller.rollForward(
            *activatePlayer(attacker, PlayerSpecialActionType.STAB),
            PlayerSelected(defender),
            DiceRollResults(6.d6, 2.d6),
        )
        state.assertNoActivePlayer()
        defender.assertStanding()
    }

    @Test
    fun doesNotCombineWithMightyBlow() {
        val attacker = state.getPlayerById("A1".playerId).apply {
            addSkill(SkillType.CLAWS)
            addSkill(SkillType.MIGHTY_BLOW)
        }
        val defender = state.getPlayerById("H1".playerId)
        assertEquals(9, defender.armorValue)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 6.dblock),
            DirectionSelected(Direction.LEFT),
            followUp(false),
            DiceRollResults(6.d6, 1.d6), // This would break armour if combined with Mighty Blow
        )
        state.assertNoActivePlayer()
        defender.assertProne()
    }

    @Test
    fun workIfBlockedWhileDistracted() {
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId).apply {
            addSkill(SkillType.CLAWS)
            makeDistracted()
        }
        assertEquals(9, attacker.armorValue)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 1.dblock),
            DiceRollResults(6.d6, 2.d6),
            Confirm, // Use Claws on Attacker
            DiceRollResults(5.d6, 2.d6),
        )
        state.assertNoActivePlayer()
        attacker.assertStunned()
    }

    @Test
    fun doNotAskForClawsOnArmour8OrLess() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.addSkill(SkillType.CLAWS)
        val defender = state.getPlayerById("H1".playerId).apply {
            baseArmorValue = 8
            armorValue = 8
        }
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 6.dblock),
            DirectionSelected(Direction.LEFT),
            followUp(false),
            DiceRollResults(6.d6, 1.d6),
        )
        state.assertNoActivePlayer()
        defender.assertProne()
    }

    @Test
    fun doNotAskIfRollingLessThan8() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.addSkill(SkillType.CLAWS)
        val defender = state.getPlayerById("H1".playerId)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 6.dblock),
            DirectionSelected(Direction.LEFT),
            followUp(false),
            DiceRollResults(6.d6, 1.d6),
        )
        state.assertNoActivePlayer()
        defender.assertProne()
    }
}
