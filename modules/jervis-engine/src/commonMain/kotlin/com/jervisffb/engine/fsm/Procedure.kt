package com.jervisffb.engine.fsm

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.commands.fsm.RemoveCurrentProcedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.Rules
import kotlinx.serialization.Serializable

@Serializable
abstract class Procedure {
    open fun name(): String = this::class.simpleName!!

    // Will be called after `enterNode`
    abstract val initialNode: Node
    // First thing called when entering this node.
    // It is called when this procedure has been added on the stack
    val enterNode: Node = EnterProcedureNode(this)
    // Last thing called when leaving this procedure.
    // It is called before this procedure is removed.
    val exitNode: Node = ExitProcedureNode(this)

    // this method will be called just before the procedure transitions to the initialNode
    abstract fun onEnterProcedure(state: Game, rules: Rules): Command?

    // this method will be called just before the Procedure is removed from the stack
    abstract fun onExitProcedure(state: Game, rules: Rules): Command?

    // Make it possible to check if this procedure can be called with the given game state.
    // Failures should be reported as exceptions
    open fun isValid(state: Game, rules: Rules) {
        // Do nothing
    }

    private class EnterProcedureNode(private val procedure: Procedure) : ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                procedure.onEnterProcedure(state, rules),
                GotoNode(procedure.initialNode),
            )
        }
    }

    private class ExitProcedureNode(private val procedure: Procedure) : ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                procedure.onExitProcedure(state, rules),
                RemoveCurrentProcedure(),
            )
        }
    }
}
