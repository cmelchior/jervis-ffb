package com.jervisffb.engine.fsm

import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.Rules

/**
 * Helper node type that makes it easier to create "transition" nodes. These
 * nodes are defined as only accepting a [Continue] action and are generally
 * just used to organize the [Procedure]s in a better way. This means they will
 * just run some computation and automatically continue after that.
 */
abstract class ComputationNode : ActionNode() {
    override fun actionOwner(state: Game, rules: Rules) = null

    abstract fun apply(state: Game, rules: Rules): Command

    override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
        return listOf(ContinueWhenReady)
    }

    override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
        return checkType<Continue>(action) {
            apply(state, rules)
        }
    }
}
