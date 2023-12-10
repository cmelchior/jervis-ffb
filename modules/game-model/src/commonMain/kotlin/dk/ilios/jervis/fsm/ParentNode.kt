package dk.ilios.jervis.fsm

import compositeCommandOf
import dk.ilios.jervis.commands.ChangeParentNodeState
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.EnterProcedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.rules.Rules

/**
 * A ParentNode is how we can break a state machine down into continuously fine-grained steps.
 * I.e. A `Half` in Blood Bowl is split into `Drives`, which are split into `Turns`, which are split into
 * `Player Actions`.
 *
 * Calling a child procedure is its own state-machine with [EnterParentNode] and [ExitParentNode] states which makes it possible to
 * that makes it possible to control state and flow after entering and exiting the child procedure.
 */
abstract class ParentNode: Node {

    enum class State {
        ENTERING, RUNNING, EXITING
    }

    override fun name(): String = this::class.simpleName!!
    abstract fun getChildProcedure(state: Game, rules: Rules): Procedure
    open fun onEnterNode(state: Game, rules: Rules): Command? = null
    abstract fun onExitNode(state: Game, rules: Rules): Command

    fun enterNode(state: Game, rules: Rules): Command {
        return compositeCommandOf(
            onEnterNode(state, rules),
            ChangeParentNodeState(State.RUNNING)
        )
    }

    fun runNode(state: Game, rules: Rules): Command {
        return compositeCommandOf(
            // Manipulate the stack by moving to the EXIT state before loading the
            // child procedure. That way, when the child procedure exits, it will
            // return in the correct state.
            ChangeParentNodeState(State.EXITING),
            EnterProcedure(getChildProcedure(state, rules)),
        )
    }

    fun exitNode(state: Game, rules: Rules): Command {
        return onExitNode(state, rules)
    }
}