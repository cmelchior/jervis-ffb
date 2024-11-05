package com.jervisffb.test.skills

import com.jervisffb.engine.actions.BlockTypeSelected
import com.jervisffb.engine.actions.CalculatedAction
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DicePoolChoice
import com.jervisffb.engine.actions.DicePoolResultsSelected
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectDicePoolResult
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.BlockType
import com.jervisffb.engine.rules.PlayerStandardActionType
import com.jervisffb.engine.rules.bb2020.skills.Block
import com.jervisffb.test.JervisGameTest
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
            CalculatedAction { _, _ -> selectSingleDieResult() },
        )
        assertEquals(FieldCoordinate(13, 5), attacker.location)
        assertEquals(PlayerState.KNOCKED_DOWN, attacker.state)
        assertEquals(FieldCoordinate(12, 5), defender.location)
        assertEquals(PlayerState.KNOCKED_DOWN, defender.state)
    }

    @Test
    fun bothDown_withBlock() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.extraSkills.add(Block())
        val defender = state.getPlayerById("H1".playerId)
        defender.extraSkills.add(Block())
        controller.rollForward(
            PlayerSelected(attacker.id),
            PlayerActionSelected(PlayerStandardActionType.BLOCK),
            PlayerSelected(defender.id),
            BlockTypeSelected(BlockType.STANDARD),
            2.dblock,
            NoRerollSelected(),
            CalculatedAction { _, _ -> selectSingleDieResult() },
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
        attacker.extraSkills.add(Block())
        val defender = state.getPlayerById("H1".playerId)
        defender.extraSkills.add(Block())
        controller.rollForward(
            PlayerSelected(attacker.id),
            PlayerActionSelected(PlayerStandardActionType.BLOCK),
            PlayerSelected(defender.id),
            BlockTypeSelected(BlockType.STANDARD),
            2.dblock,
            NoRerollSelected(),
            CalculatedAction { _, _ -> selectSingleDieResult() },
            Confirm, // Defender uses block
            Cancel, // Attacker doesn't use block
        )
        assertTrue(state.isTurnOver())
        assertEquals(FieldCoordinate(13, 5), attacker.location)
        assertEquals(PlayerState.KNOCKED_DOWN, attacker.state)
        assertEquals(FieldCoordinate(12, 5), defender.location)
        assertEquals(PlayerState.STANDING, defender.state)
    }

    private fun selectSingleDieResult(): DicePoolResultsSelected {
        // TODO Need to rework these API's so this is easier
        return controller.getAvailableActions()
            .filterIsInstance<SelectDicePoolResult>()
            .first().pools.first()
            .let { DicePoolResultsSelected(listOf(DicePoolChoice(0, it.dice.map { it.result }))) }
    }
}

