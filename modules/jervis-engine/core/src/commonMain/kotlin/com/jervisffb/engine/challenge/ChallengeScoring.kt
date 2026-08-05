package com.jervisffb.engine.challenge

import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.challenge.probability.JervisRiskScore
import com.jervisffb.engine.model.ChallengeScoringId
import com.jervisffb.engine.model.Game
import kotlin.random.Random
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
    fun scoreGame(state: Game, actions: List<GameAction>): ChallengeScore<T>

    /** A challenge with no ranking, i.e., different attempts cannot be ranked */
    data object CompletionOnly : ChallengeScoring<ChallengeScore.CompletionOnly> {
        override val id = ChallengeScoringId("completion-only")
        override val description: String = "Solutions are not ranked"

        override fun scoreGame(
            state: Game,
            actions: List<GameAction>
        ): ChallengeScore<ChallengeScore.CompletionOnly> {
            return ChallengeScore.CompletionOnly(Clock.System.now())
        }
    }

    /**
     * Solutions are ranked by their Jervis Risk Score, lower is better.
     * See [JervisRiskScore] for more information.
     */
    data object JervisRiskScoring : ChallengeScoring<ChallengeScore.JervisRiskScore> {
        override val id = ChallengeScoringId("jervis-risk-score")
        override val description: String = "Jervis Risk Score: Lower is better"

        override fun scoreGame(
            state: Game,
            actions: List<GameAction>
        ): ChallengeScore<ChallengeScore.JervisRiskScore> {
            // TODO Implement proper score calculation
            val score = JervisRiskScore(
                baseRisk = Random.nextDouble(10.0),
                benefitBySource = emptyMap()
            )
            val now = Clock.System.now()
            return ChallengeScore.JervisRiskScore(now, score)
        }
    }
}
