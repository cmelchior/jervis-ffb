package dk.ilios.jervis.fsm

import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.Confirm
import dk.ilios.jervis.actions.ConfirmWhenReady
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.rules.Rules

abstract class ConfirmationNode : ActionNode() {
    abstract fun apply(
        state: Game,
        rules: Rules,
    ): Command

    override fun getAvailableActions(
        state: Game,
        rules: Rules,
    ): List<ActionDescriptor> = listOf(ConfirmWhenReady)

    override fun applyAction(
        action: GameAction,
        state: Game,
        rules: Rules,
    ): Command {
        return checkType<Confirm>(action) {
            apply(state, rules)
        }
    }
}
