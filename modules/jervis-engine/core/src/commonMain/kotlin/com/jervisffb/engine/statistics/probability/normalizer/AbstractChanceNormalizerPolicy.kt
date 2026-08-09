package com.jervisffb.engine.statistics.probability.normalizer

import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.statistics.probability.event.ActionPathEvent
import com.jervisffb.engine.statistics.probability.event.ActionPathEventScope
import com.jervisffb.engine.statistics.probability.event.ChanceBranch
import com.jervisffb.engine.statistics.probability.event.OutcomeRatio
import com.jervisffb.engine.statistics.probability.event.RerollCategory
import com.jervisffb.engine.statistics.probability.event.RerollOption
import com.jervisffb.engine.statistics.probability.event.RerollResource
import com.jervisffb.engine.statistics.probability.event.RerollUsage
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

    // TODO This list is not complete
    protected val supportedPrimaryD6Rolls = setOf(
        DiceRollType.CATCH,
        DiceRollType.PICKUP,
        DiceRollType.DODGE,
        DiceRollType.JUMP,
        DiceRollType.RUSH,
        DiceRollType.LEAP,
    )
    protected val activationD6Rolls = setOf(DiceRollType.PRO, DiceRollType.LONER)
    protected val ignoredD6Rolls = setOf(DiceRollType.REGENERATION, DiceRollType.TEAM_CAPTAIN)

    protected fun normalizeBlock(
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

    /**
     * A challenge can finish while the procedure handling its final roll is
     * still active. Keep the raw stream intact, but omit that unfinished
     * logical roll when it and all of its nested/replacement rolls form the
     * terminal observation family.
     */
    protected fun List<ChanceObservation>.withoutTerminalUnfinalizedRollFamily(): List<ChanceObservation> {
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

    protected fun List<ActionPathEvent>.resequence(): List<ActionPathEvent> {
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
