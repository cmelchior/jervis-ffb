package com.jervisffb.engine.statistics.probability

import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.utils.sum

/**
 * Variant of [LogicalActionPathScorer], but instead of always using fixed
 * (optimal) rerolls when calculating scores, in this variant, any rerolls
 * used are included in the score for the action.
 *
 * For actions where no rerolls are used, the optimal reroll type is still
 * selected when calculating the overall success chance.
 *
 * This approach has the advantage that the scoring mirrors what is happening in
 * the UI, but it also means that using rerolls during a challenge will be
 * penalized. Rolls caused by a reroll are marked as such, allowing coaches to
 * figure out how to improve.
 */
object PhysicalActionPathScorer {
    val ALGORITHM_ID = AlgorithmId("physical-path-bb2025-v1")
    val POLICY_ID = RerollUsagePolicyId("selected-then-fixed-rerolls-v1")

    /** Normalizes observations and scores an action path while preserving actual reroll choices. */
    fun score(
        observations: List<ChanceObservation>,
        solvingTeamId: TeamId,
        allowMultipleTeamRerollsPerTurn: Boolean,
        stateCeiling: Int = LogicalActionPathScorer.DEFAULT_STATE_CEILING,
    ): ProbabilityScoreResult = scoreNormalized(
        events = ChanceNormalizer.hybrid(observations),
        solvingTeamId = solvingTeamId,
        allowMultipleTeamRerollsPerTurn = allowMultipleTeamRerollsPerTurn,
        stateCeiling = stateCeiling,
    )

    /** Scores an already normalized action path without repeating normalization. */
    internal fun scoreNormalized(
        events: List<ActionPathEvent>,
        solvingTeamId: TeamId,
        allowMultipleTeamRerollsPerTurn: Boolean,
        stateCeiling: Int = LogicalActionPathScorer.DEFAULT_STATE_CEILING,
    ): ProbabilityScoreResult {
        require(stateCeiling > 0) { "State ceiling must be positive: $stateCeiling" }

        val unsupportedReasons = validate(events)
        if (unsupportedReasons.isNotEmpty()) return unsupported(events, unsupportedReasons)

        val primaryProbability = events.fold(Probability.ALWAYS) { probability, event ->
            when (event) {
                is ActionPathEvent.Block -> probability * event.observedOutcomeProbability
                is ActionPathEvent.PhysicalD6 -> when (event.role) {
                    PhysicalD6Role.PRIMARY -> probability * event.observedOutcome.probability
                    PhysicalD6Role.ACTIVATION,
                    PhysicalD6Role.REROLL,
                    -> probability
                }
                is ActionPathEvent.D6,
                is ActionPathEvent.Unsupported,
                -> probability
            }
        }
        val demonstratedProbability = events.fold(Probability.ALWAYS) { probability, event ->
            when (event) {
                is ActionPathEvent.Block -> probability * event.observedOutcomeProbability
                is ActionPathEvent.PhysicalD6 -> probability * event.observedOutcome.probability
                is ActionPathEvent.D6,
                is ActionPathEvent.Unsupported,
                -> probability
            }
        }
        if (primaryProbability == Probability.NEVER || demonstratedProbability == Probability.NEVER) {
            return unsupported(events, listOf("The demonstrated line probability underflowed to zero."))
        }

        val relevantStates = relevantResourceStates(events, allowMultipleTeamRerollsPerTurn)
        var states: Map<ResourceState, Probability> = mapOf(ResourceState() to Probability.ALWAYS)
        events.forEachIndexed { eventIndex, event ->
            val next = mutableMapOf<ResourceState, Probability>()
            states.forEach { (resourceState, lineProbability) ->
                when (event) {
                    is ActionPathEvent.Block -> expandHypothetical(
                        event,
                        solvingTeamId,
                        allowMultipleTeamRerollsPerTurn,
                        resourceState,
                        lineProbability,
                        next,
                    )
                    is ActionPathEvent.PhysicalD6 -> expandPhysical(
                        event,
                        solvingTeamId,
                        allowMultipleTeamRerollsPerTurn,
                        resourceState,
                        lineProbability,
                        next,
                    )
                    is ActionPathEvent.D6,
                    is ActionPathEvent.Unsupported,
                    -> Unit // Rejected during validation.
                }
            }
            val relevantAfterEvent = relevantStates[eventIndex + 1]
            val pruned = mutableMapOf<ResourceState, Probability>()
            next.forEach { (state, probability) ->
                pruned.add(state.retain(relevantAfterEvent), probability)
            }
            if (pruned.size > stateCeiling) {
                return unsupported(
                    events,
                    listOf("State ceiling $stateCeiling exceeded after event $eventIndex (${pruned.size} states)."),
                )
            }
            states = pruned
        }

        val successProbability = states.values.sum()
        if (successProbability == Probability.NEVER) {
            return unsupported(events, listOf("The hybrid-policy probability underflowed to zero."))
        }

        val baseRisk = primaryProbability.toSurprisal()
        val demonstratedRisk = demonstratedProbability.toSurprisal()
        val finalRisk = successProbability.toSurprisal()
        val actualAdjustment = SurprisalAdjustment.from(baseRisk, demonstratedRisk)
        val hypotheticalAdjustment = SurprisalAdjustment.from(demonstratedRisk, finalRisk)
        return ProbabilityScoreResult.Scored(
            algorithmId = ALGORITHM_ID,
            policyId = POLICY_ID,
            events = events,
            baseProbability = primaryProbability,
            demonstratedProbability = demonstratedProbability,
            successProbability = successProbability,
            baseSurprisal = baseRisk,
            actualExtraRollAdjustment = actualAdjustment,
            hypotheticalRecoveryAdjustment = hypotheticalAdjustment,
            rerollAdjustment = actualAdjustment + hypotheticalAdjustment,
            surprisal = finalRisk,
        )
    }

