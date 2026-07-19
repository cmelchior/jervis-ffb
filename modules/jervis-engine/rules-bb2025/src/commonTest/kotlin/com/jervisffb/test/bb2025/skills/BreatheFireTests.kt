package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.BlockTypeSelected
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectPlayerAction
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.Availability
import com.jervisffb.engine.rules.bb2025.skills.BreatheFire
import com.jervisffb.engine.rules.common.actions.BlockType
import com.jervisffb.engine.rules.common.actions.PlayerSpecialActionType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.breatheFireRoll
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.utils.assertProne
import com.jervisffb.test.utils.assertStanding
import com.jervisffb.test.utils.assertStunned
import com.jervisffb.test.utils.putProne
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Class testing usage of the [BreatheFire] skill
 */
class BreatheFireTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun isSpecialActionSimilarToBlock() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.addSkill(SkillType.BREATHE_FIRE)
        controller.rollForward(
            PlayerSelected(attacker),
        )
        assertTrue(controller.getAvailableActions().get<SelectPlayerAction>().actions.any { it.type == PlayerSpecialActionType.BREATHE_FIRE })
        assertTrue(controller.getAvailableActions().get<SelectPlayerAction>().actions.any { it.type == PlayerStandardActionType.BLOCK })
    }

    @Test
    fun canAbortIfNoDefenderSelected() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.addSkill(SkillType.BREATHE_FIRE)
        controller.rollForward(
            *activatePlayer(attacker, PlayerSpecialActionType.BREATHE_FIRE),
        )
        assertEquals(attacker, state.activePlayer)
        controller.rollForward(
            EndAction,
        )
        assertNull(state.activePlayer)
        assertEquals(Availability.AVAILABLE, attacker.available)
    }

    @Test
    fun multipleBreatheActionsPrTurn() {
        val attacker1 = state.getPlayerById("A1".playerId)
        attacker1.addSkill(SkillType.BREATHE_FIRE)
        val defender1 = state.getPlayerById("H1".playerId)
        val attacker2 = state.getPlayerById("A2".playerId)
        attacker2.addSkill(SkillType.BREATHE_FIRE)
        val defender2 = state.getPlayerById("H2".playerId)

        controller.rollForward(
            *activatePlayer(attacker1, PlayerSpecialActionType.BREATHE_FIRE),
            PlayerSelected(defender1),
            *breatheFireRoll(2.d6),
            *activatePlayer(attacker2, PlayerSpecialActionType.BREATHE_FIRE),
            PlayerSelected(defender2),
            *breatheFireRoll(4.d6),
        )
        assertNull(state.activePlayer)
        defender1.assertStanding()
        defender2.assertProne()
        assertEquals(Availability.HAS_ACTIVATED, attacker1.available)
        assertEquals(Availability.HAS_ACTIVATED, attacker2.available)
    }

    @Test
    fun breathePlaceTargetProne() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.addSkill(SkillType.BREATHE_FIRE)
        val defender = state.getPlayerById("H1".playerId)
        controller.rollForward(
            *activatePlayer(attacker, PlayerSpecialActionType.BREATHE_FIRE),
            PlayerSelected(defender),
            *breatheFireRoll(4.d6),
        )
        assertNull(state.activePlayer)
        assertEquals(Availability.HAS_ACTIVATED, attacker.available)
        defender.assertProne()
    }

    @Test
    fun breatheKnockPlayerDown() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.addSkill(SkillType.BREATHE_FIRE)
        val defender = state.getPlayerById("H1".playerId)
        controller.rollForward(
            *activatePlayer(attacker, PlayerSpecialActionType.BREATHE_FIRE),
            PlayerSelected(defender),
            *breatheFireRoll(6.d6),
            DiceRollResults(3.d6, 6.d6),
            DiceRollResults(1.d6, 6.d6)
        )
        assertNull(state.activePlayer)
        assertEquals(Availability.HAS_ACTIVATED, attacker.available)
        defender.assertStunned()
    }

    @Test
    fun breatheOnThemselves() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.addSkill(SkillType.BREATHE_FIRE)
        val defender = state.getPlayerById("H1".playerId)
        controller.rollForward(
            *activatePlayer(attacker, PlayerSpecialActionType.BREATHE_FIRE),
            PlayerSelected(defender),
            *breatheFireRoll(1.d6),
            DiceRollResults(3.d6, 6.d6),
            DiceRollResults(1.d6, 6.d6)
        )
        // Turnover since player on active team is Knocked Down
        assertNull(state.activePlayer)
        assertEquals(homeTeam, state.activeTeam)
        attacker.assertStunned()
        defender.assertStanding()
    }

    @Test
    fun modifierOnStrongTarget() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.addSkill(SkillType.BREATHE_FIRE)
        val defender = state.getPlayerById("H1".playerId)
        defender.apply {
            baseStrength = 5
            strength = 5
        }
        controller.rollForward(
            *activatePlayer(attacker, PlayerSpecialActionType.BREATHE_FIRE),
            PlayerSelected(defender),
            *breatheFireRoll(4.d6),
        )
        assertNull(state.activePlayer)
        defender.assertStanding()
        attacker.assertStanding()
    }

    @Test
    fun breatheNoEffect() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.addSkill(SkillType.BREATHE_FIRE)
        val defender = state.getPlayerById("H1".playerId)
        controller.rollForward(
            *activatePlayer(attacker, PlayerSpecialActionType.BREATHE_FIRE),
            PlayerSelected(defender),
            *breatheFireRoll(3.d6),
        )
        assertNull(state.activePlayer)
        assertEquals(awayTeam, state.activeTeam)
        defender.assertStanding()
        attacker.assertStanding()
    }


    @Test
    fun noArmourModifiersAllowed() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.apply {
            addSkill(SkillType.BREATHE_FIRE)
            addSkill(SkillType.MIGHTY_BLOW.idAdjustment(1))
            addSkill(SkillType.DIRTY_PLAYER)
        }
        val defender = state.getPlayerById("H1".playerId)
        controller.rollForward(
            *activatePlayer(attacker, PlayerSpecialActionType.BREATHE_FIRE),
            PlayerSelected(defender),
            *breatheFireRoll(6.d6),
            DiceRollResults(2.d6, 6.d6)
        )
        assertNull(state.activePlayer)
        assertEquals(Availability.HAS_ACTIVATED, attacker.available)
        defender.assertProne()
    }

    @Test
    fun useDuringBlitz() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.addSkill(SkillType.BREATHE_FIRE)
        val defender = state.getPlayerById("H1".playerId)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLITZ),
            PlayerSelected(defender),
            PlayerSelected(defender), // Start Blitz "block"
            BlockTypeSelected(BlockType.BREATHE_FIRE),
            *breatheFireRoll(6.d6),
            DiceRollResults(3.d6, 6.d6), // Armour Roll
            DiceRollResults(1.d6, 1.d6), // Injury Roll
        )
        // Using Breathe Fire ends Blitz immediately, i.e. no move after is possible
        assertNull(state.activePlayer)
        defender.assertStunned()
        assertEquals(Availability.HAS_ACTIVATED, attacker.available)
        assertEquals(awayTeam, state.activeTeam)
    }

    @Test
    fun isNotAvailableWhenProne() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.apply {
            addSkill(SkillType.BREATHE_FIRE)
            putProne()
        }
        controller.rollForward(PlayerSelected(attacker))
        assertFalse(
            controller.getAvailableActions().get<SelectPlayerAction>().actions.any { it.type == PlayerSpecialActionType.BREATHE_FIRE }
        )
    }

    @Test
    fun isNotAvailableIfNotAdjacentToOpponent() {
        val attacker = state.getPlayerById("A7".playerId)
        attacker.addSkill(SkillType.BREATHE_FIRE)
        controller.rollForward(PlayerSelected(attacker))
        assertFalse(controller.getAvailableActions().get<SelectPlayerAction>().actions.any { it.type == PlayerSpecialActionType.BREATHE_FIRE })
    }
}
