package com.jervisffb.engine.bb2025.challenge.rule

import com.jervisffb.engine.GameDelta
import com.jervisffb.engine.challenge.ChallengeContext
import com.jervisffb.engine.challenge.ChallengeContextHolder
import com.jervisffb.engine.challenge.ChallengeRule
import com.jervisffb.engine.challenge.RuleProgress
import com.jervisffb.engine.common.commands.SetHalf
import com.jervisffb.engine.common.commands.SetTurnMarker
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team

data class TurnLimitContext(
    val team: Team,
    val startingTurn: Int
): ChallengeContext

/**
 * Challenge rule that specifies how many turns the starting team has to reach
 * the goal. The starting turn is included in this count.
 *
 * Reaching end-of-half or end-of-game will always trigger a failure regardless
 * of more turns being available.
 * */
data class TurnLimit(val turns: Int) : ChallengeRule {
    init {
        require(turns >= 1) { "A challenge needs at least one turn: $turns" }
    }

    override val description: String = when (turns) {
        1 -> "You have 1 turn to solve it."
        else -> "You have $turns turns to solve it."
    }

    override val isMultipleAllowed: Boolean = false

    override fun applyToGame(state: Game) {
        // Do nothing
    }

    override fun initialize(state: Game): ChallengeContext {
        return TurnLimitContext(
            team = state.activeTeam ?: error("No active team"),
            startingTurn = state.activeTeam?.turnMarker ?: 0
        )
    }

    override fun evaluate(
        state: Game,
        delta: GameDelta,
        contexts: ChallengeContextHolder
    ): RuleProgress {
        val context = contexts.get<TurnLimitContext>()
        var ruleBroken = false
        for (command in delta.allCommands()) {
            when (command) {
                is SetHalf -> {
                    // Going to a new half will always violate this rule
                    ruleBroken = true
                    break
                }
                is SetTurnMarker -> {
                    val isTurnLimitReached = (command.nextTurn >= (context.startingTurn + turns - 1))
                    ruleBroken = isTurnLimitReached
                    break
                }
                else -> { /* Do nothing */ }
            }
        }

        return RuleProgress(
            ruleBroken = ruleBroken,
            updatedContext = context
        )
    }
}
