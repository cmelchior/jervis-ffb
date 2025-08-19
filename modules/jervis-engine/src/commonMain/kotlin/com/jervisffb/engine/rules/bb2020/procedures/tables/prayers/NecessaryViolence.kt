package com.jervisffb.engine.rules.bb2020.procedures.tables.prayers

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.reports.ReportGameProgress
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.PrayersToNuffleRollContext

/**
 * Procedure for handling the Prayer to Nuffle "Necessary Violence" as described on page 39
 * of the rulebook.
 */
object NecessaryViolence : Procedure() {
    override val initialNode: Node = ApplyEvent
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<PrayersToNuffleRollContext>()
    }

    object ApplyEvent : ComputationNode() {
        // TODO Figure out how to do this
        override fun apply(
            state: Game,
            rules: Rules,
        ): Command {
            val context = state.getContext<PrayersToNuffleRollContext>()
            return compositeCommandOf(
                ReportGameProgress("${context.team.name} receives Necessary Violence"),
                ExitProcedure(),
            )
        }
    }
}
