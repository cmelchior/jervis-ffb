package com.jervisffb.engine.statistics.probability

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.SkillType

// TODO This list is not complete
private val supportedPrimaryD6Rolls = setOf(
    DiceRollType.CATCH,
    DiceRollType.PICKUP,
    DiceRollType.DODGE,
    DiceRollType.JUMP,
    DiceRollType.RUSH,
    DiceRollType.LEAP,
)

private val activationD6Rolls = setOf(DiceRollType.PRO, DiceRollType.LONER)
private val ignoredD6Rolls = setOf(DiceRollType.REGENERATION, DiceRollType.TEAM_CAPTAIN)

/**
 * The normalizer converts detailed engine probability observations into the
 * smaller, event ledger consumed by the probability scorers. How this is done
 * depends on the exact policy.
 *
 * Currently, two modes exist:
 * - Logical: Treats an initial roll and all its rerolls as one logical event.
 *   This means that rerolling dice does not have any negative consequences. The
 *   final probability will be calculated as if the reroll didn't happen.
 *
 * - Physical: Preserves every physical D6, including Pro/Loner activation rolls
 *   and rerolled dice. This means that rerolling dice will impact the final
 *   probability in a negative way as reroll sources are marked as used (and
 *   cannot be used again) and an extra roll (most likely with a non-zero chance
 *   of succeeding) is added to the list of rolls.
 *
 * Once the list of random events has been normalized to a dice roll ledger,
 * it is handed off to the selected scorer, which consumes the normalized events
 * and uses a small forward dynamic program to calculate the optimal use of
 * rerolls to maximize the chance of total success for the entire action path.
 *
 * See [LogicalActionPathScorer]
 * See [PhysicalActionPathScorer]
 */
internal object ChanceNormalizer {
    fun fixed(observations: List<ChanceObservation>): List<ActionPathEvent> {
        val scoreableObservations = observations.withoutTerminalUnfinalizedRollFamily()
        val diceRolls = scoreableObservations.filterIsInstance<ChanceObservation.DiceRoll>()
        val replacements = diceRolls.groupBy { it.rerolledRollId }
        val normalized = buildList {
            scoreableObservations.forEach { observation ->
                when (observation) {
                    is ChanceObservation.UnstructuredAction -> add(
                        ActionPathEvent.Unsupported(
                            observation.index,
                            reason = "${observation.nodeDescription} emitted ${observation.actionName} without structured dice data.",
                        ),
                    )
                    is ChanceObservation.DiceRoll -> when {
                        observation.rerolledRollId != null -> Unit
                        observation.enclosingRollId != null -> Unit
                        observation.rollType in ignoredD6Rolls -> Unit
                        observation.rollType in activationD6Rolls -> Unit
                        observation.rollType in supportedPrimaryD6Rolls -> add(
                            normalizeLogicalD6(observation, replacements),
                        )
                        observation.rollType == DiceRollType.BLOCK -> add(
                            normalizeBlock(observation, diceRolls, replacements),
                        )
                        else -> add(unsupported(observation, "Roll type is not supported by fixed-line scoring."))
                    }
                }
            }
        }
        return normalized.resequence()
    }

    fun hybrid(observations: List<ChanceObservation>): List<ActionPathEvent> {
        val scoreableObservations = observations.withoutTerminalUnfinalizedRollFamily()
        val diceRolls = scoreableObservations.filterIsInstance<ChanceObservation.DiceRoll>()
        val byId = diceRolls.associateBy { it.index }
        val replacements = diceRolls.groupBy { it.rerolledRollId }
        val normalized = buildList {
            scoreableObservations.forEach { observation ->
                when (observation) {
                    is ChanceObservation.UnstructuredAction -> add(
                        ActionPathEvent.Unsupported(
                            observation.index,
                            reason = "${observation.nodeDescription} emitted ${observation.actionName} without structured dice data.",
                        ),
                    )
                    is ChanceObservation.DiceRoll -> when {
                        observation.rollType == DiceRollType.BLOCK && observation.rerolledRollId == null -> add(
                            normalizeBlock(observation, diceRolls, replacements),
                        )
                        observation.rollType == DiceRollType.BLOCK -> Unit
                        observation.rollType in ignoredD6Rolls -> Unit
                        observation.rollType in supportedPrimaryD6Rolls || observation.rollType in activationD6Rolls -> add(
                            normalizePhysicalD6(observation, byId),
                        )
                        observation.rerolledRollId != null -> Unit
                        else -> add(unsupported(observation, "Roll type is not supported by hybrid scoring."))
                    }
                }
            }
        }
        return normalized.resequence()
    }

