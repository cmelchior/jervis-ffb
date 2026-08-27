package com.jervisffb.engine.bb2025.procedures.inducements.desperatemeasures

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.Rules

/**
 * Responsible for applying the "Set Piece" Desperate Measure.
 *
 * See page 15 in Spike 22.
 */
object SportsEspionage: Procedure() {
    override val initialNode: Node = Dummy
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    object Dummy: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            return ExitProcedure()
        }
    }
}
