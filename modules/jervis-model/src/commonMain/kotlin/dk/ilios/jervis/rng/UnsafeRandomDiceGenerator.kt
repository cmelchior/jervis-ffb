package dk.ilios.jervis.rng

import dk.ilios.jervis.actions.D12Result
import dk.ilios.jervis.actions.D16Result
import dk.ilios.jervis.actions.D20Result
import dk.ilios.jervis.actions.D2Result
import dk.ilios.jervis.actions.D3Result
import dk.ilios.jervis.actions.D4Result
import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.D8Result
import dk.ilios.jervis.actions.DBlockResult
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.DieResult
import kotlin.random.Random

/**
 * Dice Generator that just uses whatever default random implementation exists
 * on the platform.
 *
 * This is almost never random enough to model dice rolls in a game, so this class
 * should only be used for testing.
 */
class UnsafeRandomDiceGenerator: DiceRollGenerator {
    val random = Random.Default

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
