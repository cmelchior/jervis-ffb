package com.jervisffb.engine.statistics.probability.observation

import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.statistics.probability.event.ActionPathEvent
import com.jervisffb.engine.statistics.probability.normalizer.ChanceNormalizer
import kotlinx.serialization.Serializable

/** Policy-neutral facts emitted by the rules engine for consumers of chance data. */
@Serializable
sealed interface ChanceObservation {
    /** Stable identifier and chronological position in the chance data stream. */
    val index: Int

    /** One physical roll of one or more dice. */
    @Serializable
    data class DiceRoll(
        override val index: Int,
        val rollType: DiceRollType,
        val teamId: TeamId,
        val playerId: PlayerId? = null,
        val dice: List<ChanceDieResult>,
        val scope: ChanceObservationScope,
        /** The roll whose reroll flow caused this nested roll, such as Pro or Loner. */
        val enclosingRollId: Int? = null,
        /** The exact physical roll replaced by this roll. */
        val rerolledRollId: Int? = null,
        val selectedResultIds: List<ChanceResultId> = emptyList(),
        val selectedBy: TeamId? = null,
        val success: Boolean? = null,
        val rerollOptions: List<ChanceRerollOption> = emptyList(),
        val selectedReroll: ChanceRerollSelection? = null,
        val finalized: Boolean = false,
    ) : ChanceObservation {
        init {
            require(dice.isNotEmpty()) { "A dice-roll observation must contain at least one die." }
            require(dice.all { it.id.rollId == index }) {
                "Every result ID must refer to its containing roll $index."
            }
            require(enclosingRollId == null || enclosingRollId < index) {
                "An enclosing roll must precede its nested roll: $enclosingRollId >= $index"
            }
            require(rerolledRollId == null || rerolledRollId < index) {
                "A rerolled roll must precede its replacement: $rerolledRollId >= $index"
            }
        }

        val isReroll: Boolean
            get() = rerolledRollId != null
    }

    /**
     * A chance action for which no structured roll data exists, i.e., we cannot
     * say anything useful about it except the value being rolled.
     *
     * [ChanceNormalizer] will normally treat this as [ActionPathEvent.Unsupported]
     * */
    @Serializable
    data class UnstructuredAction(
        override val index: Int,
        val nodeDescription: String,
        val actionName: String,
        val dice: List<DieResult> = emptyList(),
    ) : ChanceObservation
}
