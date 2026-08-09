package com.jervisffb.engine.statistics.probability.event

import kotlinx.serialization.Serializable

/**
 * A reroll option that can be selected for a particular [ActionPathEvent].
 *
 * [appliesTo] is expressed relative to the demonstrated branch. This matters
 * for skills such as Catch, which can reroll a failed roll but not a successful
 * one. The activation chance belongs to the edge rather than the resource: a
 * team reroll can be guaranteed for one player and Loner-gated for another.
 */
@Serializable
data class RerollOption(
    val resource: RerollResource,
    val activation: OutcomeRatio = OutcomeRatio.CERTAIN,
    val appliesTo: Set<ChanceBranch>,
    val activationFailure: ActivationFailureBehavior = ActivationFailureBehavior.STOP,
)
