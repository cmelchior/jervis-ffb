package com.jervisffb.engine.statistics.probability

import com.jervisffb.engine.actions.BlockDice
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.RerollSourceId
import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.rules.DiceRollType
import kotlinx.serialization.Serializable

/**
 * This file contains classes related to tracking dice rolls and their
 * reroll choices across a single, chosen "Action Path".
 *
 * I.e., a user will select the game actions performed, and these classes
 * track rolls and rerolls that were encountered during that path. This can be
 * used to calculate the total probability of success for a given path, while
 * taking account into account available reroll possibilities.
 *
 * Note; it is assumed that the action path under consideration is successful,
 * for the definition of success needed in the context. These events provide no
 * useful information if a path is unsuccessful.
 *
 * The primary use case is challenges, where the Action Path is the actions
 * selected by the coach to complete the challenge.
 *
 * This also means that these classes are only usable for Action Paths that, at
 * most, span 1 team turn.
 */

/**
 * These classes describe a chance event recorded while the action path is
 * played.
 *
 * They are created by the [ChanceNormalizer] will run through the list of
 * [ChanceObservation]'s provided by the game engine and normalize them
 * into a list of [ActionPathEvent] that can be processed by one of the
 * `<X>Scorer` algorithms to provide a single score for the action path.
 */
@Serializable
sealed class ActionPathEvent {
    // Index of this event in the list of events recorded.
    // Once placed, an event never moves, so storing it here is fine and makes
    // it easier to find its position in the list.
    abstract val index: Int

    /**
     * A D6 test for a single roll, collapsing any reroll into it.
     *
     * A binary D6 test scored from the dice value selected.
     * I.e., if a value `v` is selected, the probability of success is
     * `(7-v)/6`.
     *
     * Example, if a 2 is selected, the chance of success i `5/6`.
     *
     * Note; this event makes no attempt to be "correct", it assumes that the
     * user will select the optimal value by taking account into account
     * any skills or modifiers that might apply for the result to turn into
     * a success. This has the advantage that an optimal score requires
     * knowledge about stats and skills and thus prompts users towards learning
     * these things.
     *
     * A suboptimal choice is still allowed, but will just result in a lower
     * score.
     *
     * Since these probabilities are mostly used during challenges, we know
     * that a correct value was chosen, albeit it might not be perfect.
     *
     * Example:
     * -A Dodge test starts with a D6 result of 1.
     * - The player uses Dodge and rolls again, getting 5.
     * - These are two physical dice, but one logical Dodge roll. [D6]
     *   represents that single test using its final selected result.
     */
    @Serializable
    data class D6(
        override val index: Int,
        val rollType: DiceRollType,
        val owner: TeamId,
        val selectedValue: Int,
        val observedSuccess: Boolean,
        val scope: ActionPathEventScope,
        val recoveries: List<RerollOption> = emptyList(),
    ) : ActionPathEvent() {
        init {
            val legalValues = if (observedSuccess) 2..6 else 1..5
            require(selectedValue in legalValues) {
                "A selected D6 ${if (observedSuccess) "success" else "failure"} must be in $legalValues: " +
                    selectedValue
            }
        }

        val observedOutcome: OutcomeRatio
            get() = when (observedSuccess) {
                true -> OutcomeRatio(7 - selectedValue, 6)
                false -> OutcomeRatio(selectedValue, 6)
            }
    }

