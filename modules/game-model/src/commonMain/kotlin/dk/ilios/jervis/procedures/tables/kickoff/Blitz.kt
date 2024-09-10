package dk.ilios.jervis.procedures.tables.kickoff

import compositeCommandOf
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.fsm.ComputationNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.reports.ReportGameProgress
import dk.ilios.jervis.rules.Rules

/**
 * Procedure for handling the Kick-Off Event: "Blitz" as described on page 41
 * of the rulebook.
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
