package com.jervisffb.engine.bb2025.challenge.modifier

import com.jervisffb.engine.GameDelta
import com.jervisffb.engine.challenge.ChallengeContext
import com.jervisffb.engine.challenge.ChallengeContextHolder
import com.jervisffb.engine.challenge.GoalModifier
import com.jervisffb.engine.challenge.GoalStatus
import com.jervisffb.engine.challenge.ModifierProgress
import com.jervisffb.engine.common.context.BlockContext
import com.jervisffb.engine.common.context.FoulContext
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.getContextOrNull

/**
 * Goal modifier setting a lower bound on how defensive assists the target has
 * to have. This applies to both Fouls and Blocks.
 *
 * This modifier does not fail if fewer assists are used as more might come in
 * the future.
 */
data class DefensiveAssistsRequired(val count: Int) : GoalModifier {
    init {
        require(count >= 0) { "Defensive assists cannot be negative: $count" }
    }

    override val description: String = "against $count defensive assists"
    override fun initialize(state: Game): ChallengeContext? = null
    override fun evaluate(
        state: Game,
        delta: GameDelta,
        contexts: ChallengeContextHolder
    ): ModifierProgress {
        val blockContext = state.getContextOrNull<BlockContext>()
        val foulContext = state.getContextOrNull<FoulContext>()
        val requiredAssistsProvided = when {
            blockContext != null -> blockContext.defensiveAssists >= count
            foulContext != null -> foulContext.defensiveAssists >= count
            else -> null
        }

        val status = when (requiredAssistsProvided) {
            true -> GoalStatus.COMPLETED
            false -> GoalStatus.IN_PROGRESS // Should this be failed?
            null -> GoalStatus.IN_PROGRESS
        }

        return ModifierProgress(status, null)
    }
}
