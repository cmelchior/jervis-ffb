package com.jervisffb.test.probability

import com.jervisffb.engine.actions.BlockDice
import com.jervisffb.engine.actions.D6Result
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

    @Test
    fun d6CombinationsEqualOrAbove() {
        assertEquals(3, D6Result.combinationsEqualToTotalOrAbove(dice = 1, total = 4))
        assertEquals(1, D6Result.combinationsEqualToTotalOrAbove(dice = 2, total = 12))
        assertEquals(21, D6Result.combinationsEqualToTotalOrAbove(dice = 2, total = 7))
        assertEquals(36, D6Result.combinationsEqualToTotalOrAbove(dice = 2, total = 2))
        assertEquals(135, D6Result.combinationsEqualToTotalOrAbove(dice = 3, total = 10))
    }

    @Test
    fun d6CombinationsEqual() {
        assertEquals(1, D6Result.combinationsEqualToTotal(dice = 2, total = 12))
        assertEquals(6, D6Result.combinationsEqualToTotal(dice = 2, total = 7))
        assertEquals(1, D6Result.combinationsEqualToTotal(dice = 2, total = 2))
        assertEquals(27, D6Result.combinationsEqualToTotal(dice = 3, total = 10))
    }
}
