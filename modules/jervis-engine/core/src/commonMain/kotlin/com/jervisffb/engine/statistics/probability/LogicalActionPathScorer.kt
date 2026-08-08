package com.jervisffb.engine.statistics.probability

import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.utils.sum

/**
 * Evaluates a selected Action Path using [FixedRerollUsagePolicy]. This means
 * that if rerolls were selected during the action path, that reroll is ignored
 * when calculating the final score and instead only the final die result is
 * used.
 *
 * Instead, we use forward dynamic program to calculate the optimal use of
 * reroll types to maximize the chance of success. It does not replay alternate
 * game states.
 *
 * TODO I don't think we completely ignore it. Because the reroll option used
 *  will be missing from future rolls, which means it will be missing from the
 *  chance observation. Double-check this.
 */
object LogicalActionPathScorer {
    val ALGORITHM_ID = AlgorithmId("logical-path-bb2025-v1")
    const val DEFAULT_STATE_CEILING: Int = 100_000

    /** Normalizes observations and scores the resulting demonstrated action path. */
    fun score(
        observations: List<ChanceObservation>,
        solvingTeamId: TeamId,
        allowMultipleTeamRerollsPerTurn: Boolean,
        stateCeiling: Int = DEFAULT_STATE_CEILING,
    ): ProbabilityScoreResult = scoreNormalized(
        events = ChanceNormalizer.fixed(observations),
        solvingTeamId = solvingTeamId,
        allowMultipleTeamRerollsPerTurn = allowMultipleTeamRerollsPerTurn,
        stateCeiling = stateCeiling,
    )

