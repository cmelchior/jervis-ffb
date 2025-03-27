package com.jervisffb.engine.rng

import com.jervisffb.engine.actions.D12Result
import com.jervisffb.engine.actions.D16Result
import com.jervisffb.engine.actions.D20Result
import com.jervisffb.engine.actions.D2Result
import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.actions.D4Result
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.DieResult
import kotlin.random.Random

/**
 * Dice Generator that just uses whatever default random implementation exists
 * on the platform.
 *
 * This is almost never random enough to model dice rolls in a game, so this class
 * should only be used for testing.
 */
class UnsafeRandomDiceGenerator(seed: Long = Random.nextLong()): DiceRollGenerator {
    val random = Random(seed)

    override fun rollDie(die: Dice): DieResult {
        return when (die) {
            Dice.D2 -> D2Result(generate(max = 2))
            Dice.D3 -> D3Result(generate(max = 3))
            Dice.D4 -> D4Result(generate(max = 4))
            Dice.D6 -> D6Result(generate(max = 6))
            Dice.D8 -> D8Result(generate(max = 8))
            Dice.D12 -> D12Result(generate(max = 12))
            Dice.D16 -> D16Result(generate(max = 16))
            Dice.D20 -> D20Result(generate(max = 20))
            Dice.BLOCK -> DBlockResult(generate(max = 6))
        }
    }

    private fun generate(max: Int): Int {
        return random.nextInt(max) + 1
    }
}
