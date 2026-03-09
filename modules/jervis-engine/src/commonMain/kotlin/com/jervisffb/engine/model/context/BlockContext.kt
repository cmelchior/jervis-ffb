package com.jervisffb.engine.model.context

import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.rules.common.procedures.BlockDieRoll
import com.jervisffb.engine.rules.common.procedures.actions.block.calculateBlockDiceToRoll

/**
 * Wrap temporary data needed to track a "standard block". This can either
 * be part of a Blitz, a normal Block action, or using the Multiple Block
 * skill.
 */
data class BlockContext(
    val attacker: Player,
    val defender: Player,
    val isBlitzing: Boolean = false,
    val isUsingJuggernaught: Boolean = false,
    val isUsingMultiBlock: Boolean = false,
    val offensiveAssists: Int = 0,
    val defensiveAssists: Int = 0,
    val roll: List<BlockDieRoll> = emptyList(),
    val hasAcceptedResult: Boolean = false, // Do not want to reroll any further
    var resultIndex: Int = -1, // Index into `roll` that defines the selected roll
    val didFollowUp: Boolean = false,
    // Block was aborted before rolling any dice
    val aborted: Boolean = false,
): ProcedureContext {
    val result: DBlockResult
        get() = roll[resultIndex].result

    // Helper method to share logic between roll and reroll
    fun calculateNoOfBlockDice(): Int {
        return calculateBlockDiceToRoll(
            attacker.strength,
            offensiveAssists,
            defender.strength,
            defensiveAssists
        )
    }
}
