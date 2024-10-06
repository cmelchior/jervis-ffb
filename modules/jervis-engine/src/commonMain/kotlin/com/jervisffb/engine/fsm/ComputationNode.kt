package com.jervisffb.engine.fsm

import com.jervisffb.engine.actions.ActionDescriptor
import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.Rules

/**
 * Helper node type that makes it easier to create "transition" nodes, that just
 * run computations but doesn't require user input.
 */
abstract class ComputationNode : ActionNode() {
    override fun actionOwner(state: Game, rules: Rules) = null

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
