package dk.ilios.jervis.fumbbl.model

import dk.ilios.jervis.actions.DBlockResult
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

    fun toJervisResult(): DBlockResult {
        return when (this) {
            SKULL -> DBlockResult(1)
            BOTH_DOWN -> DBlockResult(2)
            PUSHBACK -> DBlockResult(3)
            POW_PUSHBACK -> DBlockResult(5)
            POW -> DBlockResult(6)
        }
    }
}
