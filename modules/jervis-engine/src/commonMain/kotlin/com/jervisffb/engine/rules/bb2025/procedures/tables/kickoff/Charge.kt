package com.jervisffb.engine.rules.bb2025.procedures.tables.kickoff

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.reports.ReportGameProgress
import com.jervisffb.engine.rules.Rules

/**
 * Procedure for handling the Kick-Off Event: "Charge!" as described on page XX
 * of the rulebook.
 */
object Charge : Procedure() {
    override val initialNode: Node = GiveBribe

    override fun onEnterProcedure(
        state: Game,
        rules: Rules,
    ): Command? = null

    override fun onExitProcedure(
        state: Game,
        rules: Rules,
    ): Command? = null

    object GiveBribe : ComputationNode() {
        // TODO Figure out how to do this
        override fun apply(
            state: Game,
            rules: Rules,
        ): Command {
            return compositeCommandOf(
                ReportGameProgress("Do Charge!"),
                ExitProcedure(),
            )
        }
    }
}
