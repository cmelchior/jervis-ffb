package dk.ilios.jervis.procedures.tables.kickoff

import compositeCommandOf
import dk.ilios.jervis.commands.AddBribe
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.fsm.ComputationNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.inducements.Bribe
import dk.ilios.jervis.reports.ReportGetTheRef
import dk.ilios.jervis.rules.Rules

/**
 * Procedure for handling the Kick-Off Event: "Get the Ref" as described on page 41
 * of the rulebook.
 */
object GetTheRef : Procedure() {
    override val initialNode: Node = GiveBribes
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object GiveBribes : ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            // Each team gets a free bribe, this allows them to go above the limit
            // of 3 when buying them as inducements
            return compositeCommandOf(
                AddBribe(state.homeTeam, Bribe()),
                AddBribe(state.awayTeam, Bribe()),
                ReportGetTheRef(state),
                ExitProcedure(),
            )
        }
    }
}
