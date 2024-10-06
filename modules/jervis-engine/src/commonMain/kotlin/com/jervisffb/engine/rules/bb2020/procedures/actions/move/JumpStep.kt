package com.jervisffb.engine.rules.bb2020.procedures.actions.move

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.Rules

/**
 * Procedure controlling a Move action as described on page XX in the rulebook.
 */
object JumpStep : Procedure() {
    override val initialNode: Node = CheckTargetSquare

    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object CheckTargetSquare : ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            TODO("Not yet implemented")
        }
    }
}
