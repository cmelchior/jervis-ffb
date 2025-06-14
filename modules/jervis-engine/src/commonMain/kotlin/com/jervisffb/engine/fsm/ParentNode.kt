package com.jervisffb.engine.fsm

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.EnterProcedure
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ChangeParentNodeState
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.Rules

/**
 * A ParentNode is how we can break a state machine down into continuously
 * fine-grained steps. I.e. A `Half` in Blood Bowl is split into `Drives`,
 * which are split into `Turns`, which are split into `Player Actions`.
 *
 * Calling a child procedure is its own little state machine with [onEnterNode]
 * and [onExitNode] methods, which allows us to better control the flow in and
 * out of child procedures.
 */
abstract class ParentNode : Node {

    /**
     * The state of the [ParentNode].
     * This is only used for internal bookkeeping.
     */
    enum class State {
        CHECK_SKIP,
        ENTERING,
        RUNNING,
        EXITING,
    }

    /**
     * Returns the [Procedure] this node should load and go into.
     */
    abstract fun getChildProcedure(state: Game, rules: Rules): Procedure

    /**
     * Called before [onEnterNode]. This will check if we want to skip this
     * node completely. If a [Node] is returned, the current node is skipped in
     * favor of the returned one. If `null` is returned, the node is executed
     * as normal, and [onEnterNode] is called next.
     */
    open fun skipNodeFor(state: Game, rules: Rules): Node? = null

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
    fun shouldEnterNode(state: Game, rules: Rules): Command {
        val newNode = skipNodeFor(state, rules)
        return if (newNode != null) {
            GotoNode(newNode)
        } else {
            ChangeParentNodeState(State.ENTERING)
        }
    }

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

    /**
     * Helper node that makes it easy to exit a procedure from [ParentNode.skipNodeFor]
     */
    protected object ExitProcedureNode: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command = ExitProcedure()
    }
}

