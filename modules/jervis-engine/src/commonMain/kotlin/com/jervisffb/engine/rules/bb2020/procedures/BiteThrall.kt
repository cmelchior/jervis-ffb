package com.jervisffb.engine.rules.bb2020.procedures

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.rules.Rules

/**
 * Procedure responsible for handling biting a Thrall as a consequence of having
 * failed a Blood Lust roll.
 *
 * Biting a Thrall can happen both in the middle of an action (Pass / Hand-off)
 * or at the end of a player's activation.
 *
 * Failing to bite a thrall (either voluntary or involuntary) will result in a
 * turn-over, so callers of this procedure should check for that when it
 * returns.
 */
object BiteThrall: Procedure() {
    override val initialNode: Node = Dummy
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<ActivatePlayerContext>()

    object Dummy: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            return ExitProcedure()
        }
    }
}
