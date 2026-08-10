package com.jervisffb.engine.statistics.probability.normalizer

import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.statistics.probability.event.ActionPathEvent
import com.jervisffb.engine.statistics.probability.event.ActionPathEventScope
import com.jervisffb.engine.statistics.probability.event.ActualRerollUse
import com.jervisffb.engine.statistics.probability.event.ChanceBranch
import com.jervisffb.engine.statistics.probability.event.OutcomeRatio
import com.jervisffb.engine.statistics.probability.event.PhysicalRollRole
import com.jervisffb.engine.statistics.probability.event.RerollCategory
import com.jervisffb.engine.statistics.probability.event.RerollOption
import com.jervisffb.engine.statistics.probability.event.RerollResource
import com.jervisffb.engine.statistics.probability.event.RerollUsage
import com.jervisffb.engine.statistics.probability.observation.ChanceDieResult
import com.jervisffb.engine.statistics.probability.observation.ChanceObservation
import com.jervisffb.engine.statistics.probability.observation.ChanceObservationScope
import com.jervisffb.engine.statistics.probability.observation.ChanceRerollOption
import com.jervisffb.engine.statistics.probability.observation.ChanceRerollSource
import com.jervisffb.engine.statistics.probability.observation.ChanceRerollSourceKind
import com.jervisffb.engine.statistics.probability.observation.ChanceRerollTestEffect

/**
 * Shared conversion and validation helpers for [ChanceNormalizerPolicy]
 * implementations.
 */
abstract class AbstractChanceNormalizerPolicy : ChanceNormalizerPolicy {

    override val activationRollTypes = setOf(
        DiceRollType.LONER,
        DiceRollType.PRO,
    )
    override val ignoredRollTypes = setOf(
        DiceRollType.ARMOUR,
        DiceRollType.REGENERATION,
        DiceRollType.TEAM_CAPTAIN
    )
    override val primaryRollTypes = buildSet {
        // Assume that all rolls that do not have a narrower category should always be
        // included in the scoring.
        addAll(DiceRollType.entries.toSet())
        removeAll(activationRollTypes)
        removeAll(ignoredRollTypes)
    }

    protected fun normalizeLogicalBlock(
        root: ChanceObservation.DiceRoll,
        replacements: Map<Int?, List<ChanceObservation.DiceRoll>>,
    ): ActionPathEvent = normalizeBlock(root, replacements, physical = false)

    protected fun normalizePhysicalBlock(
        root: ChanceObservation.DiceRoll,
        replacements: Map<Int?, List<ChanceObservation.DiceRoll>>,
    ): ActionPathEvent = normalizeBlock(root, replacements, physical = true)

    private fun normalizeBlock(
        root: ChanceObservation.DiceRoll,
        replacements: Map<Int?, List<ChanceObservation.DiceRoll>>,
        physical: Boolean,
    ): ActionPathEvent {
        if (!root.finalized) return unsupported(root, "Block roll was not finalized.")
        if (root.selectedResultIds.size != 1) {
            return unsupported(root, "A block must identify exactly one selected result.")
        }
        val finalDice = finalBlockDice(root, replacements)
        val selectedResultId = root.selectedResultIds.single()
        val selectedLogicalDieId = root.dice.firstOrNull { it.id == selectedResultId }?.dieId
        val selected = (
            finalDice.firstOrNull { it.id == selectedResultId }
                ?: finalDice.firstOrNull { it.dieId == selectedLogicalDieId }
        )?.result as? DBlockResult
            ?: return unsupported(root, "The selected block result was not found.")
        val blockResults = finalDice.map { die ->
            die.result as? DBlockResult
                ?: return unsupported(root, "A block observation contained a non-block result.")
        }

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
        val opponentChooses = root.selectedBy != null && root.selectedBy != root.team
        return when (physical) {
            false -> ActionPathEvent.Logical.block(
                index = root.index,
                owner = root.team,
                results = blockResults,
                selectedFace = selected.blockResult,
                opponentChooses = opponentChooses,
                scope = root.scope.toFixedLineScope(),
                recoveries = recoveryResult.options,
            )
            true -> ActionPathEvent.Physical.block(
                index = root.index,
                traceRootIndex = root.index,
                owner = root.team,
                results = blockResults,
                selectedFace = selected.blockResult,
                opponentChooses = opponentChooses,
                role = PhysicalRollRole.PRIMARY,
                scope = root.scope.toFixedLineScope(),
                recoveries = recoveryResult.options,
                finalized = true,
            )
        }
    }

