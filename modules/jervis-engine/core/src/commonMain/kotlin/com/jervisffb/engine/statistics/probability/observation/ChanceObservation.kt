package com.jervisffb.engine.statistics.probability.observation

import com.jervisffb.engine.GameDelta
import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.statistics.probability.ProbabilityTracker
import com.jervisffb.engine.statistics.probability.event.ActionPathEvent
import com.jervisffb.engine.statistics.probability.normalizer.ChanceNormalizer
import com.jervisffb.engine.statistics.probability.scorer.ActionPathScorer
import kotlinx.serialization.Serializable

/**
 * Policy-neutral facts about a chance event, like a dice roll or coin toss.
 * Emitted by the rules engine in [GameDelta.chanceObservations].
 *
 * And consumed by [ProbabilityTracker], [ChanceNormalizer] and
 * [ActionPathScorer].
 */
@Serializable
sealed interface ChanceObservation {
    /** Stable identifier and chronological position in the chance data stream. */
    val index: Int

    /** One physical roll of one or more dice. */
    @Serializable
    data class DiceRoll(
        override val index: Int,
        // Which type of dice were rolled.
        val rollType: DiceRollType,
        // Which team was rolling the dice.
        val teamId: TeamId,
        // Which player, if any, was rolling the dice.
        val playerId: PlayerId? = null,
        // The physical results in this roll. Result IDs are unique within the observation stream;
        // the optional logical DieId can occur again when that die is rerolled.
        val dice: List<ChanceDieResult>,
        // At what scope was the roll done? This impacts how the scorer interprets
        // rerolls on it, when trying to find an optimal reroll strategy.
        val scope: ChanceObservationScope,
        // The roll whose reroll flow caused this nested roll, such as Pro or Loner.
        val enclosingRollIndex: Int? = null,
        // The exact physical roll replaced by this roll.
        val rerolledRollIndex: Int? = null,
        val selectedResultIds: List<ChanceResultId> = emptyList(),
        val selectedBy: TeamId? = null,
        // Factual result when resolved; null while unresolved or when the roll has no binary interpretation.
        // Success here just means "rolled the intended result", not the outcome of the action.
        val success: Boolean? = null,
        // How the demonstrated action path evaluates this roll, when known.
        val outcome: ChanceOutcome? = null,
        val rerollOptions: List<ChanceRerollOption> = emptyList(),
        val selectedReroll: ChanceRerollSelection? = null,
        // If the final outcome of the roll is known. I.e., all rerolls are done, and a result has been selected.
        val finalized: Boolean = false,
    ) : ChanceObservation {
        init {
            require(dice.isNotEmpty()) { "A dice-roll observation must contain at least one die." }
            require(dice.all { it.id.rollIndex == index }) {
                "Every result ID must refer to its containing roll $index."
            }
            require(enclosingRollIndex == null || enclosingRollIndex < index) {
                "An enclosing roll must precede its nested roll: $enclosingRollIndex >= $index"
            }
            require(rerolledRollIndex == null || rerolledRollIndex < index) {
                "A rerolled roll must precede its replacement: $rerolledRollIndex >= $index"
            }
        }
    }

    /**
     * A chance action for which no structured roll data exists, i.e., we cannot
     * say anything useful about it except the value being rolled.
     *
     * [ChanceNormalizer] will normally treat this as
     * [ActionPathEvent.Unsupported].
     * */
    @Serializable
    data class UnstructuredAction(
        override val index: Int,
        val nodeDescription: String,
        val actionName: String,
        val dice: List<DieResult> = emptyList(),
    ) : ChanceObservation
}