    /** Expands a physical D6, respecting any reroll actually chosen in the trace. */
    private fun expandPhysical(
        event: ActionPathEvent.PhysicalD6,
        solvingTeamId: TeamId,
        allowMultipleTeamRerollsPerTurn: Boolean,
        resourceState: ResourceState,
        lineProbability: Probability,
        target: MutableMap<ResourceState, Probability>,
    ) {
        val actualRecovery = event.actualRecovery
        if (actualRecovery != null) {
            if (!resourceState.isAvailable(actualRecovery.resource, event, allowMultipleTeamRerollsPerTurn)) return
            val consumed = resourceState.consume(
                actualRecovery.resource,
                event,
                trackTeamTurn = !allowMultipleTeamRerollsPerTurn,
            )
            target.add(consumed, lineProbability * event.observedOutcome.probability)
        } else if (event.canUseHypotheticalRecovery) {
            expandHypothetical(
                event,
                solvingTeamId,
                allowMultipleTeamRerollsPerTurn,
                resourceState,
                lineProbability,
                target,
            )
        } else {
            target.add(resourceState, lineProbability * event.observedOutcome.probability)
        }
    }

    /** Expands a hypothetical recovery branch for an event without an actual reroll choice. */
    private fun expandHypothetical(
        event: ActionPathEvent,
        solvingTeamId: TeamId,
        allowMultipleTeamRerollsPerTurn: Boolean,
        resourceState: ResourceState,
        lineProbability: Probability,
        target: MutableMap<ResourceState, Probability>,
    ) {
        val observedProbability = event.observedOutcomeProbability
        val alternativeProbability = Probability.ALWAYS - observedProbability
        val ownerIsSolvingTeam = event.owner == solvingTeamId
        val recoveryBranch = if (ownerIsSolvingTeam) ChanceBranch.ALTERNATIVE else ChanceBranch.SELECTED
        val recovery = FixedRerollUsagePolicy.select(
            event.recoveries.filter { option ->
                recoveryBranch in option.appliesTo &&
                    resourceState.isAvailable(option.resource, event, allowMultipleTeamRerollsPerTurn)
            },
        )

        if (ownerIsSolvingTeam) {
            target.add(resourceState, lineProbability * observedProbability)
            if (recovery != null) {
                val consumed = resourceState.consume(
                    recovery.resource,
                    event,
                    trackTeamTurn = !allowMultipleTeamRerollsPerTurn,
                )
                val recovered = alternativeProbability * recovery.activation.probability * observedProbability
                target.add(consumed, lineProbability * recovered)
            }
        } else if (recovery == null) {
            target.add(resourceState, lineProbability * observedProbability)
        } else {
            val consumed = resourceState.consume(
                recovery.resource,
                event,
                trackTeamTurn = !allowMultipleTeamRerollsPerTurn,
            )
            val activation = recovery.activation.probability
            val survivesRecovery = (Probability.ALWAYS - activation) + activation * observedProbability
            target.add(consumed, lineProbability * observedProbability * survivesRecovery)
        }
    }

