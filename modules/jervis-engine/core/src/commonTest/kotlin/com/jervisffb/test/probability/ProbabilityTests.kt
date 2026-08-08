package com.jervisffb.test.probability

import com.jervisffb.engine.actions.BlockDice
import com.jervisffb.engine.actions.DBlockResult
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Test various aspects of calculating probabilities.
 */
class ProbabilityTests {

    private companion object {
        const val EPSILON = 1e-9
    }

    @Test
    fun pushProbability() {
        assertEquals(2/6.0, DBlockResult.faceProbability(BlockDice.PUSH_BACK).value)
    }

    @Test
    fun attackerChoosesResultInBlockPool() {
        // POW has one face on the block die, so the attacker needs at least
        // one POW in the pool.
        assertEquals(1 / 6.0, DBlockResult.successProbability(BlockDice.POW, 1).value, EPSILON)
        assertEquals(11 / 36.0, DBlockResult.successProbability(BlockDice.POW, 2).value, EPSILON)
        assertEquals(91 / 216.0, DBlockResult.successProbability(BlockDice.POW, 3).value, EPSILON)

        // Pushback has two faces on the block die.
        assertEquals(5 / 9.0, DBlockResult.successProbability(BlockDice.PUSH_BACK, 2).value, EPSILON)
    }

    @Test
    fun defenderChoosesResultInBlockPool() {
        // POW has one face on the block die, so every die must show POW.
        assertEquals(1 / 6.0, DBlockResult.successProbability(BlockDice.POW, 1, opponentChooses = true).value, EPSILON)
        assertEquals(1 / 36.0, DBlockResult.successProbability(BlockDice.POW, 2, opponentChooses = true).value, EPSILON)
        assertEquals(
            1 / 216.0,
            DBlockResult.successProbability(BlockDice.POW, 3, opponentChooses = true).value,
            EPSILON,
        )

        // Pushback has two faces, and all dice must show one of them.
        assertEquals(
            1 / 9.0,
            DBlockResult.successProbability(BlockDice.PUSH_BACK, 2, opponentChooses = true).value,
            EPSILON,
        )
    }
}
