package dk.ilios.bowlbot.fsm

import dk.ilios.bowlbot.actions.Action
import dk.ilios.bowlbot.actions.ActionDescriptor
import dk.ilios.bowlbot.actions.Continue
import dk.ilios.bowlbot.actions.ContinueWhenReady
import dk.ilios.bowlbot.commands.Command
import dk.ilios.bowlbot.model.Game
import dk.ilios.bowlbot.rules.Rules

abstract class ComputationNode: ActionNode() {
    abstract fun apply(state: Game, rules: Rules): Command
    override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> = listOf(ContinueWhenReady)
    override fun applyAction(action: Action, state: Game, rules: Rules): Command {
        return checkType<Continue>(action) {
            apply(state, rules)
        }
    }
}