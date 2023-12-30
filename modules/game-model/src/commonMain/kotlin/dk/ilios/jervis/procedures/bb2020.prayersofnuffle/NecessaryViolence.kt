package dk.ilios.jervis.procedures.bb2020.prayersofnuffle

import compositeCommandOf
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.fsm.ComputationNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.reports.LogCategory
import dk.ilios.jervis.reports.SimpleLogEntry
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.rules.Rules

/**
 * Procedure for handling the Prayer of Nuffle "Necessary Violence" as described on page 39
 * of the rulebook.
 */
object NecessaryViolence: Procedure() {
    override val initialNode: Node = ApplyEvent
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object ApplyEvent: ComputationNode() {
        // TODO Figure out how to do this
        override fun apply(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                SimpleLogEntry("Do Necessary Violence!", category = LogCategory.GAME_PROGRESS),
                ExitProcedure()
            )
        }
    }
}