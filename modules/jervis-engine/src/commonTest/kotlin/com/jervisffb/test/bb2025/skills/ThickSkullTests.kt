package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.rules.bb2025.skills.ThickSkull
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.standardBlock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Class testing usage of the [ThickSkull] skill.
 */
class ThickSkullTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun workOnStuntyRolling7() {
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        defender.addSkill(SkillType.STUNTY)
        defender.addSkill(SkillType.THICK_SKULL)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 6.dblock),
            DirectionSelected(Direction.LEFT),
            Cancel, // Do not follow up
            DiceRollResults(6.d6, 6.d6), // Armour roll
            DiceRollResults(1.d6, 6.d6), // Injury Roll
            Confirm, // Use Thick Skull
        )
        assertEquals(PlayerState.STUNNED, defender.state)
    }

    @Test
    fun workOnStuntyRolling7UsingModifiers() {
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        attacker.addSkill(SkillType.DIRTY_PLAYER)
        defender.apply { defender
            addSkill(SkillType.STUNTY)
            addSkill(SkillType.THICK_SKULL)
            state = PlayerState.PRONE
            hasTackleZones = false
        }
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.FOUL),
            PlayerSelected("H1".playerId),
            DiceRollResults(6.d6, 5.d6), // Armour roll. Break without Dirty Player
            DiceRollResults(1.d6, 5.d6), // Injury Roll
            Confirm, // Use Dirty Player
            Confirm, // Use Thick Skull
        )
        assertEquals(PlayerState.STUNNED, defender.state)
    }

    @Test
    fun doesNotWorkOnStuntyRolling8() {
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        defender.addSkill(SkillType.STUNTY)
        defender.addSkill(SkillType.THICK_SKULL)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 6.dblock),
            DirectionSelected(Direction.LEFT),
            Cancel, // Do not follow up
            DiceRollResults(6.d6, 6.d6), // Armour roll
            DiceRollResults(2.d6, 6.d6), // Injury Roll
            Cancel // Do not use Apothecary
        )
        assertNull(state.activePlayer)
        assertEquals(PlayerState.KNOCKED_OUT, defender.state)
    }

    @Test
    fun workOnPlayerRolling8() {
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        defender.addSkill(SkillType.THICK_SKULL)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 6.dblock),
            DirectionSelected(Direction.LEFT),
            Cancel, // Do not follow up
            DiceRollResults(6.d6, 6.d6), // Armour roll
            DiceRollResults(2.d6, 6.d6), // Injury Roll
            Confirm, // Use Thick Skull
        )
        assertEquals(PlayerState.STUNNED, defender.state)
    }

    @Test
    fun workOnPlayerRolling8UsingModifiers() {
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        attacker.addSkill(SkillType.DIRTY_PLAYER)
        defender.apply { defender
            addSkill(SkillType.THICK_SKULL)
            state = PlayerState.PRONE
            hasTackleZones = false
        }
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.FOUL),
            PlayerSelected("H1".playerId),
            DiceRollResults(6.d6, 5.d6), // Armour roll. Break without Dirty Player
            DiceRollResults(2.d6, 5.d6), // Injury Roll
            Confirm, // Use Dirty Player
            Confirm, // Use Thick Skull
        )
        assertEquals(PlayerState.STUNNED, defender.state)
    }

    @Test
    fun doesNotWorkOnPlayerRolling9() {
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        defender.addSkill(SkillType.THICK_SKULL)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 6.dblock),
            DirectionSelected(Direction.LEFT),
            Cancel, // Do not follow up
            DiceRollResults(6.d6, 6.d6), // Armour roll
            DiceRollResults(3.d6, 6.d6), // Injury Roll
            Cancel // Do not use Apothecary
        )
        assertNull(state.activePlayer)
        assertEquals(PlayerState.KNOCKED_OUT, defender.state)
    }
}
