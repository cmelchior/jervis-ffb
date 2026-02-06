package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.BlockTypeSelected
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.bb2025.skills.Taunt
import com.jervisffb.engine.rules.common.actions.BlockType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.standardBlock
import com.jervisffb.test.utils.SelectSingleBlockDieResult
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Class testing usage of the [Taunt] skill.
 */
class TauntTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun workOnStumbleWithDodge() {
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        defender.addSkill(SkillType.TAUNT)
        defender.addSkill(SkillType.DODGE)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 5.dblock),
            Confirm, // Use Dodge
            DirectionSelected(Direction.UP_LEFT),
            Confirm, // Use Taunt
        )
        assertNull(state.activePlayer)
        assertEquals(awayTeam, state.activeTeam)
        assertEquals(FieldCoordinate(12, 5), attacker.location)
    }

    @Test
    fun workPushedBack() {
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        defender.addSkill(SkillType.TAUNT)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 3.dblock), // Pushed Back
            DirectionSelected(Direction.UP_LEFT),
            Confirm, // Use Taunt
        )
        assertNull(state.activePlayer)
        assertEquals(awayTeam, state.activeTeam)
        assertEquals(FieldCoordinate(12, 5), attacker.location)
    }

    @Test
    fun workOnPow() {
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        defender.addSkill(SkillType.TAUNT)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 6.dblock), // Pow
            DirectionSelected(Direction.UP_LEFT),
            Confirm, // Use Taunt
            DiceRollResults(1.d6, 1.d6),
        )
        assertNull(state.activePlayer)
        assertEquals(awayTeam, state.activeTeam)
        assertEquals(FieldCoordinate(12, 5), attacker.location)
    }

    @Test
    fun tauntNotAvailableIfFendIsSelected() {
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        defender.addSkill(SkillType.TAUNT)
        defender.addSkill(SkillType.FEND)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 3.dblock), // Pushed Back
            DirectionSelected(Direction.UP_LEFT),
            Confirm, // Use Fend
        )
        assertNull(state.activePlayer)
        assertEquals(awayTeam, state.activeTeam)
        assertEquals(FieldCoordinate(13, 5), attacker.location)
    }

    @Test
    fun tauntIsNotUsedOnFrenzy() {
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        defender.addSkill(SkillType.TAUNT)
        attacker.addSkill(SkillType.FRENZY)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 3.dblock), // Pushed Back
            DirectionSelected(Direction.UP_LEFT),
            BlockTypeSelected(BlockType.STANDARD),
            3.dblock,
            NoRerollSelected(),
            SelectSingleBlockDieResult(),
            DirectionSelected(Direction.UP),
        )
        assertNull(state.activePlayer)
        assertEquals(FieldCoordinate(11, 3), defender.location)
        assertEquals(awayTeam, state.activeTeam)
        assertEquals(FieldCoordinate(11, 4), attacker.location)
    }

    @Ignore
    @Test
    fun doesNotWorkAgainstRooted() {
        // Wait for Take Root to be implemented
    }
}
