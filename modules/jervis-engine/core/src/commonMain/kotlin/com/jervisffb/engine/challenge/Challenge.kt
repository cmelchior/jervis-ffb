package com.jervisffb.engine.challenge

import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.model.ChallengeId
import com.jervisffb.engine.model.Coach
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.serialization.SerializedTeam
import com.jervisffb.engine.statistics.GameStatistics

/**
 * This class describes a single challenge.
 *
 * A challenge is a starting position plus three things the coach has to work
 * with: a [goal] to reach, [rules] restricting how they may reach it, and a
 * [scoring] method deciding how their solution ranks against everyone else's.
 *
 * Build one with [ChallengeBuilder] rather than calling this constructor.
 *
 * [setup] is used to construct the starting game state.
 */
data class Challenge(
    val id: ChallengeId,
    // Should increment whenever there is a "breaking" change to the challenge,
    // i.e., change to rules, teams or setup
    val version: Int,
    val name: String,
    val author: Coach,
    val description: String,
    val category: ChallengeCategory,
    val scoring: ChallengeScoring<*>,
    val goal: ChallengeGoal,
    val rules: List<ChallengeRule>,
    val gameRules: Rules,
    // The teams as they were authored, for the builder DSL and for showing the
    // challenge on the details page.
    //
    // These must never be handed to a `Game`, as they must not be modified.
    // They are only here to make it easier to access team data in the game UI.
    val homeTeam: Team,
    val awayTeam: Team,
    // Snapshots of the teams before the challenge started.
    // `createGame` rebuilds from these so every attempt starts clean.
    val initialHomeTeamState: SerializedTeam,
    val initialAwayTeamState: SerializedTeam,
    val setup: List<GameAction>,
) {
    /** Builds a fresh game for this challenge. */
    fun createGame(): GameEngineController {
        val game = Game(
            gameRules,
            SerializedTeam.deserialize(gameRules, initialHomeTeamState, homeTeam.coach),
            SerializedTeam.deserialize(gameRules, initialAwayTeamState, awayTeam.coach),
        )
        return GameEngineController(
            state = game,
            initialActions = setup,
            validateActions = true,
            protectInitialActions = true, // Actions provided by challenge authors cannot be modified.
            statistics = if (scoring is ChallengeScoring.ProbabilityScoring) GameStatistics() else null,
            onStarted = { controller ->
                rules.forEach { rule -> rule.applyToGame(controller.state) }
            }
        )
    }
}
