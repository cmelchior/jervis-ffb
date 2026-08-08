package com.jervisffb.engine.statistics.probability

import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.commands.probabiliy.AddChanceObservation
import com.jervisffb.engine.commands.probabiliy.UpdateChanceObservation
import com.jervisffb.engine.model.DieId
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.RerollSourceId
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.common.skills.Duration
import kotlinx.serialization.Serializable

/**
 * Marker for procedures with an element of chance.
 *
 * By using this interface, classes promise they will create a
 * [AddChanceObservation] or [UpdateChanceObservation] command for any chance
 * event they control. This allows the engine to track the chance events and
 * their outcomes.
 *
 * Procedures with chance that do not do this end up creating a
 * [ChanceObservation.UnstructuredAction] which will short-circuit any
 * probability calculations done on a chain of events where it is included.
 */
interface ChanceObservationHandler

/**
 * Identifies the game boundaries containing a random event.
 * TODO Do we strictly need this?
 */
@Serializable
data class ChanceObservationScope(
    val half: Int,
    val drive: Int,
    val team: TeamId,
    val turn: Int,
    val player: PlayerId? = null,
)

/** Identifies one result within one physical dice roll. */
@Serializable
data class ChanceResultId(
    val rollId: Int,
    val index: Int,
)

/** One physical die result, optionally linked to the engine's logical die. */
@Serializable
data class ChanceDieResult(
    val id: ChanceResultId,
    val result: DieResult,
    val dieId: DieId? = null,
)

/** The engine concept that supplied a reroll. This does not assign scoring priority. */
@Serializable
enum class ChanceRerollSourceKind {
    SKILL,
    TEAM_REROLL,
    OTHER,
}

/** The factual effect of a test performed while using a reroll source. */
@Serializable
enum class ChanceRerollTestEffect {
    ALLOWS_REROLL,
    RESTORES_SOURCE,
}

/** A test which can occur before or after a reroll source is used. */
@Serializable
data class ChanceRerollTest(
    val rollType: DiceRollType,
    val dieSides: Int,
    val successTarget: Int,
    val effect: ChanceRerollTestEffect,
)

/** A self-contained snapshot of a reroll source at the time of a roll. */
@Serializable
data class ChanceRerollSource(
    val id: RerollSourceId,
    val owner: TeamId,
    val kind: ChanceRerollSourceKind,
    val description: String,
    val resetAt: Duration,
    val skillId: SkillId? = null,
    val tests: List<ChanceRerollTest> = emptyList(),
)

/** One set of results a source can reroll and the branches where it applies. */
@Serializable
data class ChanceRerollOption(
    val source: ChanceRerollSource,
    val resultIds: List<ChanceResultId>,
    val appliesOnSuccess: Boolean,
    val appliesOnFailure: Boolean,
    val currentlyAvailable: Boolean,
)

/** What the coach selected and what the rules ultimately allowed. */
@Serializable
data class ChanceRerollSelection(
    val sourceId: RerollSourceId,
    val resultIds: List<ChanceResultId>,
    val allowed: Boolean? = null,
    val aborted: Boolean = false,
)

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