    /** Scores an already normalized action path without repeating normalization. */
    internal fun scoreNormalized(
        events: List<ActionPathEvent>,
        solvingTeamId: TeamId,
        allowMultipleTeamRerollsPerTurn: Boolean,
        stateCeiling: Int = DEFAULT_STATE_CEILING,
    ): ProbabilityScoreResult {
        require(stateCeiling > 0) { "State ceiling must be positive: $stateCeiling" }

        val unsupportedReasons = validate(events)
        if (unsupportedReasons.isNotEmpty()) {
            return unsupported(events, unsupportedReasons)
        }

        val baseProbability = events.fold(Probability.ALWAYS) { probability, event ->
            probability * event.observedOutcomeProbability
        }
        if (baseProbability == Probability.NEVER) {
            return unsupported(events, listOf("The base line probability underflowed to zero."))
        }

        val relevantStates = relevantResourceStates(events, allowMultipleTeamRerollsPerTurn)
        var states: Map<ResourceState, Probability> = mapOf(ResourceState() to Probability.ALWAYS)
        events.forEachIndexed { eventIndex, event ->
            val next = mutableMapOf<ResourceState, Probability>()
            states.forEach { (resourceState, lineProbability) ->
                expandEvent(
                    event = event,
                    solvingTeamId = solvingTeamId,
                    allowMultipleTeamRerollsPerTurn = allowMultipleTeamRerollsPerTurn,
                    resourceState = resourceState,
                    lineProbability = lineProbability,
                    target = next,
                )
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

        val successProbability = states.values.sum().coerceIn(Probability.NEVER, Probability.ALWAYS)
        if (successProbability == Probability.NEVER) {
            return unsupported(events, listOf("The fixed-policy probability underflowed to zero."))
        }

        val baseRisk = baseProbability.toSurprisal()
        val finalRisk = successProbability.toSurprisal()
        return ProbabilityScoreResult.Scored(
            algorithmId = ALGORITHM_ID,
            policyId = FixedRerollUsagePolicy.POLICY_ID,
            events = events,
            baseProbability = baseProbability,
            demonstratedProbability = baseProbability,
            successProbability = successProbability,
            baseSurprisal = baseRisk,
            actualExtraRollAdjustment = SurprisalAdjustment.ZERO,
            hypotheticalRecoveryAdjustment = SurprisalAdjustment.from(baseRisk, finalRisk),
            rerollAdjustment = SurprisalAdjustment.from(baseRisk, finalRisk),
            surprisal = finalRisk,
        )
    }

    /** Expands one action-path event into its possible resource-state outcomes. */
    private fun expandEvent(
        event: ActionPathEvent,
        solvingTeamId: TeamId,
        allowMultipleTeamRerollsPerTurn: Boolean,
        resourceState: ResourceState,
        lineProbability: Probability,
        target: MutableMap<ResourceState, Probability>,
    ) {
        val observedProbability = event.observedOutcomeProbability
        val alternativeProbability = observedProbability.inverse()
        val ownerIsSolvingTeam = event.owner == solvingTeamId
        val recoveryBranch = when (ownerIsSolvingTeam) {
            true -> ChanceBranch.ALTERNATIVE
            false -> ChanceBranch.SELECTED
        }
        val recovery = FixedRerollUsagePolicy.select(
            event.recoveries.filter { option ->
                recoveryBranch in option.appliesTo &&
                    resourceState.isAvailable(option.resource, event, allowMultipleTeamRerollsPerTurn)
            },
        )

        if (ownerIsSolvingTeam) {
            // The demonstrated outcome needs no resource.
            target.add(resourceState, lineProbability * observedProbability)
            if (recovery != null) {
                val consumed = resourceState.consume(
                    recovery.resource,
                    event,
                    trackTeamTurn = !allowMultipleTeamRerollsPerTurn,
                )
                // STOP means an activation failure retains the line-breaking
                // original result, so only an activated successful reroll lives.
                val recovered = alternativeProbability *
                    recovery.activation.probability *
                    observedProbability
                target.add(consumed, lineProbability * recovered)
            }
        } else {
            // The opponent accepts the alternative outcome, which breaks the
            // submitted line. Only the demonstrated branch can survive.
            if (recovery == null) {
                target.add(resourceState, lineProbability * observedProbability)
            } else {
                val consumed = resourceState.consume(
                    recovery.resource,
                    event,
                    trackTeamTurn = !allowMultipleTeamRerollsPerTurn,
                )
                val activation = recovery.activation.probability
                // Failed activation retains the demonstrated result. When the
                // recovery activates, only another demonstrated result survives.
                val survivesRecovery = (Probability.ALWAYS - activation) + activation * observedProbability
                target.add(consumed, lineProbability * observedProbability * survivesRecovery)
            }
        }
    }

    /** Returns validation errors that prevent this scorer from evaluating [events]. */
    private fun validate(events: List<ActionPathEvent>): List<String> = buildList {
        events.forEachIndexed { index, event ->
            if (event.index != index) {
                add("Expected event sequence $index but found ${event.index}.")
            }
            if (event is ActionPathEvent.Unsupported) {
                add(listOfNotNull(event.rollType?.description, event.reason).joinToString(": "))
            }
            if (event is ActionPathEvent.PhysicalD6) {
                add("Physical D6 event $index belongs to the hybrid actual-choice scorer.")
            }
            event.recoveries.forEach { option ->
                if (option.resource.usage == RerollUsage.UNSUPPORTED) {
                    add("Recovery ${option.resource.id.id} has an unsupported lifetime.")
                }
                if (option.activationFailure != ActivationFailureBehavior.STOP) {
                    add("Recovery ${option.resource.id.id} has unsupported activation-failure behavior.")
                }
                if (event.owner != option.resource.owner) {
                    add("Recovery ${option.resource.id.id} is owned by a different team than event $index.")
                }
                if (usageScope(option.resource, event.scope) == null &&
                    option.resource.usage != RerollUsage.REUSABLE
                ) {
                    add("Recovery ${option.resource.id.id} is missing its ${option.resource.usage} scope.")
                }
            }
        }
    }.distinct()

    /** Builds a result explaining why the action path could not be scored. */
    private fun unsupported(events: List<ActionPathEvent>, reasons: List<String>) =
        ProbabilityScoreResult.Unsupported(
            algorithmId = ALGORITHM_ID,
            policyId = FixedRerollUsagePolicy.POLICY_ID,
            events = events,
            reasons = reasons,
        )

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
            val resourceKeys = event.recoveries.mapNotNull { usageScope(it.resource, event.scope) }
            val teamTurns = when (allowMultipleTeamRerollsPerTurn) {
                true -> emptyList()
                false -> event.recoveries
                    .filter { it.resource.category == RerollCategory.TEAM_REROLL }
                    .mapNotNull { option -> event.scope?.let { teamTurnScope(option.resource, it) } }
            }
            result[index] = RelevantResourceState(
                consumedResources = next.consumedResources + resourceKeys,
                teamRerollTurns = next.teamRerollTurns + teamTurns,
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
                consumedResources = when (usage) {
                    null -> consumedResources
                    else -> consumedResources + usage
                },
                teamRerollTurns = when (usedTeamTurn) {
                    null -> teamRerollTurns
                    else -> teamRerollTurns + usedTeamTurn
                },
            )
        }

        /** Drops state entries that cannot affect any remaining event. */
        fun retain(relevant: RelevantResourceState): ResourceState = ResourceState(
            consumedResources = consumedResources.intersect(relevant.consumedResources),
            teamRerollTurns = teamRerollTurns.intersect(relevant.teamRerollTurns),
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
