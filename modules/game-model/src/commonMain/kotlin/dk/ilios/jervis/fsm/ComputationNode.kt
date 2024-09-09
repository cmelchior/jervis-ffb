package dk.ilios.jervis.fsm

import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.Continue
import dk.ilios.jervis.actions.ContinueWhenReady
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.rules.Rules

/**
 * Helper node type that makes it easier to create "transition" nodes, that just
 * run computations but doesn't require user input.
 */
abstract class ComputationNode : ActionNode() {
    abstract fun apply(state: Game, rules: Rules): Command

    override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
        return listOf(ContinueWhenReady)
    }

    override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
        return checkType<Continue>(action) {
            apply(state, rules)
        }
    }
}
