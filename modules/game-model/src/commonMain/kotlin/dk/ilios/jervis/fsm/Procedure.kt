package dk.ilios.jervis.fsm

import compositeCommandOf
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.GotoNode
import dk.ilios.jervis.commands.RemoveCurrentProcedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.rules.Rules
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

    open fun isValid(
        state: Game,
        rules: Rules,
    ) {
        // Do nothing
    }

    // this method will be called just before the procedure transitions to the initialNode
    abstract fun onEnterProcedure(
        state: Game,
        rules: Rules,
    ): Command?

    // this method will be called just before the Procedure is removed from the stack
    abstract fun onExitProcedure(
        state: Game,
        rules: Rules,
    ): Command?

    private class EnterProcedureNode(private val procedure: Procedure) : ComputationNode() {
        override fun apply(
            state: Game,
            rules: Rules,
        ): Command {
            return compositeCommandOf(
                procedure.onEnterProcedure(state, rules),
                GotoNode(procedure.initialNode),
            )
        }
    }

    private class ExitProcedureNode(private val procedure: Procedure) : ComputationNode() {
        override fun apply(
            state: Game,
            rules: Rules,
        ): Command {
            return compositeCommandOf(
                procedure.onExitProcedure(state, rules),
                RemoveCurrentProcedure(),
            )
        }
    }
}