    protected fun normalizeLogicalOutcome(
        root: ChanceObservation.DiceRoll,
        finalRoll: ChanceObservation.DiceRoll,
    ): ActionPathEvent {
        val outcome = finalRoll.outcome ?: root.outcome
            ?: return unsupported(root, "The roll does not expose a structured outcome interpretation.")
        val success = finalRoll.success
            ?: return unsupported(root, "The roll does not expose a factual success result.")
        if (!root.finalized || !finalRoll.finalized) {
            return unsupported(root, "Outcome roll was not finalized.")
        }
        val recoveryResult = recoveries(root.rerollOptions, success)
        recoveryResult.reason?.let { return unsupported(root, it) }
        return ActionPathEvent.Logical.outcome(
            index = root.index,
            rollType = root.rollType,
            owner = root.team,
            results = finalRoll.dice.map { it.result },
            category = outcome.category,
            isSuccess = success,
            successProbability = outcome.successProbability,
            scope = root.scope.toFixedLineScope(),
            recoveries = recoveryResult.options,
        )
    }

    protected fun normalizePhysicalOutcome(
        roll: ChanceObservation.DiceRoll,
        observations: Map<Int, ChanceObservation.DiceRoll>,
    ): ActionPathEvent {
        val outcome = roll.outcome
            ?: return unsupported(roll, "The roll does not expose a structured outcome interpretation.")
        val success = roll.success
            ?: return unsupported(roll, "The roll does not expose a factual success result.")
        if (!roll.finalized) return unsupported(roll, "Outcome roll was not finalized.")

        val selectedSource = roll.selectedReroll?.takeUnless { it.aborted }?.let { selection ->
            roll.rerollOptions.firstOrNull { it.source.id == selection.sourceId }?.source
                ?: return unsupported(roll, "Selected reroll source ${selection.sourceId.id} was not snapshotted.")
        }
        val sourceResult = selectedSource?.toRecoveryResource()
        sourceResult?.reason?.let { return unsupported(roll, it) }
        val actualRecovery = selectedSource?.let { source ->
            ActualRerollUse(sourceResult!!.resource!!, source.description)
        }
        val recoveryResult = when (actualRecovery) {
            null -> recoveries(roll.rerollOptions, success)
            else -> RecoveryConversion(emptyList())
        }
        recoveryResult.reason?.let { return unsupported(roll, it) }

        return ActionPathEvent.Physical.outcome(
            index = roll.index,
            traceRootIndex = traceRoot(roll, observations),
            rollType = roll.rollType,
            owner = roll.team,
            results = roll.dice.map { it.result },
            category = outcome.category,
            role = when {
                roll.rerolledRollIndex != null -> PhysicalRollRole.REROLL
                roll.rollType in activationRollTypes -> PhysicalRollRole.ACTIVATION
                else -> PhysicalRollRole.PRIMARY
            },
            scope = roll.scope.toFixedLineScope(),
            isSuccess = success,
            successProbability = outcome.successProbability,
            actualRecovery = actualRecovery,
            recoveries = recoveryResult.options,
            finalized = true,
        )
    }

    protected fun traceRoot(
        roll: ChanceObservation.DiceRoll,
        observations: Map<Int, ChanceObservation.DiceRoll>,
    ): Int {
        var current = roll
        while (current.rerolledRollIndex != null) {
            current = observations[current.rerolledRollIndex] ?: break
        }
        return current.index
    }

    /**
     * Replaces the original result for each logical block die with its latest
     * physical result. A block reroll may replace one die or the whole pool.
     */
    private fun finalBlockDice(
        root: ChanceObservation.DiceRoll,
        replacements: Map<Int?, List<ChanceObservation.DiceRoll>>,
    ): List<ChanceDieResult> {
        val diceByLogicalId = root.dice
            .mapNotNull { die -> die.dieId?.let { it to die } }
            .toMap()
            .toMutableMap()
        var current = root
        val seen = mutableSetOf(root.index)
        while (true) {
            val replacement = replacements[current.index].orEmpty().maxByOrNull { it.index } ?: break
            if (!seen.add(replacement.index)) break
            replacement.dice.forEach { die ->
                if (die.dieId != null) diceByLogicalId[die.dieId] = die
            }
            current = replacement
        }
        return root.dice.map { die -> die.dieId?.let(diceByLogicalId::get) ?: die }
    }