    /**
     * One "physical" D6 selected by the coach.
     *
     * [PhysicalActionPathScorer] deliberately maps every selected value
     * `v` as `success`, even when the game engine treated that die as a failed
     * test.
     *
     * This approach allows us to keep the game UI largely the same (by showing
     * reroll options), but using them will penalize the final score.
     *
     * A natural 1 is a zero-risk event, but it remains visible in the
     * ledger and triggers a reroll source to be marked as used, which can
     * negatively affect the success chance of future rolls.
     *
     * This is by design as coaches can see the impact of using reroll options
     * in their final probability score.
     */
    @Serializable
    data class PhysicalD6(
        override val index: Int,
        // Index of the root event, e.g. a Dodge Reroll will point back to the first roll
        val traceRootSequence: Int,
        val rollType: DiceRollType,
        val owner: TeamId,
        val selectedValue: D6Result,
        val role: PhysicalD6Role,
        val scope: ActionPathEventScope,
        val observedSuccess: Boolean? = null,
        val actualRecovery: ActualRecoveryUse? = null,
        val recoveries: List<RerollOption> = emptyList(),
        val finalized: Boolean = false,
    ) : ActionPathEvent() {
        init {
            require(traceRootSequence <= index) {
                "A physical D6 cannot precede its trace root: $traceRootSequence > $index"
            }
        }

        val observedOutcome: OutcomeRatio
            get() {
                val sides = selectedValue.max.toInt()
                return OutcomeRatio((sides + 1) - selectedValue.value, sides)
            }

        // Returns `true` if a reroll was theoretically possible (regardless of it existing or not)
        val canUseHypotheticalRecovery: Boolean
            get() = finalized && role != PhysicalD6Role.REROLL && actualRecovery == null
    }

    /**
     * A standard block scored from the face selected in the chosen action path.
     *
     * TODO Rerolling Blocks are not supported right now.
     */
    @Serializable
    data class Block(
        override val index: Int,
        val owner: TeamId,
        val selectedFace: BlockDice,
        val diceCount: Int,
        val opponentChooses: Boolean,
        val scope: ActionPathEventScope,
        val recoveries: List<RerollOption> = emptyList(),
    ) : ActionPathEvent() {
        init {
            require(diceCount > 0) { "A block must roll at least one die: $diceCount" }
        }

        val observedOutcomeProbability: Probability
            get() = DBlockResult.successProbability(selectedFace, diceCount, opponentChooses)
    }

    /**
     * Dice roll that the algorithm cannot categorize and score without
     * guessing. If we hit this, we should refuse to score the action path and
     * instead report the problematic roll(s), so support can be added for them.
     */
    @Serializable
    data class Unsupported(
        override val index: Int,
        val rollType: DiceRollType? = null,
        val reason: String,
    ) : ActionPathEvent()
}

internal val ActionPathEvent.owner: TeamId?
    get() = when (this) {
        is ActionPathEvent.Block -> owner
        is ActionPathEvent.D6 -> owner
        is ActionPathEvent.PhysicalD6 -> owner
        is ActionPathEvent.Unsupported -> null
    }

internal val ActionPathEvent.scope: ActionPathEventScope?
    get() = when (this) {
        is ActionPathEvent.Block -> scope
        is ActionPathEvent.D6 -> scope
        is ActionPathEvent.PhysicalD6 -> scope
        is ActionPathEvent.Unsupported -> null
    }

internal val ActionPathEvent.recoveries: List<RerollOption>
    get() = when (this) {
        is ActionPathEvent.Block -> recoveries
        is ActionPathEvent.D6 -> recoveries
        is ActionPathEvent.PhysicalD6 -> recoveries
        is ActionPathEvent.Unsupported -> emptyList()
    }

internal val ActionPathEvent.observedOutcomeProbability: Probability
    get() = when (this) {
        is ActionPathEvent.Block -> observedOutcomeProbability
        is ActionPathEvent.D6 -> observedOutcome.probability
        is ActionPathEvent.PhysicalD6 -> observedOutcome.probability
        is ActionPathEvent.Unsupported -> error("Unsupported observations have no probability")
    }

/**
 * An exact ratio retained by probability scorers.
 *
 * Ratios are retained in the ledger so selected dice values do not acquire
 * rounding errors. The dynamic program converts them to [Probability] only
 * while evaluating the complete action path.
 */
