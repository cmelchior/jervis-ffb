package dk.ilios.jervis.fsm

import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.Continue
import dk.ilios.jervis.actions.ContinueWhenReady
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.rules.Rules

abstract class ComputationNode : ActionNode() {
    abstract fun apply(
        state: Game,
        rules: Rules,
    ): Command

    override fun getAvailableActions(
        state: Game,
        rules: Rules,
    ): List<ActionDescriptor> = listOf(ContinueWhenReady)

    override fun applyAction(
        action: GameAction,
        state: Game,
        rules: Rules,
    ): Command {
        return checkType<Continue>(action) {
            apply(state, rules)
        }
    }
}
