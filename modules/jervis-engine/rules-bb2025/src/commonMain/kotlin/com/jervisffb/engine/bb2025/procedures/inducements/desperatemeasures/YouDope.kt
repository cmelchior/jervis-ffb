package com.jervisffb.engine.bb2025.procedures.inducements.desperatemeasures

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.Rules

/**
 * Responsible for applying the "You Dope!" Desperate Measure.
 *
 * See page 15 in Spike 22.
 */
object YouDope: Procedure() {
    override val initialNode: Node
        get() = TODO("Not yet implemented")

    override fun onEnterProcedure(state: Game, rules: Rules): Command? {
        TODO("Not yet implemented")
    }

    override fun onExitProcedure(state: Game, rules: Rules): Command? {
        TODO("Not yet implemented")
    }
}
