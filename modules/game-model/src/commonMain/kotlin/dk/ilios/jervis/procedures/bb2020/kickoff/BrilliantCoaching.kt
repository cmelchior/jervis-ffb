package dk.ilios.jervis.procedures.bb2020.kickoff

import compositeCommandOf
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.fsm.ComputationNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.reports.LogCategory
import dk.ilios.jervis.reports.SimpleLogEntry
import dk.ilios.jervis.rules.Rules

/**
 * Procedure for handling the Kick-Off Event: "Brilliant Coaching" as described on page 41
 * of the rulebook.
 */
object BrilliantCoaching : Procedure() {
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
        // If Main Coach is banned, you get -1 to this roll. Page 63
        override fun apply(
            state: Game,
            rules: Rules,
        ): Command {
            return compositeCommandOf(
                SimpleLogEntry("Do Brilliant Coaching!", category = LogCategory.GAME_PROGRESS),
                ExitProcedure(),
            )
        }
    }
}