    private fun normalizeLogicalD6(
        root: ChanceObservation.DiceRoll,
        replacements: Map<Int?, List<ChanceObservation.DiceRoll>>,
    ): ActionPathEvent {
        val finalRoll = finalReplacement(root, replacements)
        if (!root.finalized || !finalRoll.finalized) {
            return unsupported(root, "D6 roll was not finalized.")
        }
        val result = finalRoll.dice.singleOrNull()?.result as? D6Result
            ?: return unsupported(root, "Expected one D6 result.")
        val success = finalRoll.success
            ?: return unsupported(root, "The roll does not expose a factual success result.")
        val legalValues = if (success) 2..6 else 1..5
        if (result.value !in legalValues) {
            return unsupported(root, "D6 value ${result.value} is inconsistent with success=$success.")
        }
        val recoveryResult = recoveries(root.rerollOptions, success)
        recoveryResult.reason?.let { return unsupported(root, it) }
        return ActionPathEvent.D6(
            index = root.index,
            rollType = root.rollType,
            owner = root.teamId,
            selectedValue = result.value,
            observedSuccess = success,
            scope = root.scope.toFixedLineScope(),
            recoveries = recoveryResult.options,
        )
    }

    private fun normalizePhysicalD6(
        roll: ChanceObservation.DiceRoll,
        observations: Map<Int, ChanceObservation.DiceRoll>,
    ): ActionPathEvent {
        val result = roll.dice.singleOrNull()?.result as? D6Result
            ?: return unsupported(roll, "Expected one physical D6 result.")
        if (!roll.finalized) return unsupported(roll, "Physical D6 roll was not finalized.")

        val selectedSource = roll.selectedReroll?.takeUnless { it.aborted }?.let { selection ->
            roll.rerollOptions.firstOrNull { it.source.id == selection.sourceId }?.source
                ?: return unsupported(roll, "Selected reroll source ${selection.sourceId.id} was not snapshotted.")
        }
        val sourceResult = selectedSource?.toRecoveryResource()
        sourceResult?.reason?.let { return unsupported(roll, it) }
        val actualRecovery = selectedSource?.let { source ->
            ActualRecoveryUse(sourceResult!!.resource!!, source.description)
        }
        val recoveryResult = when (actualRecovery) {
            null -> recoveries(roll.rerollOptions, roll.success)
            else -> RecoveryConversion(emptyList())
        }
        recoveryResult.reason?.let { return unsupported(roll, it) }

        return ActionPathEvent.PhysicalD6(
            index = roll.index,
            traceRootSequence = traceRoot(roll, observations),
            rollType = roll.rollType,
            owner = roll.teamId,
            selectedValue = result,
            role = when {
                roll.rerolledRollId != null -> PhysicalD6Role.REROLL
                roll.rollType in activationD6Rolls -> PhysicalD6Role.ACTIVATION
                else -> PhysicalD6Role.PRIMARY
            },
            scope = roll.scope.toFixedLineScope(),
            observedSuccess = if (actualRecovery == null) roll.success else null,
            actualRecovery = actualRecovery,
            recoveries = recoveryResult.options,
            finalized = true,
        )
    }

    private fun normalizeBlock(
        root: ChanceObservation.DiceRoll,
        diceRolls: List<ChanceObservation.DiceRoll>,
        replacements: Map<Int?, List<ChanceObservation.DiceRoll>>,
    ): ActionPathEvent {
        if (!root.finalized) return unsupported(root, "Block roll was not finalized.")
        if (replacements[root.index].orEmpty().isNotEmpty()) {
            return unsupported(root, "Demonstrated block rerolls are not supported by chance scoring v1.")
        }
        if (root.selectedResultIds.size != 1) {
            return unsupported(root, "A block must identify exactly one selected result.")
        }
        val resultId = root.selectedResultIds.single()
        val selected = diceRolls
            .asSequence()
            .flatMap { it.dice.asSequence() }
            .firstOrNull { it.id == resultId }
            ?.result as? DBlockResult
            ?: return unsupported(root, "The selected block result was not found.")

        val nonTeamSource = root.rerollOptions.firstOrNull {
            it.source.kind != ChanceRerollSourceKind.TEAM_REROLL
        }
        if (nonTeamSource != null) {
            return unsupported(root, "Partial block-die rerolls are not supported by chance scoring v1.")
        }
        val rootResultIds = root.dice.map { it.id }.toSet()
        if (root.rerollOptions.any { it.resultIds.toSet() != rootResultIds }) {
            return unsupported(root, "Partial block-die rerolls are not supported by chance scoring v1.")
        }
        val recoveryResult = recoveries(root.rerollOptions, observedSuccess = null)
        recoveryResult.reason?.let { return unsupported(root, it) }
        return ActionPathEvent.Block(
            index = root.index,
            owner = root.teamId,
            selectedFace = selected.blockResult,
            diceCount = root.dice.size,
            opponentChooses = root.selectedBy != null && root.selectedBy != root.teamId,
            scope = root.scope.toFixedLineScope(),
            recoveries = recoveryResult.options,
        )
    }

