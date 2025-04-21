package com.jervisffb.engine.rules.bb2020.procedures.tables.kickoff

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
 * Procedure for handling the Kick-Off Event: "Blitz" as described on page 41
 * of the rulebook.
 *
 * Also supports the BB7 variant of the event, which is described on page 94 in Death Zone.
 */
object Blitz : Procedure() {
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
                ReportGameProgress("Do Blitz!"),
                ExitProcedure(),
            )
        }
    }
}