    /**
     * A challenge can finish while the procedure handling its final roll is
     * still active. Keep the raw stream intact, but omit that unfinished
     * logical roll when it and all of its nested/replacement rolls form the
     * terminal observation family.
     */
    protected fun List<ChanceObservation>.withoutTerminalUnfinalizedRollFamily(): List<ChanceObservation> {
        val rollsByIndex = filterIsInstance<ChanceObservation.DiceRoll>().associateBy { it.index }
        val newestUnfinalized = asReversed()
            .filterIsInstance<ChanceObservation.DiceRoll>()
            .firstOrNull { !it.finalized }
            ?: return this

        fun ancestorIndexes(roll: ChanceObservation.DiceRoll): Set<Int> {
            val ancestors = mutableSetOf<Int>()
            val pending = ArrayDeque<ChanceObservation.DiceRoll>()
            pending.add(roll)
            while (pending.isNotEmpty()) {
                val current = pending.removeLast()
                if (!ancestors.add(current.index)) continue
                listOfNotNull(current.enclosingRollIndex, current.rerolledRollIndex)
                    .mapNotNull(rollsByIndex::get)
                    .forEach(pending::add)
            }
            return ancestors
        }

        val familyRootIndex = ancestorIndexes(newestUnfinalized).minOrNull() ?: return this
        val familyRoot = rollsByIndex[familyRootIndex] ?: return this
        if (familyRoot.finalized) return this
        val familyStart = indexOfFirst { it.index == familyRootIndex }
        if (familyStart == -1) return this

        val terminalFamily = drop(familyStart).all { observation ->
            observation is ChanceObservation.DiceRoll && familyRootIndex in ancestorIndexes(observation)
        }
        return if (terminalFamily) take(familyStart) else this
    }

    protected data class RecoveryConversion(
        val options: List<RerollOption>,
        val reason: String? = null,
    )

    protected data class ResourceConversion(
        val resource: RerollResource? = null,
        val reason: String? = null,
    )

    protected fun recoveries(
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

    protected fun ChanceRerollSource.toRecoveryResource(): ResourceConversion {
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
            RerollResource(
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
        Duration.END_OF_ACTIVATION -> RerollUsage.ONCE_PER_ACTIVATION
        Duration.END_OF_ACTION -> RerollUsage.ONCE_PER_ACTION
        Duration.END_OF_TURN,
        Duration.END_OF_OWN_TEAM_TURN -> RerollUsage.ONCE_PER_TURN
        Duration.END_OF_DRIVE -> RerollUsage.ONCE_PER_DRIVE
        Duration.END_OF_HALF -> RerollUsage.ONCE_PER_HALF
        Duration.END_OF_GAME -> RerollUsage.ONCE_PER_GAME
        Duration.IMMEDIATE,
        Duration.SPECIAL,
        Duration.STANDING_UP -> RerollUsage.UNSUPPORTED
    }

    protected fun unsupported(
        roll: ChanceObservation.DiceRoll,
        reason: String,
    ): ActionPathEvent.Unsupported = ActionPathEvent.Unsupported(roll.index, roll.rollType, reason)

    protected fun ChanceObservationScope.toFixedLineScope() = ActionPathEventScope(
        half = half,
        drive = drive,
        turn = turn,
        player = player,
    )

    protected fun List<ActionPathEvent>.reindex(): List<ActionPathEvent> {
        val newIndexByOldIndex = associate { it.index to 0 }.toMutableMap()
        forEachIndexed { index, event -> newIndexByOldIndex[event.index] = index }
        return mapIndexed { index, event ->
            when (event) {
                is ActionPathEvent.Logical -> event.copy(index = index)
                is ActionPathEvent.Unsupported -> event.copy(index = index)
                is ActionPathEvent.Physical -> event.copy(
                    index = index,
                    traceRootIndex = newIndexByOldIndex[event.traceRootIndex] ?: index,
                )
            }
        }
    }
}
