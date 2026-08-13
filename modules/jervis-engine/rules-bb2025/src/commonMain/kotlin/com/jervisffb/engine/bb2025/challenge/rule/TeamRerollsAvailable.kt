package com.jervisffb.engine.bb2025.challenge.rule

import com.jervisffb.engine.GameDelta
import com.jervisffb.engine.bb2025.procedures.rerolls.StandardTeamReroll
import com.jervisffb.engine.challenge.ChallengeContext
import com.jervisffb.engine.challenge.ChallengeContextHolder
import com.jervisffb.engine.challenge.ChallengeRule
import com.jervisffb.engine.challenge.RuleProgress
import com.jervisffb.engine.model.Game

/**
 * Challenge rule that overrides how many team re-rolls the team starts with.
 *
 * For now, this rule removes all kinds of rerolls: Leader, Mascot, Inducements
 * and replaces them with standard rerolls. It is still unclear if this is the
 * best approach, so it might change in the future.
 */
data class TeamRerollsAvailable(val count: Int) : ChallengeRule {
    init {
        require(count >= 0) { "A team cannot have a negative number of re-rolls: $count" }
    }

    override val description: String = when (count) {
        0 -> "Team re-rolls are disabled."
        1 -> "1 team re-roll is available."
        else -> "$count team re-rolls are available."
    }

    override val isMultipleAllowed: Boolean = false

    override fun applyToGame(state: Game) {
        // Override team rerolls, so there is only the configured amount available
        // Remove team rerolls above the configured
        state.homeTeam.rerolls.apply {
            clear()
            repeat(count) { i ->
                add(StandardTeamReroll(state.homeTeam.id, i))
            }
        }
        state.awayTeam.rerolls.apply {
            clear()
            repeat(count) { i ->
                add(StandardTeamReroll(state.awayTeam.id, i))
            }
        }
    }

    override fun initialize(state: Game): ChallengeContext? = null

    override fun evaluate(
        state: Game,
        delta: GameDelta,
        contexts: ChallengeContextHolder
    ): RuleProgress {
        // A pure read of the state, so the tracker's undo (which drops the last
        // step) gets the right answer for free.
        val used = maxOf(
            state.homeTeam.rerolls.count { it.rerollUsed },
            state.awayTeam.rerolls.count { it.rerollUsed },
        )
        return RuleProgress(ruleBroken = used > count, updatedContext = null)
    }
}
