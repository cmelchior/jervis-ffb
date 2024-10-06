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
 * Some skills trigger when an opponent player are about to start their action,
 * like Dump-off and Foul Appearance. This procedure is responsible for checking for
 * these cases and potentially react to them.
 */
object CheckForActionInterruptSkills: Procedure() {
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
