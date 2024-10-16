package com.jervisffb.engine.rules.bb2020.procedures.tables.kickoff

import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.AddBribe
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.inducements.Bribe
import com.jervisffb.engine.reports.ReportGetTheRef
import com.jervisffb.engine.rules.Rules

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
