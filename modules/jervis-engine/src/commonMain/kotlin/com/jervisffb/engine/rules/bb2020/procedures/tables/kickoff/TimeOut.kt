package com.jervisffb.engine.rules.bb2020.procedures.tables.kickoff

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetTurnMarker
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.reports.ReportTimeout
import com.jervisffb.engine.rules.Rules

/**
 * Procedure for handling the Kick-Off Event: "Time-Out" as described on page 41
 * of the rulebook.
 */
object TimeOut : Procedure() {
    override val initialNode: Node = MoveTurnMarker
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object MoveTurnMarker : ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val kickingTurnNo = state.kickingTeam.turnMarker
            val receivingTurnNo = state.receivingTeam.turnMarker
            return if (state.kickingTeam.turnMarker in 6..8) {
                compositeCommandOf(
                    SetTurnMarker(state.kickingTeam, kickingTurnNo - 1),
                    SetTurnMarker(state.receivingTeam, receivingTurnNo - 1),
                    ReportTimeout(state, kickingTurnNo - 1, receivingTurnNo - 1, false),
                    ExitProcedure(),
                )
            } else {
                compositeCommandOf(
                    SetTurnMarker(state.kickingTeam, kickingTurnNo +1),
                    SetTurnMarker(state.receivingTeam, receivingTurnNo + 1),
                    ReportTimeout(state, kickingTurnNo + 1, receivingTurnNo + 1, false),
                    ExitProcedure(),
                )
            }
        }
    }
}