    /** Returns validation errors that prevent this scorer from evaluating [events]. */
    private fun validate(events: List<ActionPathEvent>): List<String> = buildList {
        events.forEachIndexed { index, event ->
            if (event.index != index) add("Expected event sequence $index but found ${event.index}.")
            when (event) {
                is ActionPathEvent.D6 -> add("Logical D6 event $index belongs to the fixed-recovery scorer.")
                is ActionPathEvent.Unsupported -> add(
                    listOfNotNull(event.rollType?.description, event.reason).joinToString(": "),
                )
                is ActionPathEvent.PhysicalD6 -> {
                    if (!event.finalized) add("Physical D6 event $index was not finalized.")
                    if (event.canUseHypotheticalRecovery && event.observedSuccess == null) {
                        add("Physical D6 event $index is missing its observed branch.")
                    }
                    event.actualRecovery?.let { use -> validateResource(use.resource, event, index) }
                }
                is ActionPathEvent.Block -> Unit
            }
            event.recoveries.forEach { option ->
                validateResource(option.resource, event, index)
                if (option.activationFailure != ActivationFailureBehavior.STOP) {
                    add("Recovery ${option.resource.id.id} has unsupported activation-failure behavior.")
                }
            }
        }
    }.distinct()

    /** Adds validation errors for a recovery resource attached to an event. */
    private fun MutableList<String>.validateResource(
        resource: RecoveryResource,
        event: ActionPathEvent,
        index: Int,
    ) {
        if (resource.usage == RerollUsage.UNSUPPORTED) {
            add("Recovery ${resource.id.id} has an unsupported lifetime.")
        }
        if (event.owner != resource.owner) {
            add("Recovery ${resource.id.id} is owned by a different team than event $index.")
        }
        if (usageScope(resource, event.scope) == null && resource.usage != RerollUsage.REUSABLE) {
            add("Recovery ${resource.id.id} is missing its ${resource.usage} scope.")
        }
    }

    /** Builds a result explaining why the action path could not be scored. */
    private fun unsupported(events: List<ActionPathEvent>, reasons: List<String>) =
        ProbabilityScoreResult.Unsupported(ALGORITHM_ID, POLICY_ID, events, reasons)

    private data class RelevantResourceState(
        val consumedResources: Set<String> = emptySet(),
        val teamRerollTurns: Set<String> = emptySet(),
    )

