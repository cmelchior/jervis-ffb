package dk.ilios.bowlbot.fsm

import compositeCommandOf
import dk.ilios.bowlbot.commands.Command
import dk.ilios.bowlbot.commands.EnterProcedure
import dk.ilios.bowlbot.commands.ExitProcedure
import dk.ilios.bowlbot.commands.GotoNode
import dk.ilios.bowlbot.commands.NoOpCommand
import dk.ilios.bowlbot.model.Game
import dk.ilios.bowlbot.rules.Rules

/**
 * A ParentNode is how we can break a state machine down into continuously fine-grained steps.
 * I.e. A `Half` in Blood Bowl is split into `Drives`, which are split into `Turns`, which are split into
 * `Player Actions`.
 *
 * Calling a child procedure is its own state-machine with [OnEnter] and [OnExit] states which makes it possible to
 * that makes it possible to control state and flow after entering and exiting the child procedure.
 */
abstract class ParentNode: Procedure, Node {
    override fun name(): String = this::class.simpleName!!
    open fun onEnter(state: Game, rules: Rules): Command = NoOpCommand
    abstract val childProcedure: Procedure
    abstract fun onExit(state: Game, rules: Rules): Command

    override val initialNode: ActionNode = OnEnter()

    private inner class OnEnter: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                onEnter(state, rules),
                GotoNode(LoadSubProcedure())
            )
        }
    }

    private inner class LoadSubProcedure: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                // Manipulate the stack by putting the exit event on before loading the next procedure
                GotoNode(OnExit()),
                EnterProcedure(childProcedure)
            )
        }
    }

    private inner class OnExit: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                // Exit the sub procedure before calling onExit, this means that GotoNode instructions
                // work correctly in the context of the procedure that loaded the sub procedure
                ExitProcedure(),
                onExit(state, rules),
            )
         }
    }
}