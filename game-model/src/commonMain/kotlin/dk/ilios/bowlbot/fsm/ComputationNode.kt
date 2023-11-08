package dk.ilios.bowlbot.fsm

import dk.ilios.bowlbot.actions.Action
import dk.ilios.bowlbot.actions.ActionDescriptor
import dk.ilios.bowlbot.actions.Continue
import dk.ilios.bowlbot.actions.ContinueWhenReady
import dk.ilios.bowlbot.commands.Command
import dk.ilios.bowlbot.model.Game

abstract class ComputationNode: ActionNode() {
    abstract fun apply(state: Game): Command
    override fun getAvailableActions(state: Game): List<ActionDescriptor> = listOf(ContinueWhenReady)
    override fun applyAction(action: Action, state: Game): Command {
        return checkType<Continue>(action) {
            apply(state)
        }
    }
}