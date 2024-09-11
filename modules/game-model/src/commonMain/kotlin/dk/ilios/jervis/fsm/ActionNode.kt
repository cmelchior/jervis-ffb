package dk.ilios.jervis.fsm

import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.DiceResults
import dk.ilios.jervis.actions.DieResult
import dk.ilios.jervis.actions.FieldSquareSelected
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.actions.SelectFieldLocation
import dk.ilios.jervis.actions.SelectPlayer
import dk.ilios.jervis.actions.SelectSkill
import dk.ilios.jervis.actions.SkillSelected
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.utils.INVALID_ACTION

/**
 * Node type that represents the need of a users "action" to progress.
 *
 * Actions are described using [ActionDescriptor], while the actual user action
 * is represented using a [GameAction]
 */
abstract class ActionNode : Node {

    /**
     * Returns which team are responsible for sending an action to [applyAction], `null` if
     * it doesn't matter. In which case, it will be treated as a "System" action.
     *
     * Developer's Commentary:
     * We need to have a way to tell the rest of the system who is responsible for
     * creating the [GameAction]. It might technically be more correct to store this
     * inside [Game] or the [ActionDescriptor], but either of these approaches
     * would make the code quite a bit more convoluted. So for now, we are just
     * treating it as metadata that are part of a node, similar to [Procedure.initialNode]
     *
     * This approach also assumes that any given node only accepts input from
     * one player. Which (for now) seems a reasonable restriction.
     */
    abstract fun actionOwner(state: Game, rules: Rules): Team?

    /**
     * Returns the set of valid [GameAction]s that will be accepted by this node.
     */
    abstract fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor>

    /**
     * Calculate all changes that will happen as a consequence of applying a specific
     * [GameAction] to this node.
     */
    abstract fun applyAction(action: GameAction, state: Game, rules: Rules): Command

    /**
     * Check that not only verify the game action type, but also the value.
     * This is, e.g., relevant when selecting locations or players.
     */
    inline fun <reified T : GameAction> checkTypeAndValue(
        state: Game,
        rules: Rules,
        action: GameAction,
        node: ActionNode,
        function: (T) -> Command,
    ): Command {
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

    inline fun <reified T : GameAction> checkType(
        action: GameAction,
        function: (T) -> Command,
    ): Command {
        val userAction =
            if (action is DiceResults && action.rolls.size == 1) {
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

    inline fun <reified D1 : DieResult> checkDiceRoll(
        action: GameAction,
        function: (D1) -> Command,
    ): Command {
        when (action) {
            is DiceResults -> {
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
                    "Action (${action::class}) is not of the expected type: ${DiceResults::class}",
                )
            }
        }
    }

    inline fun <reified D1 : DieResult, reified D2 : DieResult> checkDiceRoll(
        action: GameAction,
        function: (D1, D2) -> Command,
    ): Command {
        if (action is DiceResults) {
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
                "Action (${action::class}) is not of the expected type: ${DiceResults::class}",
            )
        }
    }

    inline fun <reified D1 : DieResult> checkDiceRollList(
        action: GameAction,
        function: (List<D1>) -> Command,
    ): Command {
        if (action is DiceResults) {
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
                "Action (${action::class}) is not of the expected type: ${DiceResults::class}",
            )
        }
    }
}