@Serializable
data class OutcomeRatio(
    val favorableOutcomes: Int,
    val possibleOutcomes: Int,
) {
    init {
        require(possibleOutcomes > 0) { "The number of possible outcomes must be positive." }
        require(favorableOutcomes in 0..possibleOutcomes) {
            "Favorable outcomes must be between 0 and $possibleOutcomes: $favorableOutcomes"
        }
    }

    val probability: Probability
        get() = Probability(favorableOutcomes.toDouble() / possibleOutcomes)

    companion object {
        val CERTAIN = OutcomeRatio(1, 1)
    }
}

/**
 * ID used to identify the resource-reset boundaries containing a chance event.
 */
@Serializable
data class ActionPathEventScope(
    val half: Int,
    val drive: Int,
    val turn: Int,
    val player: PlayerId? = null,
)

/**
 * The two possible branches of a binary event.
 */
@Serializable
enum class ChanceBranch {
    /**
     * The branch selected by the coaching when running the action path.
     * Example: Selecting a 4 on a 3+ Dodge turns it into selecting the
     * "success" branch.
     */
    SELECTED,

    /**
     * The other branch, which breaks the selected action path.
     * Example: If a 4 was selected on a 3+ Dodge, the alternative branch would
     * be the 1 and 2 values that result in a failure.
     */
    ALTERNATIVE,
}

/**
 * [RecoveryResource] are grouped into categories, allowing policies to
 * order them based on the category. This makes it possible to create heuristics
 * for choosing the optimal reroll, which is far cheaper than using Dynamic
 * Programming to find them.
 *
 * TODO Expand these categories to cover the categories we care about.
 */
@Serializable
enum class RerollCategory {
    STANDARD_SKILL, // "free" skill rerolls (that might only apply to a single dice roll type)
    PRO, // Pro is special because it provides a flexible reroll that is guarded by its own roll
    TEAM_REROLL, // A team reroll that applies to the curren team turn
}

/** How often a [RerollResource] can be consumed. */
@Serializable
enum class RerollUsage {
    REUSABLE, // The source is available independently for every event.
    ONCE_PER_ACTION,
    ONCE_PER_ACTIVATION,
    ONCE_PER_TURN,
    ONCE_PER_DRIVE,
    ONCE_PER_HALF,
    ONCE_PER_GAME,
    UNSUPPORTED, // The source cannot be represented safely by the current state model.
}

/** A stable, consumable recovery resource referenced by one or more events. */
@Serializable
data class RecoveryResource(
    val id: RerollSourceId,
    val owner: TeamId,
    val category: RerollCategory,
    val usage: RerollUsage,
)

/** A recovery source the coach demonstrably attempted to use. */
@Serializable
data class ActualRecoveryUse(
    val resource: RecoveryResource,
    val description: String,
)

/** Why a physical D6 exists in an actual-choice trace. */
@Serializable
enum class PhysicalD6Role {
    PRIMARY,
    ACTIVATION,
    REROLL,
}

/** What happens when a recovery's activation roll fails. */
@Serializable
enum class ActivationFailureBehavior {
    /** Keep the original result and do not try another recovery. */
    STOP,

    /** Reserved for mechanics that v1 deliberately refuses to approximate. */
    UNSUPPORTED,
}

/**
 * A reroll option that can be selected for a particular [ActionPathEvent].
 *
 * [appliesTo] is expressed relative to the demonstrated branch. This matters
 * for skills such as Catch, which can reroll a failed roll but not a successful
 * one. The activation chance belongs to the edge rather than the resource: a
 * team reroll can be guaranteed for one player and Loner-gated for another.
 */
@Serializable
data class RerollOption(
    val resource: RecoveryResource,
    val activation: OutcomeRatio = OutcomeRatio.CERTAIN,
    val appliesTo: Set<ChanceBranch>,
    val activationFailure: ActivationFailureBehavior = ActivationFailureBehavior.STOP,
)
