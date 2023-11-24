package dk.ilios.bowlbot.fsm

import compositeCommandOf
import dk.ilios.bowlbot.commands.Command
import dk.ilios.bowlbot.commands.GotoNode
import dk.ilios.bowlbot.commands.NoOpCommand
import dk.ilios.bowlbot.commands.RemoveCurrentProcedure
import dk.ilios.bowlbot.model.Game
import dk.ilios.bowlbot.rules.Rules

abstract class Procedure {
    open fun name(): String = this::class.simpleName!!
    abstract val initialNode: Node
    val enterNode: Node = EnterProcedureNode(this)
    val exitNode: Node = ExitProcedureNode(this)

    // this method will be called just before the procedure transitions to the initialNode
    abstract fun onEnterProcedure(state: Game, rules: Rules): Command?
    // this method will be called just before the Procedure is removed from the stack
    abstract fun onExitProcedure(state: Game, rules: Rules): Command?

    private class EnterProcedureNode(private val procedure: Procedure) : ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                procedure.onEnterProcedure(state, rules) ?: NoOpCommand,
                GotoNode(procedure.initialNode)
            )
        }
    }

    private class ExitProcedureNode(private val procedure: Procedure) : ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                procedure.onExitProcedure(state, rules) ?: NoOpCommand,
                RemoveCurrentProcedure()
            )
        }
    }
}