    private fun finalReplacement(
        root: ChanceObservation.DiceRoll,
        replacements: Map<Int?, List<ChanceObservation.DiceRoll>>,
    ): ChanceObservation.DiceRoll {
        var current = root
        val seen = mutableSetOf(root.index)
        while (true) {
            val replacement = replacements[current.index].orEmpty().maxByOrNull { it.index } ?: return current
            if (!seen.add(replacement.index)) return current
            current = replacement
        }
    }

    private fun traceRoot(
        roll: ChanceObservation.DiceRoll,
        observations: Map<Int, ChanceObservation.DiceRoll>,
    ): Int {
        var current = roll
        while (current.rerolledRollId != null) {
            current = observations[current.rerolledRollId] ?: break
        }
        return current.index
    }

    /**
     * A challenge can finish while the procedure handling its final roll is
     * still active. Keep the raw stream intact, but omit that unfinished
     * logical roll when it and all of its nested/replacement rolls form the
     * terminal observation family.
     */
    private fun List<ChanceObservation>.withoutTerminalUnfinalizedRollFamily(): List<ChanceObservation> {
        val rollsById = filterIsInstance<ChanceObservation.DiceRoll>().associateBy { it.index }
        val newestUnfinalized = asReversed()
            .filterIsInstance<ChanceObservation.DiceRoll>()
            .firstOrNull { !it.finalized }
            ?: return this

        fun ancestorIds(roll: ChanceObservation.DiceRoll): Set<Int> {
            val ancestors = mutableSetOf<Int>()
            val pending = ArrayDeque<ChanceObservation.DiceRoll>()
            pending.add(roll)
            while (pending.isNotEmpty()) {
                val current = pending.removeLast()
                if (!ancestors.add(current.index)) continue
                listOfNotNull(current.enclosingRollId, current.rerolledRollId)
                    .mapNotNull(rollsById::get)
                    .forEach(pending::add)
            }
            return ancestors
        }

        val familyRootId = ancestorIds(newestUnfinalized).minOrNull() ?: return this
        val familyRoot = rollsById[familyRootId] ?: return this
        if (familyRoot.finalized) return this
        val familyStart = indexOfFirst { it.index == familyRootId }
        if (familyStart == -1) return this

        val terminalFamily = drop(familyStart).all { observation ->
            observation is ChanceObservation.DiceRoll && familyRootId in ancestorIds(observation)
        }
        return if (terminalFamily) take(familyStart) else this
    }

    private data class RecoveryConversion(
        val options: List<RerollOption>,
        val reason: String? = null,
    )

    private data class ResourceConversion(
        val resource: RecoveryResource? = null,
        val reason: String? = null,
    )

