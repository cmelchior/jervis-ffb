package dk.ilios.jervis.procedures.bb2020.kickoff

import compositeCommandOf
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.commands.SetTurnMarker
import dk.ilios.jervis.fsm.ComputationNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.reports.ReportTimeout
import dk.ilios.jervis.rules.Rules

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
            val kickingTurnNo = state.kickingTeam.turnData.turnMarker
            val receivingTurnNo = state.receivingTeam.turnData.turnMarker
            return if (state.kickingTeam.turnData.turnMarker in 6..8) {
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
