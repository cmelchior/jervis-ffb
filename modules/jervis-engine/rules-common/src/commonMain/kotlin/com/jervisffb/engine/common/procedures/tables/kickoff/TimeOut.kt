package com.jervisffb.engine.common.procedures.tables.kickoff

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.common.commands.SetTurnMarker
import com.jervisffb.engine.common.reports.ReportTimeout
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.KickOffEventContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.builder.GameType
import com.jervisffb.engine.rules.builder.GameVersion

/**
 * Procedure for handling the Kick-Off Event: "Time-Out".
 *
 * See page 41 in the BB2020 rulebook.
 * See page 48 in the BB2025 rulebook.
 * See page 11 in Spike 22.
 */
object TimeOut : Procedure() {
    override val initialNode: Node = MoveTurnMarker
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object MoveTurnMarker : ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<KickOffEventContext>()
            val kickingTurnNo = state.kickingTeam.turnMarker
            val receivingTurnNo = state.receivingTeam.turnMarker
            // The exact range the timeout triggers changes slightly between rulesets
            val moveBackwardsRange = getMoveBackwardsRange(rules)
            return if (state.kickingTeam.turnMarker in moveBackwardsRange) {
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

    //
    // HELPER FUNCTIONS
    //
    private fun getMoveBackwardsRange(rules: Rules): IntRange {
        // In BB2020, Both Standard and BB7 define the exact range where a
        // Time-Out trigger, but it generalizes to the last 3 turns.
        // In BB2025, it is the last 3 turns for BB11 and the last two turns for
        // BB7.
        val lastTurns = when (rules.baseVersion) {
            GameVersion.BB2020 -> 3
            GameVersion.BB2025 -> {
                when (rules.gameType) {
                    GameType.STANDARD,
                    GameType.DUNGEON_BOWL,
                    GameType.GUTTER_BOWL -> 3
                    GameType.BB7 -> 2
                }
            }
        }
        return rules.turnsPrHalf - (lastTurns - 1) .. rules.turnsPrHalf
    }
}
