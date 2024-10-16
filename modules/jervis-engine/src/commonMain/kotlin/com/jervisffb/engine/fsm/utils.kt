package com.jervisffb.engine.fsm

import com.jervisffb.engine.actions.DicePoolResultsSelected
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectFieldLocation
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.actions.SelectSkill
import com.jervisffb.engine.actions.SkillSelected
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.utils.INVALID_ACTION

/**
 * Check that not only verify the game action type, but also the value.
 * This is, e.g., relevant when selecting locations or players.
 */
inline fun <reified T : GameAction> ActionNode.checkTypeAndValue(
    state: Game,
    rules: Rules,
    action: GameAction,
    function: (T) -> Command,
): Command {
    val node = this
    if (action is T) {
        val availableActions = node.getAvailableActions(state, rules)
        // Validate that an action descriptor exists with the provided value
        when(action) {
            is FieldSquareSelected -> {
                val hasActionDescriptor = node.getAvailableActions(state, rules)
                    .filterIsInstance<SelectFieldLocation>()
                    .firstOrNull { action.x == it.x && action.y == it.y } != null
                if (!hasActionDescriptor) {
                    INVALID_ACTION(action, "Location wasn't recognised as a valid action: $availableActions")
                }
            }
            is PlayerSelected -> {
                val hasActionDescriptor = node.getAvailableActions(state, rules)
                    .filterIsInstance<SelectPlayer>()
                    .firstOrNull { it.player == action.playerId } != null
                if (!hasActionDescriptor) {
                    INVALID_ACTION(action, "Player $action wasn't recognised as a valid action: $availableActions")
                }
            }
            is SkillSelected -> {
                val hasActionDescriptor = node.getAvailableActions(state, rules)
                    .filterIsInstance<SelectSkill>()
                    .firstOrNull { it.skill == action } != null
                if (!hasActionDescriptor) {
                    INVALID_ACTION(action, "Skill wasn't recognised as a valid action: $availableActions")
                }
            }
            else -> { /* Do nothing */ }
        }
        return function(action)
    } else {
        INVALID_ACTION(action, "Action (${action::class}) is not of the expected type: ${T::class}")
    }
}

inline fun <reified T : GameAction> ActionNode.checkType(
    action: GameAction,
    function: (T) -> Command,
): Command {
    val userAction =
        if (action is DiceRollResults && action.rolls.size == 1) {
            action.rolls.first()
        } else {
            action
        }

    if (userAction is T) {
        return function(userAction)
    } else {
        throw IllegalArgumentException("Action (${action::class}) is not of the expected type: ${T::class}")
    }
}

inline fun <reified D1 : DieResult> ActionNode.checkDiceRoll(
    action: GameAction,
    function: (D1) -> Command,
): Command {
    when (action) {
        is DiceRollResults -> {
            if (action.rolls.size != 1) {
                throw IllegalArgumentException("Expected 1 dice rolls, got ${action.rolls.size}")
            }
            val first: DieResult = action.rolls.first()
            if (first !is D1) {
                throw IllegalArgumentException("Expected first roll to be ${D1::class}, but was ${first::class}")
            }
            return function(first)
        }
        is D1 -> {
            return function(action)
        }
        else -> {
            throw IllegalArgumentException(
                "Action (${action::class}) is not of the expected type: ${DiceRollResults::class}",
            )
        }
    }
}

inline fun <reified D1 : DieResult> ActionNode.checkDicePool(
    action: GameAction,
    function: (D1) -> Command,
): Command {
    if (action !is DicePoolResultsSelected) INVALID_ACTION(action)
    if (action.results.size != 1) INVALID_ACTION(action, "Expected single dice pool result, got ${action.results.size}")
    action.results.single().let {
        if (it.id != 0) INVALID_ACTION(action)
        if (it.diceSelected.size != 1) INVALID_ACTION(action)
    }
    val first = action.results.first().diceSelected.single()
    if (first !is D1) {
        INVALID_ACTION(action, "Expected first roll to be ${D1::class}, but was ${first::class}")
    }
    return function(first)
}


inline fun <reified D1 : DieResult, reified D2 : DieResult> ActionNode.checkDiceRoll(
    action: GameAction,
    function: (D1, D2) -> Command,
): Command {
    if (action is DiceRollResults) {
        if (action.rolls.size != 2) {
            throw IllegalArgumentException("Expected 2 dice rolls, got ${action.rolls.size}")
        }
        val first: DieResult = action.rolls[0]
        val second: DieResult = action.rolls[1]
        if (first !is D1) {
            throw IllegalArgumentException("Expected first roll to be ${D1::class}, but was ${first::class}")
        }
        if (second !is D2) {
            throw IllegalArgumentException("Expected first roll to be ${D1::class}, but was ${second::class}")
        }
        return function(first, second)
    } else {
        throw IllegalArgumentException(
            "Action (${action::class}) is not of the expected type: ${DiceRollResults::class}",
        )
    }
}

inline fun <reified D1 : DieResult> ActionNode.checkDiceRollList(
    action: GameAction,
    function: (List<D1>) -> Command,
): Command {
    if (action is DiceRollResults) {
        val first = action.rolls.first()
        if (first !is D1) {
            throw IllegalArgumentException("Expected first roll to be ${D1::class}, but was ${first::class}")
        }
        @Suppress("UNCHECKED_CAST")
        return function(action.rolls as List<D1>)
    } else if (action is D1) {
        return function(listOf(action))
    } else {
        throw IllegalArgumentException(
            "Action (${action::class}) is not of the expected type: ${DiceRollResults::class}",
        )
    }
}

inline fun <reified D1 : DieResult, reified D2 : DieResult> ActionNode.checkDicePool(
    action: GameAction,
    function: (D1, D2) -> Command,
): Command {
    if (action !is DicePoolResultsSelected) INVALID_ACTION(action)
    if (action.results.size != 2) INVALID_ACTION(action, "Expected 2 dice pool results, got ${action.results.size}")
    action.results.first().let {
        if (it.id != 0) INVALID_ACTION(action, "Unexpected dice pool result ID: ${it.id}")
        if (it.diceSelected.size != 1) INVALID_ACTION(action)
    }
    action.results.last().let {
        if (it.id != 1) INVALID_ACTION(action, "Unexpected dice pool result ID: ${it.id}")
        if (it.diceSelected.size != 1) INVALID_ACTION(action)
    }
    val first = action.results.first().diceSelected.single()
    val second = action.results.last().diceSelected.single()
    if (first !is D1) {
        INVALID_ACTION(action, "Expected first roll to be ${D1::class}, but was ${first::class}")
    }
    if (second !is D2) {
        INVALID_ACTION(action, "Expected first roll to be ${D1::class}, but was ${second::class}")
    }
    return function(first, second)
}
