package com.jervisffb.engine.fsm

import compositeCommandOf
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.EnterProcedure
import com.jervisffb.engine.commands.fsm.ChangeParentNodeState
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.Rules

/**
 * A ParentNode is how we can break a state machine down into continuously fine-grained steps.
 * I.e. A `Half` in Blood Bowl is split into `Drives`, which are split into `Turns`, which are split into
 * `Player Actions`.
 *
 * Calling a child procedure is its own state-machine with [EnterParentNode] and [ExitParentNode] states which makes it possible to
 * control state and flow after entering and exiting the child procedure.
 */
abstract class ParentNode : Node {

    /**
     * The state of the [ParentNode].
     * This is only used for internal bookkeeping.
     */
    enum class State {
        ENTERING,
        RUNNING,
        EXITING,
    }

    // TODO Should we add something like a `skipNodeFor` method?
    //  This would eliminate the need for `CheckFor<X>` computation nodes
    //  and move the check inline into parent nodes. The downside is that the parent
    //  node lifecycle gets even more complex :thinking:

    /**
     * Returns the [Procedure] this node should load and go into.
     */
    abstract fun getChildProcedure(state: Game, rules: Rules): Procedure

    /**
     * Called just before loading the child procedure. It is called on the level of the
     * [ParentNode] and not the procedure returned by [getChildProcedure].
     *
     * This makes it possible to define some state required by the child procedure.
     *
     * Calling [GotoNode] or [ExitProcedure] at this point is not legal.
     */
    open fun onEnterNode(state: Game, rules: Rules): Command? = null

    /**
     * Called when the child procedure has fully completed. It is called on the level of
     * the [ParentNode] and not the procedure returned by [getChildProcedure].
     *
     * This method is responsible for determining where the FSM should transition
     * to next.
     */
    abstract fun onExitNode(state: Game, rules: Rules): Command

    // This method should only be called by `GameController`
    // It is not supposed to be called by procedure subclasses.
    fun enterNode(state: Game, rules: Rules): Command {
        return compositeCommandOf(
            onEnterNode(state, rules),
            ChangeParentNodeState(State.RUNNING),
        )
    }

    // This method should only be called by `GameController`
    // It is not supposed to be called by procedure subclasses.
    fun runNode(state: Game, rules: Rules): Command {
        return compositeCommandOf(
            // Manipulate the stack by moving to the EXIT state before loading the
            // child procedure. That way, when the child procedure exits, it will
            // return to the correct state.
            ChangeParentNodeState(State.EXITING),
            EnterProcedure(getChildProcedure(state, rules)),
        )
    }

    // This method should only be called by `GameController`
    // It is not supposed to be called by procedure subclasses.
    fun exitNode(state: Game, rules: Rules): Command {
        return onExitNode(state, rules)
    }
}
