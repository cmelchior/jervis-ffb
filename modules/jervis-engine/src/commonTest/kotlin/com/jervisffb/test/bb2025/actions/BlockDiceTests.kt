package com.jervisffb.test.bb2025.actions

import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.rules.common.procedures.actions.block.BlockContext
import com.jervisffb.test.JervisGameBB2025Test
import kotlin.test.Test
import kotlin.test.assertEquals

class BlockDiceTests: JervisGameBB2025Test() {

    @Test
    fun test1Die() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.strength = 3
        val defender = state.getPlayerById("H1".playerId)
        defender.strength = 3
        val context = BlockContext(attacker, defender)
        assertEquals(1, context.calculateNoOfBlockDice())
    }

    @Test
    fun test2DiceAttacker() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.strength = 3
        val defender = state.getPlayerById("H1".playerId)
        defender.strength = 2
        val context = BlockContext(attacker, defender)
        assertEquals(2, context.calculateNoOfBlockDice())
    }

    @Test
    fun test2DiceDefender() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.strength = 2
        val defender = state.getPlayerById("H1".playerId)
        defender.strength = 3
        val context = BlockContext(attacker, defender)
        assertEquals(-2, context.calculateNoOfBlockDice())
    }

    @Test
    fun test3DiceAttacker() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.strength = 3
        val defender = state.getPlayerById("H1".playerId)
        defender.strength = 1
        val context = BlockContext(attacker, defender)
        assertEquals(3, context.calculateNoOfBlockDice())
    }

    @Test
    fun test3DiceDefender() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.strength = 1
        val defender = state.getPlayerById("H1".playerId)
        defender.strength = 3
        val context = BlockContext(attacker, defender)
        assertEquals(-3, context.calculateNoOfBlockDice())
    }
}
