package com.jervisffb.engine.challenge

import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.model.ChallengeScoringId
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.statistics.GameStatistics
import com.jervisffb.engine.statistics.probability.scorer.LogicalActionPathScorer
import com.jervisffb.engine.statistics.probability.scorer.PhysicalActionPathScorer
import kotlin.time.Clock

/**
 * How solutions to a [Challenge] are ranked against each other.
 */
sealed interface ChallengeScoring<T: ChallengeScore<T>> {
    // Identifier for this scoring method
    val id: ChallengeScoringId
    // Shown on the challenge details page so coaches know what they compete on.
    val description: String

    // Returns the score for a given game
    fun scoreGame(
        state: Game,
        actions: List<GameAction>,
        statistics: GameStatistics? = null
    ): ChallengeScore<T>

    /** A challenge with no ranking, we only count completion or not. This also mean that
     * different attempts cannot be ranked
     */
    data object CompletionOnly : ChallengeScoring<ChallengeScore.CompletionOnly> {
        override val id = ChallengeScoringId("completion-only")
        override val description: String = "Solutions are not ranked"

        override fun scoreGame(
            state: Game,
            actions: List<GameAction>,
            statistics: GameStatistics?,
        ): ChallengeScore<ChallengeScore.CompletionOnly> {
            return ChallengeScore.CompletionOnly(Clock.System.now())
        }
    }

    /**
     * Solutions are ranked by their probability of success, higher is better.
     *
     * Since calculating the probability is not straight-forward and require
     * trade-offs. The exact policy for doing this is determined by [policy]
     *
     * Scores are only comparable if they use the same policy.
     */
    data class ProbabilityScoring(
        val solvingTeamId: TeamId,
        val policy: ChallengeRerollSelectionPolicy = ChallengeRerollSelectionPolicy.PhysicalRerollSelection(),
    ) : ChallengeScoring<ChallengeScore.ProbabilityScore> {
        override val id = ChallengeScoringId("jervis-probability-score")
        override val description: String = "Jervis Probability Score: An approximation of the probability of success. Higher is better."

        override fun scoreGame(
            state: Game,
            actions: List<GameAction>,
            statistics: GameStatistics?,
        ): ChallengeScore.ProbabilityScore {
            val events = statistics?.diceProbabilities?.observations ?: error("Missing statistics")
            val result = when (val selectedPolicy = policy) {
                is ChallengeRerollSelectionPolicy.LogicalRerollSelection -> LogicalActionPathScorer.score(
                    rules = state.rules,
                    observations = events.toList(),
                    solvingTeamId = solvingTeamId,
                    stateCeiling = selectedPolicy.stateCeiling,
                )
                is ChallengeRerollSelectionPolicy.PhysicalRerollSelection -> PhysicalActionPathScorer.score(
                    rules = state.rules,
                    observations = events.toList(),
                    solvingTeamId = solvingTeamId,
                    stateCeiling = selectedPolicy.stateCeiling,
                )
            }
            return ChallengeScore.ProbabilityScore(Clock.System.now(), result)
        }
    }
}
