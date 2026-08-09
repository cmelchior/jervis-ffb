package com.jervisffb.engine.statistics.probability.scorer

import com.jervisffb.engine.statistics.probability.Probability
import com.jervisffb.engine.statistics.probability.RerollUsagePolicyId
import com.jervisffb.engine.statistics.probability.event.ActivationFailureBehavior
import com.jervisffb.engine.statistics.probability.event.RerollCategory
import com.jervisffb.engine.statistics.probability.event.RerollOption
import com.jervisffb.engine.statistics.probability.event.RerollUsage

/**
 * This reroll policy is used by [LogicalActionPathScorer].
 *
 * The policy automatically selects one reroll attempt, based on a pre-defined
 * order of importance.
 *
 * The priority list is:
 *
 * 1. Skill rerolls. Preferring re-usable skills, e.g., if a player has both
 *    Dodge and Pro, Dodge is selected if available.
 * 2. Team rerolls: Using the same priority as skills. TODO This feels wrong, we should prioritize short-lived team rerolls.
 * 3. If a player has both Pro and Loner, the activation value is compared. If
 *    equal, Pro is preferred.
 *
 * Example 1: Pro (3+) and Loner (3+). Pro is used.
 * Example 2: Pro (4+) and Loner (3+). Loner is used.
 *
 * Important: Rerolls with activation rolls like Pro or Loner Team Rerolls never
 * use team rerolls in an attempt to recover if the activation roll fails.
 */
object PriorityListRerollUsagePolicy: RerollUsagePolicy {

    override val id = RerollUsagePolicyId("fixed-reroll-priority-list-v1")

    override fun select(options: List<RerollOption>): RerollOption? {
        if (options.isEmpty()) return null

        options.firstOrNull { it.activationFailure == ActivationFailureBehavior.UNSUPPORTED }?.let {
            return it
        }

        // A normal skill reroll always has first refusal. Prefer the
        // shortest-lived deterministic resource, then use its stable ID.
        options
            .asSequence()
            .filter { it.resource.category == RerollCategory.STANDARD_SKILL }
            .sortedWith(compareBy<RerollOption>({ usagePriority(it.resource.usage) }, { it.resource.id.id }))
            .firstOrNull()
            ?.let { return it }

        // A team reroll without Loner is better than risking Pro's activation.
        val teamOptions = options
            .filter { it.resource.category == RerollCategory.TEAM_REROLL }
            .sortedWith(compareBy({ usagePriority(it.resource.usage) }, { it.resource.id.id }))
        teamOptions.firstOrNull { it.activation.probability == Probability.ALWAYS }?.let { return it }

        val pro = options
            .filter { it.resource.category == RerollCategory.PRO }
            .minByOrNull { it.resource.id.id }
        val lonerTeamReroll = teamOptions.firstOrNull()

        return when {
            pro == null -> lonerTeamReroll
            lonerTeamReroll == null -> pro
            pro.activation.probability >= lonerTeamReroll.activation.probability -> pro
            else -> lonerTeamReroll
        }
    }

    private fun usagePriority(usage: RerollUsage): Int = when (usage) {
        RerollUsage.REUSABLE -> 0
        RerollUsage.ONCE_PER_ACTION -> 1
        RerollUsage.ONCE_PER_ACTIVATION -> 2
        RerollUsage.ONCE_PER_TURN -> 3
        RerollUsage.ONCE_PER_DRIVE -> 4
        RerollUsage.ONCE_PER_HALF -> 5
        RerollUsage.ONCE_PER_GAME -> 6
        RerollUsage.UNSUPPORTED -> 7
    }
}
