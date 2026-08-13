package com.jervisffb.engine.bb2025.challenge.modifier

import com.jervisffb.engine.GameDelta
import com.jervisffb.engine.bb2025.procedures.actions.block.singleblock.SingleStandardBlockRollDice
import com.jervisffb.engine.challenge.ChallengeContext
import com.jervisffb.engine.challenge.ChallengeContextHolder
import com.jervisffb.engine.challenge.GoalModifier
import com.jervisffb.engine.challenge.GoalStatus
import com.jervisffb.engine.challenge.ModifierProgress
import com.jervisffb.engine.common.context.BlockContext
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.MAX_BLOCK_DICE

/**
 * Goal modifier setting a lower bound on how many block dice the block has to
 * be made with. Negative means the block is against the attacker.
 *
 * Examples:
 * - `count = 2` means that blocking with 2 or more dice is a completion.
 * - `count = -2` means that blocking with 2 or more dice is a completion.
 */
data class BlockDiceRequired(val count: Int) : GoalModifier {
    init {
        val againstRange = -2 .. -MAX_BLOCK_DICE
        val forRange = 1 .. MAX_BLOCK_DICE
        require(count in againstRange || count in forRange) {
            "Invalid number of block dice required: $count"
        }
    }

    override val description: String = when {
        count < 0 -> "With ${-count} or more block dice against"
        else -> "With $count or more block dice"
    }

    override fun initialize(state: Game): ChallengeContext? = null

    override fun evaluate(
        state: Game,
        delta: GameDelta,
        contexts: ChallengeContextHolder
    ): ModifierProgress {
        val requiredDiceUsed = delta.steps.any { step ->
            when (step.node == SingleStandardBlockRollDice.RollDice) {
                true -> {
                    val dice = state.getContext<BlockContext>().calculateNoOfBlockDice()
                    dice >= count
                }
                false -> false
            }
        }

        val status = when (requiredDiceUsed) {
            true -> GoalStatus.COMPLETED
            false -> GoalStatus.IN_PROGRESS
        }

        return ModifierProgress(status, null)
    }
}
