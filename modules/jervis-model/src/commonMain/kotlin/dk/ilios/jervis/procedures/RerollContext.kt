package dk.ilios.jervis.procedures

import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.DBlockResult
import dk.ilios.jervis.actions.DieResult
import dk.ilios.jervis.rules.skills.RerollSource
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

// ID that identities a single die
// Used to track individual dice through rerolls
@OptIn(ExperimentalUuidApi::class)
@JvmInline
@Serializable
value class DieId(val id: String) {
    constructor(id: Uuid) : this(id.toString())

    companion object {
        fun generate(): DieId = DieId(Uuid.random())
    }
}

sealed interface DieRoll<D : DieResult> {
    val id: DieId
    val originalRoll: D
    var rerollSource: RerollSource?
    var rerolledResult: D?
    val result: D
}

/**
 * Wrap a single Block die roll. This makes it possible to track it all the way from being rolled to its final result
 */
data class BlockDieRoll(
    override val originalRoll: DBlockResult,
    override var rerollSource: RerollSource? = null,
    override var rerolledResult: DBlockResult? = null,
) : DieRoll<DBlockResult> {
    override val id: DieId = DieId.generate()
    override val result: DBlockResult
        get() = rerolledResult ?: originalRoll
}

/**
 * Wrap a single D6 die roll. This makes it possible to track it all the way from being rolled to its final result.
 */
data class D6DieRoll(
    override val originalRoll: D6Result,
    override var rerollSource: RerollSource? = null,
    override var rerolledResult: D6Result? = null,
) : DieRoll<D6Result> {
    override val id: DieId = DieId.generate()
    override val result: D6Result
        get() = rerolledResult ?: originalRoll
}


