package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.BlockTypeSelected
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectPlayerAction
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.Availability
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.rules.bb2025.skills.Stab
import com.jervisffb.engine.rules.common.actions.BlockType
import com.jervisffb.engine.rules.common.actions.PlayerSpecialActionType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.utils.putProne
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Class testing usage of the [Stab] skill
 */
class StabTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun isSpecialActionSimilarToBlock() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.addSkill(SkillType.STAB)
        controller.rollForward(
            PlayerSelected(attacker),
        )
        assertTrue(controller.getAvailableActions().get<SelectPlayerAction>().actions.any { it.type == PlayerSpecialActionType.STAB })
        assertTrue(controller.getAvailableActions().get<SelectPlayerAction>().actions.any { it.type == PlayerStandardActionType.BLOCK })
    }

    @Test
    fun canAbortIfNoDefenderSelected() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.addSkill(SkillType.STAB)
        controller.rollForward(
            *activatePlayer(attacker, PlayerSpecialActionType.STAB),
        )
        assertEquals(attacker, state.activePlayer)
        controller.rollForward(
            EndAction,
        )
        assertNull(state.activePlayer)
        assertEquals(Availability.AVAILABLE, attacker.available)
    }

    @Test
    fun multipleStabActionsPrTurn() {
        val attacker1 = state.getPlayerById("A1".playerId)
        attacker1.addSkill(SkillType.STAB)
        val defender1 = state.getPlayerById("H1".playerId)
        val attacker2 = state.getPlayerById("A2".playerId)
        attacker2.addSkill(SkillType.STAB)
        val defender2 = state.getPlayerById("H2".playerId)

        controller.rollForward(
            *activatePlayer(attacker1, PlayerSpecialActionType.STAB),
            PlayerSelected(defender1),
            DiceRollResults(6.d6, 6.d6), // Armour Roll -> Target 1
            DiceRollResults(1.d6, 1.d6), // Injury Roll -> Target 1
            *activatePlayer(attacker2, PlayerSpecialActionType.STAB),
            PlayerSelected(defender2),
            DiceRollResults(6.d6, 6.d6), // Armour Roll -> Target 2
            DiceRollResults(1.d6, 1.d6), // Injury Roll -> Target 2
        )
        assertNull(state.activePlayer)
        assertEquals(PlayerState.STUNNED, defender1.state)
        assertEquals(PlayerState.STUNNED, defender2.state)
        assertEquals(Availability.HAS_ACTIVATED, attacker1.available)
        assertEquals(Availability.HAS_ACTIVATED, attacker2.available)
    }

    @Test
    fun noModifiersAllowed() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.apply {
            addSkill(SkillType.STAB)
            addSkill(SkillType.MIGHTY_BLOW.id(1))
            addSkill(SkillType.DIRTY_PLAYER)
        }
        val defender = state.getPlayerById("H1".playerId)
        controller.rollForward(
            *activatePlayer(attacker, PlayerSpecialActionType.STAB),
            PlayerSelected(defender),
            DiceRollResults(2.d6, 6.d6)
        )
        assertNull(state.activePlayer)
        assertEquals(Availability.HAS_ACTIVATED, attacker.available)
        assertEquals(PlayerState.STANDING, defender.state)
    }

    @Test
    fun noEffectIfArmourNotBroken() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.addSkill(SkillType.STAB)
        val defender = state.getPlayerById("H1".playerId)
        controller.rollForward(
            *activatePlayer(attacker, PlayerSpecialActionType.STAB),
            PlayerSelected(defender),
            DiceRollResults(1.d6, 1.d6), // Armour Roll -> Target 1
        )
        assertNull(state.activePlayer)
        assertEquals(PlayerState.STANDING, defender.state)
        assertEquals(Availability.HAS_ACTIVATED, attacker.available)
        assertEquals(awayTeam, state.activeTeam)
    }

    @Test
    fun useDuringBlitz() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.addSkill(SkillType.STAB)
        val defender = state.getPlayerById("H1".playerId)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLITZ),
            PlayerSelected(defender),
            PlayerSelected(defender), // Start Blitz "block"
            BlockTypeSelected(BlockType.STAB),
            DiceRollResults(3.d6, 6.d6), // Armour Roll
            DiceRollResults(1.d6, 1.d6), // Injury Roll
        )
        // Using Stab ends Blitz immediately, i.e. no move after is possible
        assertNull(state.activePlayer)
        assertEquals(PlayerState.STUNNED, defender.state)
        assertEquals(Availability.HAS_ACTIVATED, attacker.available)
        assertEquals(awayTeam, state.activeTeam)
    }

    @Test
    fun isNotAvailableWhenProne() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.apply {
            addSkill(SkillType.STAB)
            putProne()
        }
        controller.rollForward(PlayerSelected(attacker))
        assertFalse(
            controller.getAvailableActions().get<SelectPlayerAction>().actions.any { it.type == PlayerSpecialActionType.STAB }
        )
    }

    @Test
    fun isNotAvailableIfNotAdjacentToOpponent() {
        val attacker = state.getPlayerById("A7".playerId)
        attacker.addSkill(SkillType.STAB)
        controller.rollForward(PlayerSelected(attacker))
        assertFalse(controller.getAvailableActions().get<SelectPlayerAction>().actions.any { it.type == PlayerSpecialActionType.STAB })
    }
}
