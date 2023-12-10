package dk.ilios.jervis.fsm

import dk.ilios.jervis.actions.Action
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.rules.Rules

abstract class ActionNode: Node {
    abstract fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor>
    abstract fun applyAction(action: Action, state: Game, rules: Rules): Command
    inline fun <reified T: Action> checkType(action: Action): T {
        if (action is T) {
            return action
        } else {
            throw IllegalArgumentException("Action (${action::class}) is not of the expected type: ${T::class}")
        }
    }
    inline fun <reified T: Action> checkType(action: Action, function: (T) -> Command): Command {
        if (action is T) {
            return function(action)
        } else {
            throw IllegalArgumentException("Action (${action::class}) is not of the expected type: ${T::class}")
        }
    }
}