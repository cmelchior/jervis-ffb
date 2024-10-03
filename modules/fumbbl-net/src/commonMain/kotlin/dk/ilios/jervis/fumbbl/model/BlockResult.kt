package dk.ilios.jervis.fumbbl.model

import dk.ilios.jervis.actions.DBlockResult
import dk.ilios.jervis.actions.DicePoolChoice
import dk.ilios.jervis.actions.DicePoolResultsSelected
import dk.ilios.jervis.fumbbl.net.serialization.FumbblEnum
import dk.ilios.jervis.fumbbl.net.serialization.FumbblEnumSerializer
import kotlinx.serialization.Serializable

class BlockResultSerializer : FumbblEnumSerializer<BlockResult>(BlockResult::class)

@Serializable(with = BlockResultSerializer::class)
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
            SKULL -> DicePoolResultsSelected(listOf(DicePoolChoice(id = 0, diceSelected = listOf(DBlockResult(1)))))
            BOTH_DOWN -> DicePoolResultsSelected(listOf(DicePoolChoice(id = 0, diceSelected = listOf(DBlockResult(2)))))
            PUSHBACK -> DicePoolResultsSelected(listOf(DicePoolChoice(id = 0, diceSelected = listOf(DBlockResult(3)))))
            POW_PUSHBACK -> DicePoolResultsSelected(listOf(DicePoolChoice(id = 0, diceSelected = listOf(DBlockResult(5)))))
            POW -> DicePoolResultsSelected(listOf(DicePoolChoice(id = 0, diceSelected = listOf(DBlockResult(6)))))
        }
    }
}
