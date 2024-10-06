package com.jervisffb.fumbbl.net.model

import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.actions.DicePoolChoice
import com.jervisffb.engine.actions.DicePoolResultsSelected
import com.jervisffb.fumbbl.net.api.serialization.FumbblEnum
import com.jervisffb.fumbbl.net.api.serialization.FumbblEnumSerializer
import kotlinx.serialization.Serializable

class BlockResultSerializer : FumbblEnumSerializer<com.jervisffb.fumbbl.net.model.BlockResult>(com.jervisffb.fumbbl.net.model.BlockResult::class)

@Serializable(with = com.jervisffb.fumbbl.net.model.BlockResultSerializer::class)
enum class BlockResult(
    override val id: String,
) : FumbblEnum {
    SKULL("SKULL"),
    BOTH_DOWN("BOTH DOWN"),
    PUSHBACK("PUSHBACK"),
    POW_PUSHBACK("POW/PUSH"),
    POW("POW");

    fun toJervisResult(): DicePoolResultsSelected {
        return when (this) {
            com.jervisffb.fumbbl.net.model.BlockResult.SKULL -> DicePoolResultsSelected(listOf(
                DicePoolChoice(id = 0, diceSelected = listOf(
                    DBlockResult(1)
                ))
            ))
            com.jervisffb.fumbbl.net.model.BlockResult.BOTH_DOWN -> DicePoolResultsSelected(listOf(
                DicePoolChoice(id = 0, diceSelected = listOf(
                    DBlockResult(2)
                ))
            ))
            com.jervisffb.fumbbl.net.model.BlockResult.PUSHBACK -> DicePoolResultsSelected(listOf(
                DicePoolChoice(id = 0, diceSelected = listOf(
                    DBlockResult(3)
                ))
            ))
            com.jervisffb.fumbbl.net.model.BlockResult.POW_PUSHBACK -> DicePoolResultsSelected(listOf(
                DicePoolChoice(id = 0, diceSelected = listOf(
                    DBlockResult(5)
                ))
            ))
            com.jervisffb.fumbbl.net.model.BlockResult.POW -> DicePoolResultsSelected(listOf(
                DicePoolChoice(id = 0, diceSelected = listOf(
                    DBlockResult(6)
                ))
            ))
        }
    }
}
