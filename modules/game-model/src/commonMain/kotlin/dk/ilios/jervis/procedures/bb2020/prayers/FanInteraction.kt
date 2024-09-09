package dk.ilios.jervis.procedures.bb2020.prayers

import compositeCommandOf
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.fsm.ComputationNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.context.assertContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.procedures.PrayersToNuffleRollContext
import dk.ilios.jervis.reports.ReportGameProgress
import dk.ilios.jervis.rules.Rules

/**
 * Procedure for handling the Prayer to Nuffle "Fan Interaction" as described on page 39
 * of the rulebook.
 */
object FanInteraction : Procedure() {
    override val initialNode: Node = ApplyEvent
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<PrayersToNuffleRollContext>()
    }

    object ApplyEvent : ComputationNode() {
        // TODO Figure out how to do this
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<PrayersToNuffleRollContext>()
            return compositeCommandOf(
                ReportGameProgress("${context.team.name} receives Fan Interaction"),
                ExitProcedure(),
            )
        }
    }
}
