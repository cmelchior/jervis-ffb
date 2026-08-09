package com.jervisffb.engine.statistics.probability.event

import com.jervisffb.engine.actions.BlockDice
import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.statistics.probability.normalizer.ChanceNormalizer
import kotlinx.serialization.Serializable

/**
 * A normalized chance event in one chosen action path.
 *
 * Raw engine observations are converted by [ChanceNormalizer] into either one
 * logical event with replacement rolls collapsed, or the individual physical
 * rolls that occurred.
 */
@Serializable
sealed class ActionPathEvent {
    abstract val index: Int

    /** How the demonstrated action path interprets the selected result(s). */
    @Serializable
    sealed interface Resolution {
        val isSuccess: Boolean

        /** A conventional single-die threshold test. */
        @Serializable
        data class Dice(
            override val isSuccess: Boolean,
        ) : Resolution

        /** A procedure-supplied interpretation such as an at-least or target-set objective. */
        @Serializable
        data class Outcome(
            val category: ChanceOutcomeCategory,
            override val isSuccess: Boolean,
        ) : Resolution

        /** A selected face from a pool of block dice. */
        @Serializable
        data class Block(
            val selectedFace: BlockDice,
            val opponentChooses: Boolean,
        ) : Resolution {
            override val isSuccess: Boolean = true
        }
    }

    /** One logical chance event, with any replacement rolls collapsed into it. */
    @Serializable
    data class Logical(
        override val index: Int,
        val rollType: DiceRollType,
        val owner: TeamId,
        val results: List<DieResult>,
        val resolution: Resolution,
        val observedOutcome: OutcomeRatio,
        val scope: ActionPathEventScope,
        val recoveries: List<RerollOption> = emptyList(),
    ) : ActionPathEvent() {
        init {
            validateResults(results, resolution)
        }

        companion object {
            /** Creates a conventional single-die threshold event. */
            fun die(
                index: Int,
                rollType: DiceRollType,
                owner: TeamId,
                result: DieResult,
                isSuccess: Boolean,
                scope: ActionPathEventScope,
                recoveries: List<RerollOption> = emptyList(),
            ) = Logical(
                index = index,
                rollType = rollType,
                owner = owner,
                results = listOf(result),
                resolution = Resolution.Dice(isSuccess),
                observedOutcome = thresholdOutcome(result, isSuccess),
                scope = scope,
                recoveries = recoveries,
            )

            /** Creates an event whose probability is supplied by its rule procedure. */
            fun outcome(
                index: Int,
                rollType: DiceRollType,
                owner: TeamId,
                results: List<DieResult>,
                category: ChanceOutcomeCategory,
                isSuccess: Boolean,
                successProbability: OutcomeRatio,
                scope: ActionPathEventScope,
                recoveries: List<RerollOption> = emptyList(),
            ) = Logical(
                index = index,
                rollType = rollType,
                owner = owner,
                results = results,
                resolution = Resolution.Outcome(category, isSuccess),
                observedOutcome = successProbability.observedBranch(isSuccess),
                scope = scope,
                recoveries = recoveries,
            )

            /** Creates a selected block-face event from the final block-dice pool. */
            fun block(
                index: Int,
                owner: TeamId,
                results: List<DBlockResult>,
                selectedFace: BlockDice,
                opponentChooses: Boolean,
                scope: ActionPathEventScope,
                recoveries: List<RerollOption> = emptyList(),
            ) = Logical(
                index = index,
                rollType = DiceRollType.BLOCK,
                owner = owner,
                results = results,
                resolution = Resolution.Block(selectedFace, opponentChooses),
                observedOutcome = blockOutcome(selectedFace, results.size, opponentChooses),
                scope = scope,
                recoveries = recoveries,
            )
        }
    }

