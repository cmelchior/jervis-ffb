package com.jervisffb.engine.fsm

import com.jervisffb.engine.actions.DicePoolResultsSelected
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.InvalidActionException

/**
 * Cast the [GameAction] to the expected subtype.
 * This method assumes that [com.jervisffb.engine.ActionRequest.isValid] returned `true`.
 * Will throw if not possible.
 */
inline fun <reified T : GameAction> ActionNode.castAction(
    action: GameAction,
    function: (T) -> Command,
): Command {
    if (action is T) {
        return function(action)
    } else {
        throw InvalidActionException("Action ($action) is not of the expected type: ${T::class.simpleName}")
    }
}

/**
 * Smart cast a Game Action to the defined dice type, using both [DiceRollResults] and [DieResult].
 * This method assumes that [com.jervisffb.engine.ActionRequest.isValid] returned `true`.
 * Will throw if not possible.
 */
inline fun <reified D1 : DieResult> ActionNode.castDiceRoll(
    action: GameAction,
    function: (D1) -> Command,
): Command {
    when (action) {
        is D1 -> {
            return function(action)
        }
        is DiceRollResults -> {
            val first: DieResult = action.rolls.first()
            return function(first as D1)
        }
        else -> {
            throw InvalidActionException(
                "Action ($action) is not of the expected type: ${DiceRollResults::class.simpleName}",
            )
        }
    }
}

/**
 * Helper method for easily accessing a single die in a dice pool.
 * This method assumes that [com.jervisffb.engine.ActionRequest.isValid] returned `true`.
 * Will throw if not possible.
 */
inline fun <reified D1 : DieResult> ActionNode.castDicePool(
    action: GameAction,
    function: (D1) -> Command,
): Command {
    if (action !is DicePoolResultsSelected) INVALID_ACTION(action)
    val first = action.results.first().diceSelected.single()
    return function(first.result as D1)
}

/**
 * Helper method for easily accessing the result of two dice rolls.
 * This method assumes that [com.jervisffb.engine.ActionRequest.isValid] returned `true`.
 * Will throw if not possible.
 */
inline fun <reified D1 : DieResult, reified D2 : DieResult> ActionNode.castDiceRoll(
    action: GameAction,
    function: (D1, D2) -> Command,
): Command {
    if (action is DiceRollResults) {
        val first: DieResult = action.rolls[0]
        val second: DieResult = action.rolls[1]
        return function(first as D1, second as D2)
    } else {
        throw InvalidActionException(
            "Action ($action) is not of the expected type: ${DiceRollResults::class.simpleName}",
        )
    }
}

/**
 * Helper method for easily accessing the result of a number of dice (of the same type).
 * This method assumes that [com.jervisffb.engine.ActionRequest.isValid] returned `true`.
 * Will throw if not possible.
 */
inline fun <reified D1 : DieResult> ActionNode.castDiceRollList(
    action: GameAction,
    function: (List<D1>) -> Command,
): Command {
    when (action) {
        is DiceRollResults -> {
            @Suppress("UNCHECKED_CAST")
            return function(action.rolls as List<D1>)
        }
        is D1 -> {
            return function(listOf(action))
        }
        else -> {
            throw InvalidActionException(
                "Action ($action) is not of the expected type: ${DiceRollResults::class.simpleName}",
            )
        }
    }
}

/**
 * Helper method for easily accessing the outcome of selecting 2 dice, one from each dice pool.
 * This method assumes that [com.jervisffb.engine.ActionRequest.isValid] returned `true`.
 * Will throw if not possible.
 */
inline fun <reified D1 : DieResult, reified D2 : DieResult> ActionNode.castDicePool(
    action: GameAction,
    function: (D1, D2) -> Command,
): Command {
    if (action !is DicePoolResultsSelected) INVALID_ACTION(action)
    val first = action.results.first().diceSelected.single().result
    val second = action.results.last().diceSelected.single().result
    return function(first as D1, second as D2)
}
