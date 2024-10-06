package com.jervisffb.engine.rules.bb2020.procedures

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.Rules

object AnimalSavageryRoll: Procedure() {
    override val initialNode: Node = Dummy
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    object Dummy: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            return ExitProcedure()
        }

    }
}