    private fun recoveries(
        rawOptions: List<ChanceRerollOption>,
        observedSuccess: Boolean?,
    ): RecoveryConversion {
        val options = mutableListOf<RerollOption>()
        rawOptions.distinctBy { it.source.id }.forEach { raw ->
            val resource = raw.source.toRecoveryResource()
            resource.reason?.let { return RecoveryConversion(emptyList(), it) }
            val restoreTest = raw.source.tests.firstOrNull { it.effect == ChanceRerollTestEffect.RESTORES_SOURCE }
            if (restoreTest != null) {
                return RecoveryConversion(
                    emptyList(),
                    "Recovery ${raw.source.id.id} can be restored by ${restoreTest.rollType.description}.",
                )
            }
            val activationTests = raw.source.tests.filter { it.effect == ChanceRerollTestEffect.ALLOWS_REROLL }
            if (activationTests.size > 1) {
                return RecoveryConversion(emptyList(), "Recovery ${raw.source.id.id} has multiple activation tests.")
            }
            val activation = activationTests.singleOrNull()?.let { test ->
                if (test.successTarget !in 1..test.dieSides) {
                    return RecoveryConversion(emptyList(), "Recovery ${raw.source.id.id} has an invalid activation target.")
                }
                OutcomeRatio(test.dieSides - test.successTarget + 1, test.dieSides)
            } ?: OutcomeRatio.CERTAIN
            val branches = buildSet {
                when (observedSuccess) {
                    true -> {
                        if (raw.appliesOnSuccess) add(ChanceBranch.SELECTED)
                        if (raw.appliesOnFailure) add(ChanceBranch.ALTERNATIVE)
                    }
                    false -> {
                        if (raw.appliesOnFailure) add(ChanceBranch.SELECTED)
                        if (raw.appliesOnSuccess) add(ChanceBranch.ALTERNATIVE)
                    }
                    null -> {
                        if (raw.appliesOnSuccess || raw.appliesOnFailure) {
                            add(ChanceBranch.SELECTED)
                            add(ChanceBranch.ALTERNATIVE)
                        }
                    }
                }
            }
            if (branches.isNotEmpty()) {
                options.add(RerollOption(resource.resource!!, activation, branches))
            }
        }
        return RecoveryConversion(options)
    }

    private fun ChanceRerollSource.toRecoveryResource(): ResourceConversion {
        val category = when (kind) {
            ChanceRerollSourceKind.TEAM_REROLL -> RerollCategory.TEAM_REROLL
            ChanceRerollSourceKind.SKILL -> when (skillId?.type) {
                SkillType.PRO -> RerollCategory.PRO
                null -> return ResourceConversion(reason = "Skill recovery $id is missing its skill ID.")
                else -> RerollCategory.STANDARD_SKILL
            }
            ChanceRerollSourceKind.OTHER -> {
                return ResourceConversion(reason = "Recovery $id has an unsupported source kind.")
            }
        }
        return ResourceConversion(
            RecoveryResource(
                id = id,
                owner = owner,
                category = category,
                usage = resetAt.toRecoveryUsage(),
            ),
        )
    }

    private fun Duration.toRecoveryUsage(): RerollUsage = when (this) {
        Duration.PERMANENT -> RerollUsage.REUSABLE
        Duration.START_OF_ACTIVATION,
        Duration.END_OF_ACTIVATION,
        -> RerollUsage.ONCE_PER_ACTIVATION
        Duration.END_OF_ACTION -> RerollUsage.ONCE_PER_ACTION
        Duration.END_OF_TURN,
        Duration.END_OF_OWN_TEAM_TURN,
        -> RerollUsage.ONCE_PER_TURN
        Duration.END_OF_DRIVE -> RerollUsage.ONCE_PER_DRIVE
        Duration.END_OF_HALF -> RerollUsage.ONCE_PER_HALF
        Duration.END_OF_GAME -> RerollUsage.ONCE_PER_GAME
        Duration.IMMEDIATE,
        Duration.SPECIAL,
        Duration.STANDING_UP,
        -> RerollUsage.UNSUPPORTED
    }

    private fun unsupported(
        roll: ChanceObservation.DiceRoll,
        reason: String,
    ): ActionPathEvent.Unsupported = ActionPathEvent.Unsupported(roll.index, roll.rollType, reason)

    private fun ChanceObservationScope.toFixedLineScope() = ActionPathEventScope(
        half = half,
        drive = drive,
        turn = turn,
        player = player,
    )

    private fun List<ActionPathEvent>.resequence(): List<ActionPathEvent> {
        val sequenceMap = associate { it.index to 0 }.toMutableMap()
        forEachIndexed { index, event -> sequenceMap[event.index] = index }
        return mapIndexed { index, event ->
            when (event) {
                is ActionPathEvent.D6 -> event.copy(index = index)
                is ActionPathEvent.Block -> event.copy(index = index)
                is ActionPathEvent.Unsupported -> event.copy(index = index)
                is ActionPathEvent.PhysicalD6 -> event.copy(
                    index = index,
                    traceRootSequence = sequenceMap[event.traceRootSequence] ?: index,
                )
            }
        }
    }
}
