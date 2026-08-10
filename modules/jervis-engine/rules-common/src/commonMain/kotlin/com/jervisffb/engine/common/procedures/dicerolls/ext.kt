package com.jervisffb.engine.common.procedures.dicerolls

import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.statistics.probability.event.ChanceOutcomeCategory
import com.jervisffb.engine.statistics.probability.event.OutcomeRatio
import com.jervisffb.engine.statistics.probability.observation.ChanceDieResult
import com.jervisffb.engine.statistics.probability.observation.ChanceObservation
import com.jervisffb.engine.statistics.probability.observation.ChanceObservationScope
import com.jervisffb.engine.statistics.probability.observation.ChanceOutcome
import com.jervisffb.engine.statistics.probability.observation.ChanceResultId

/**
 * Creates a finalized observation for a roll that is looking a result up on a
 * table. This means that the outcome is TARGET_SET and the result is an
 * automatic "success" since, by our definition of the Jervis Probability Score,
 * rolling a die also sets the target to that die.
 *
 * Examples: Rolling on the Kick-off or Weather table.
 * Example: Coin Toss can be seen as a table lookup with 2 entries.
 */
fun createFinalTableLookupObservation(
    state: Game,
    team: Team,
    rollType: DiceRollType,
    dice: List<DieResult>,
    favorableOutcomes: Int,
    possibleOutcomes: Int,
): ChanceObservation.DiceRoll? {
    if (!state.collectChanceData) return null
    val index = state.nextAvailableChanceObservationIndex
    return ChanceObservation.DiceRoll(
        index = index,
        rollType = rollType,
        team = team.id,
        dice = dice.mapIndexed { resultIndex, result ->
            ChanceDieResult(
                id = ChanceResultId(index, resultIndex),
                result = result,
            )
        },
        scope = ChanceObservationScope.fromState(state, team),
        success = true,
        outcome = ChanceOutcome(
            category = ChanceOutcomeCategory.TARGET_SET,
            successProbability = OutcomeRatio(favorableOutcomes, possibleOutcomes),
        ),
        finalized = true,
    )
}

/**
 * Creates a finalized observation for a single die roll that has an AT_LEAST
 * outcome.
 *
 * We set the target to the value of the die rolled. This is on purpose, as the
 * Jervis Probability Score measures the probability of the selected sequence
 * and not the probability to reach an optimal outcome. Thus, the rolled value
 * becomes the target for "success".
 *
 * Used for all single D6 rolls that cannot be rerolled, like Kickoff Table
 * events (Cheering Fans, Dodgy Snack).
 */
private fun createFinalAtLeastObservation(
    state: Game,
    team: Team,
    player: Player?,
    rollType: DiceRollType,
    die: DieResult,
    target: Int = die.value,
): ChanceObservation.DiceRoll? {
    if (!state.collectChanceData) return null
    val index = state.nextAvailableChanceObservationIndex
    val success: Boolean = die.value >= target
    val possibleOutcomes = (die.max - die.min + 1)
    val favorableOutcomes = (possibleOutcomes - target) + 1

    return ChanceObservation.DiceRoll(
        index = index,
        rollType = rollType,
        team = team.id,
        player = player?.id,
        dice = listOf(die).mapIndexed { resultIndex, result ->
            ChanceDieResult(
                id = ChanceResultId(index, resultIndex),
                result = result,
            )
        },
        scope = ChanceObservationScope.fromState(state, team),
        success = success,
        outcome = ChanceOutcome(
            category = ChanceOutcomeCategory.AT_LEAST,
            successProbability = OutcomeRatio(favorableOutcomes, possibleOutcomes),
        ),
        finalized = true,
    )
}

// Overload helper
fun createFinalAtLeastObservation(
    state: Game,
    player: Player,
    rollType: DiceRollType,
    die: DieResult,
    target: Int = die.value,
): ChanceObservation.DiceRoll? {
    return createFinalAtLeastObservation(
        state = state,
        team = player.team,
        player = player,
        rollType = rollType,
        die = die,
        target = target,
    )
}

// Overload helper
fun createFinalAtLeastObservation(
    state: Game,
    team: Team,
    rollType: DiceRollType,
    die: DieResult,
    target: Int = die.value,
): ChanceObservation.DiceRoll? {
    return createFinalAtLeastObservation(
        state = state,
        team = team,
        player = null,
        rollType = rollType,
        die = die,
        target = target,
    )
}
