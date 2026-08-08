package com.jervisffb.engine.statistics.probability

import com.jervisffb.engine.actions.BlockDice
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.model.RerollSourceId
import com.jervisffb.engine.rules.DiceRollType
import kotlinx.serialization.Serializable

/**
 * A single roll in a solution, reduced to what the difficulty rating needs:
 * how likely it was to succeed, and which re-rolls could have rescued it.
 *
 * Only rolls that the solution actually depends on belong here. A roll counts,
 * if failing it, would end the team turn or put the goal out of reach, which
 * leaves out Armour, Injury and Casualty rolls.
 */
@Serializable
data class RollChance(
    val rollType: DiceRollType,
    val successProbability: Probability,
    val eligibleRerolls: Set<RerollSourceId> = emptySet(),
) {
    init {
        require(successProbability > Probability.NEVER) {
            "Success probability must be in (0, 1]: $successProbability"
        }
    }

    // How demanding this roll is on its own
    val risk: Surprisal
        get() = successProbability.toSurprisal()

    companion object {
        /**
         * A D6 test that the coach chose to pass on [target] or above.
         */
        fun d6(
            rollType: DiceRollType,
            target: D6Result,
            eligibleRerolls: Set<RerollSourceId> = emptySet(),
        ): RollChance = RollChance(rollType, D6Result.successProbability(target), eligibleRerolls)

        /**
         * A block where the coach used [face] out of a pool of [diceCount] dice.
         */
        fun block(
            face: BlockDice,
            diceCount: Int,
            opponentChooses: Boolean = false,
            eligibleRerolls: Set<RerollSourceId> = emptySet(),
        ): RollChance = RollChance(
            DiceRollType.BLOCK,
            DBlockResult.successProbability(face, diceCount, opponentChooses),
            eligibleRerolls,
        )
    }
}