    /** One physical roll selected by the coach, preserving actual reroll usage. */
    @Serializable
    data class Physical(
        override val index: Int,
        val traceRootIndex: Int,
        val rollType: DiceRollType,
        val owner: TeamId,
        val results: List<DieResult>,
        val resolution: Resolution,
        val role: PhysicalRollRole,
        val scope: ActionPathEventScope,
        val observedOutcome: OutcomeRatio,
        val actualRecovery: ActualRerollUse? = null,
        val recoveries: List<RerollOption> = emptyList(),
        val finalized: Boolean = false,
    ) : ActionPathEvent() {
        init {
            require(traceRootIndex <= index) {
                "A physical chance event cannot precede its trace root: $traceRootIndex > $index"
            }
            validateResults(results, resolution)
        }

        val canUseHypotheticalRecovery: Boolean
            get() = finalized && role != PhysicalRollRole.REROLL && actualRecovery == null

        companion object {
            /** Creates a conventional physical single-die threshold event. */
            fun die(
                index: Int,
                traceRootIndex: Int,
                rollType: DiceRollType,
                owner: TeamId,
                result: DieResult,
                role: PhysicalRollRole,
                scope: ActionPathEventScope,
                isSuccess: Boolean,
                actualRecovery: ActualRerollUse? = null,
                recoveries: List<RerollOption> = emptyList(),
                finalized: Boolean = false,
            ) = Physical(
                index = index,
                traceRootIndex = traceRootIndex,
                rollType = rollType,
                owner = owner,
                results = listOf(result),
                resolution = Resolution.Dice(isSuccess),
                role = role,
                scope = scope,
                observedOutcome = thresholdOutcome(result, isSuccess = true),
                actualRecovery = actualRecovery,
                recoveries = recoveries,
                finalized = finalized,
            )

            /** Creates a physical event whose probability is supplied by its rule procedure. */
            fun outcome(
                index: Int,
                traceRootIndex: Int,
                rollType: DiceRollType,
                owner: TeamId,
                results: List<DieResult>,
                category: ChanceOutcomeCategory,
                role: PhysicalRollRole,
                scope: ActionPathEventScope,
                isSuccess: Boolean,
                successProbability: OutcomeRatio,
                actualRecovery: ActualRerollUse? = null,
                recoveries: List<RerollOption> = emptyList(),
                finalized: Boolean = false,
            ) = Physical(
                index = index,
                traceRootIndex = traceRootIndex,
                rollType = rollType,
                owner = owner,
                results = results,
                resolution = Resolution.Outcome(category, isSuccess),
                role = role,
                scope = scope,
                observedOutcome = successProbability.observedBranch(isSuccess),
                actualRecovery = actualRecovery,
                recoveries = recoveries,
                finalized = finalized,
            )

            /** Creates a physical selected block-face event from the final dice pool. */
            fun block(
                index: Int,
                traceRootIndex: Int,
                owner: TeamId,
                results: List<DBlockResult>,
                selectedFace: BlockDice,
                opponentChooses: Boolean,
                role: PhysicalRollRole,
                scope: ActionPathEventScope,
                actualRecovery: ActualRerollUse? = null,
                recoveries: List<RerollOption> = emptyList(),
                finalized: Boolean = false,
            ) = Physical(
                index = index,
                traceRootIndex = traceRootIndex,
                rollType = DiceRollType.BLOCK,
                owner = owner,
                results = results,
                resolution = Resolution.Block(selectedFace, opponentChooses),
                role = role,
                scope = scope,
                observedOutcome = blockOutcome(selectedFace, results.size, opponentChooses),
                actualRecovery = actualRecovery,
                recoveries = recoveries,
                finalized = finalized,
            )
        }
    }

    /** A chance observation that cannot be scored without guessing. */
    @Serializable
    data class Unsupported(
        override val index: Int,
        val rollType: DiceRollType? = null,
        val reason: String,
    ) : ActionPathEvent()
}

private fun validateResults(
    results: List<DieResult>,
    resolution: ActionPathEvent.Resolution,
) {
    require(results.isNotEmpty()) { "A chance event must contain at least one result." }
    when (resolution) {
        is ActionPathEvent.Resolution.Dice -> require(results.size == 1 && results.single() !is DBlockResult) {
            "A conventional die resolution must contain exactly one non-block result."
        }
        is ActionPathEvent.Resolution.Outcome -> Unit
        is ActionPathEvent.Resolution.Block -> {
            require(results.all { it is DBlockResult }) {
                "A block resolution must contain only block-die results."
            }
            require(results.filterIsInstance<DBlockResult>().any { it.blockResult == resolution.selectedFace }) {
                "The selected block face must occur in the final block-dice pool."
            }
        }
    }
}

private fun thresholdOutcome(result: DieResult, isSuccess: Boolean): OutcomeRatio {
    val possibleOutcomes = result.max - result.min + 1
    val favorableOutcomes = when (isSuccess) {
        true -> result.max - result.value + 1
        false -> result.value - result.min + 1
    }
    return OutcomeRatio(favorableOutcomes, possibleOutcomes)
}

private fun OutcomeRatio.observedBranch(success: Boolean): OutcomeRatio = if (success) this else complement

private fun blockOutcome(
    selectedFace: BlockDice,
    diceCount: Int,
    opponentChooses: Boolean,
): OutcomeRatio {
    require(diceCount > 0) { "A block needs at least one die: $diceCount" }
    val favorableFaces = if (selectedFace == BlockDice.PUSH_BACK) 2L else 1L
    val possibleOutcomes = integerPower(6L, diceCount)
    val favorableOutcomes = when (opponentChooses) {
        true -> integerPower(favorableFaces, diceCount)
        false -> possibleOutcomes - integerPower(6L - favorableFaces, diceCount)
    }
    require(possibleOutcomes <= Int.MAX_VALUE && favorableOutcomes <= Int.MAX_VALUE) {
        "Block dice pool is too large to represent as an exact outcome ratio: $diceCount"
    }
    return OutcomeRatio(favorableOutcomes.toInt(), possibleOutcomes.toInt())
}

private fun integerPower(base: Long, exponent: Int): Long {
    var result = 1L
    repeat(exponent) { result *= base }
    return result
}
