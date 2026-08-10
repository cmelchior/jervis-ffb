package com.jervisffb.engine.statistics.probability.scorer

import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.statistics.probability.AlgorithmId
import com.jervisffb.engine.statistics.probability.event.ActionPathEvent
import com.jervisffb.engine.statistics.probability.normalizer.ChanceNormalizer
import com.jervisffb.engine.statistics.probability.observation.ChanceObservation

/**
 * Converts a list of [ActionPathEvent] into a probability score.
 */
interface ActionPathScorer {

    companion object {
        const val DEFAULT_STATE_CEILING: Int = 100_000
    }

    // These identify the combination of policies used to create a score.
    // Scores with different combinations of policies are not comparable.
    val algorithmId: AlgorithmId
    val rerollUsagePolicy: RerollUsagePolicy

    // Normalizer used to convert chance observations into action path events.
    val normalizer: ChanceNormalizer

    /**
     * Normalizes observations and scores the resulting action path.
     */
    fun score(
        rules: Rules,
        observations: List<ChanceObservation>,
        solvingTeamId: TeamId,
        stateCeiling: Int = DEFAULT_STATE_CEILING,
    ): ProbabilityScoreResult = scoreNormalized(
        rules = rules,
        events = normalizer.normalize(observations),
        solvingTeamId = solvingTeamId,
        stateCeiling = stateCeiling,
    )

    /**
     *  Scores an already normalized action path without repeating normalization.
     */
    fun scoreNormalized(
        rules: Rules,
        events: List<ActionPathEvent>,
        solvingTeamId: TeamId,
        stateCeiling: Int = DEFAULT_STATE_CEILING,
    ): ProbabilityScoreResult

}
