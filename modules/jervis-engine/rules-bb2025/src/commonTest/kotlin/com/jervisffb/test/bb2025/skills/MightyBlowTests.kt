package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.PlayerDogoutState
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.modifiers.MightyBlowArmourModifier
import com.jervisffb.engine.model.modifiers.MightyBlowInjuryModifier
import com.jervisffb.engine.rules.bb2025.skills.MightyBlow
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryContext
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.standardBlock
import com.jervisffb.test.utils.assertNoActivePlayer
import com.jervisffb.test.utils.assertProne
import com.jervisffb.test.utils.assertStunned
import com.jervisffb.test.utils.makeDistracted
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Class testing usage of the [MightyBlow] skill.
 */
class MightyBlowTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun worksOnBothDown_attacker() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.addSkill(SkillType.MIGHTY_BLOW)
        val defender = state.getPlayerById("H1".playerId)
        assertEquals(9, defender.armorValue)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 2.dblock),
            DiceRollResults(2.d6, 6.d6), // Armour
            Confirm // Use Mighty Blow
        )
        assertNotNull(state.getContext<RiskingInjuryContext>().armourModifiers.single { it is MightyBlowArmourModifier })
        controller.rollForward(
            DiceRollResults(1.d6, 1.d6), // Defender Injury
            DiceRollResults(1.d6, 1.d6), // Attacker Armour
        )
        defender.assertStunned()
        attacker.assertProne()
        state.assertNoActivePlayer()
    }

    @Test
    fun worksOnBothDown_defender() {
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId).apply {
            addSkill(SkillType.MIGHTY_BLOW)
        }
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 2.dblock),
            DiceRollResults(2.d6, 6.d6), // Armour on Defender
            DiceRollResults(2.d6, 6.d6), // Armour on Attacker
            Confirm, // Use Mighty Blow
        )
        assertNotNull(state.getContext<RiskingInjuryContext>().armourModifiers.single { it is MightyBlowArmourModifier })
        controller.rollForward(
            DiceRollResults(1.d6, 1.d6), // Defender Injury
        )
        state.assertNoActivePlayer()
        attacker.assertStunned()
    }

    @Test
    fun doesNotUseIfItDoesNotBreakArmour() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.addSkill(SkillType.MIGHTY_BLOW)
        val defender = state.getPlayerById("H1".playerId)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 6.dblock),
            DirectionSelected(Direction.LEFT),
            Confirm, // Follow up
            DiceRollResults(2.d6, 5.d6), // Does not break armour
        )
        state.assertNoActivePlayer()
        defender.assertProne()
    }

    @Test
    fun doesNotWorkOnInjuryIfUsedOnArmour() {
        val attacker = state.getPlayerById("A1".playerId).apply {
            addSkill(SkillType.BLOCK)
            addSkill(SkillType.MIGHTY_BLOW)
        }
        val defender = state.getPlayerById("H1".playerId)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 2.dblock),
            Confirm, // Attacker uses block
            DiceRollResults(3.d6, 6.d6), // Armour
        )
        assertFalse(state.getContext<RiskingInjuryContext>().armourModifiers.any { it is MightyBlowArmourModifier })
        controller.rollForward(
            DiceRollResults(6.d6, 1.d6), // Defender Injury
            Confirm, // Use Mighty Blow
            Cancel, // Do not use apothecary
        )
        state.assertNoActivePlayer()
        assertEquals(PlayerDogoutState.KNOCKED_OUT, defender.state)
    }


    @Test
    fun useOnInjury() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.apply {
            addSkill(SkillType.MIGHTY_BLOW)
        }
        val defender = state.getPlayerById("H1".playerId)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 6.dblock),
            DirectionSelected(Direction.LEFT),
            Confirm, // Follow up
            DiceRollResults(4.d6, 5.d6), // Break armour without using Mighty Blow
            DiceRollResults(1.d6, 6.d6), // Injury
            Confirm, // Use Mighty Blow
        )
        assertNotNull(state.getContext<RiskingInjuryContext>().injuryModifiers.single { it is MightyBlowInjuryModifier })
        controller.rollForward(
            Cancel, // Do not use apothecary
        )
        state.assertNoActivePlayer()
        assertEquals(PlayerDogoutState.KNOCKED_OUT, defender.state)
    }

    @Test
    fun doesNotWorkWhenDistracted() {
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        defender.apply {
            addSkill(SkillType.MIGHTY_BLOW)
            makeDistracted()
        }
        assertTrue(rules.isDistracted(defender))
        assertEquals(9, attacker.armorValue)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 2.dblock),
            DiceRollResults(2.d6, 6.d6), // Defender armour roll
            DiceRollResults(2.d6, 6.d6), // Attacker armour roll
        )
        assertNull(state.activePlayer)
        defender.assertProne()
    }
}
