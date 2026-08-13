package com.jervisffb.engine.bb2025.challenge.rule

import com.jervisffb.engine.GameDelta
import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.challenge.ChallengeContext
import com.jervisffb.engine.challenge.ChallengeContextHolder
import com.jervisffb.engine.challenge.ChallengeRule
import com.jervisffb.engine.challenge.RuleProgress
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.common.planner.MoveCandidate
import com.jervisffb.engine.rules.common.planner.MovePolicy
import com.jervisffb.engine.rules.common.planner.MovePolicyContext
import com.jervisffb.engine.rules.policy.GameRulePolicy

/**
 * Challenge rule that specifies if certain move types are allowed or not.
 * See `false` for the types that should not be available during the challenge.
 */
data class MoveTypesAvailable(
    val dodge: Boolean = true,
    val rush: Boolean = true,
    val jump: Boolean = true,
) : ChallengeRule, MovePolicy {

    override val description: String = buildString {
        val disallowedTypes = buildList {
            if (!dodge) add("Dodging")
            if (!rush) add("Rushing")
            if (!jump) add("Jumping")
        }
        if (disallowedTypes.isEmpty()) {
            append("All move types are allowed.")
            return@buildString
        }

        append(
            when (disallowedTypes.size) {
                1 -> disallowedTypes.single()
                2 -> disallowedTypes.joinToString(" and ")
                else -> "${disallowedTypes.dropLast(1).joinToString(", ")}, and ${disallowedTypes.last()}"
            },
        )
        when (disallowedTypes.size) {
            1 -> append(" is not allowed.")
            else -> append(" are not allowed.")
        }
    }

    override val policies: List<GameRulePolicy> = listOf(this)

    override fun allowsMoveType(context: MovePolicyContext, type: MoveType): Boolean {
        return jump || type != MoveType.JUMP
    }

    override fun allowsMove(context: MovePolicyContext, candidate: MoveCandidate): Boolean {
        return (dodge || !candidate.target.requiresDodge) && (rush || !candidate.target.requiresRush)
    }

    override fun applyToGame(state: Game) {
        // Do nothing
    }

    override fun initialize(state: Game): ChallengeContext? = null

    override fun evaluate(
        state: Game,
        delta: GameDelta,
        contexts: ChallengeContextHolder
    ): RuleProgress {
        // This rule is using the action policy to filter out certain move types.
        // Getting here, everything is allowed.
        return RuleProgress(false, null)
    }
}