    /** Computes which consumed-resource keys can still affect each event position. */
    private fun relevantResourceStates(
        events: List<ActionPathEvent>,
        allowMultipleTeamRerollsPerTurn: Boolean,
    ): List<RelevantResourceState> {
        val result = MutableList(events.size + 1) { RelevantResourceState() }
        for (index in events.indices.reversed()) {
            val event = events[index]
            val next = result[index + 1]
            val resources = buildList {
                addAll(event.recoveries.map { it.resource })
                if (event is ActionPathEvent.PhysicalD6) event.actualRecovery?.resource?.let(::add)
            }
            result[index] = RelevantResourceState(
                consumedResources = next.consumedResources + resources.mapNotNull { usageScope(it, event.scope) },
                teamRerollTurns = next.teamRerollTurns + when (allowMultipleTeamRerollsPerTurn) {
                    true -> emptyList()
                    false -> resources
                        .filter { it.category == RerollCategory.TEAM_REROLL }
                        .mapNotNull { resource -> event.scope?.let { teamTurnScope(resource, it) } }
                },
            )
        }
        return result
    }

    private data class ResourceState(
        val consumedResources: Set<String> = emptySet(),
        val teamRerollTurns: Set<String> = emptySet(),
    ) {
        /** Returns whether [resource] may be consumed for [event] in this state. */
        fun isAvailable(
            resource: RecoveryResource,
            event: ActionPathEvent,
            allowMultipleTeamRerollsPerTurn: Boolean,
        ): Boolean {
            val usage = usageScope(resource, event.scope)
            if (usage != null && usage in consumedResources) return false
            if (resource.category == RerollCategory.TEAM_REROLL && !allowMultipleTeamRerollsPerTurn) {
                val turn = teamTurnScope(resource, event.scope ?: return false)
                if (turn in teamRerollTurns) return false
            }
            return true
        }

        /** Returns the state after consuming [resource] for [event]. */
        fun consume(
            resource: RecoveryResource,
            event: ActionPathEvent,
            trackTeamTurn: Boolean,
        ): ResourceState {
            val usage = usageScope(resource, event.scope)
            val usedTeamTurn = if (resource.category == RerollCategory.TEAM_REROLL && trackTeamTurn) {
                event.scope?.let { teamTurnScope(resource, it) }
            } else {
                null
            }
            return copy(
                consumedResources = usage?.let { consumedResources + it } ?: consumedResources,
                teamRerollTurns = usedTeamTurn?.let { teamRerollTurns + it } ?: teamRerollTurns,
            )
        }

        /** Drops state entries that cannot affect any remaining event. */
        fun retain(relevant: RelevantResourceState): ResourceState = ResourceState(
            consumedResources.intersect(relevant.consumedResources),
            teamRerollTurns.intersect(relevant.teamRerollTurns),
        )
    }

    /** Adds probability to a resource state while merging duplicate states. */
    private fun MutableMap<ResourceState, Probability>.add(state: ResourceState, probability: Probability) {
        if (probability == Probability.NEVER) return
        this[state] = (this[state] ?: Probability.NEVER) + probability
    }

    /** Resolves a resource's lifetime into the key used by the DP state. */
    private fun usageScope(resource: RecoveryResource, scope: ActionPathEventScope?): String? {
        val resourceKey = "${resource.owner.value}:${resource.id.id}"
        return when (resource.usage) {
            RerollUsage.REUSABLE -> null
            RerollUsage.ONCE_PER_ACTION -> scope?.player?.let { "$resourceKey@action:$it" }
            RerollUsage.ONCE_PER_ACTIVATION -> scope?.player?.let { "$resourceKey@activation:$it" }
            RerollUsage.ONCE_PER_TURN -> scope?.turn?.let { "$resourceKey@turn:$it" }
            RerollUsage.ONCE_PER_DRIVE -> scope?.drive?.let { "$resourceKey@drive:$it" }
            RerollUsage.ONCE_PER_HALF -> scope?.half?.let { "$resourceKey@half:$it" }
            RerollUsage.ONCE_PER_GAME -> "$resourceKey@game"
            RerollUsage.UNSUPPORTED -> null
        }
    }

    /** Returns the key used to enforce one team reroll per team turn. */
    private fun teamTurnScope(resource: RecoveryResource, scope: ActionPathEventScope): String =
        "${resource.owner.value}:${scope.turn}"
}
