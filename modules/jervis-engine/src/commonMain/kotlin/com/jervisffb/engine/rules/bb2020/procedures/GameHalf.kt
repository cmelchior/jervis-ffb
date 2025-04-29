package com.jervisffb.engine.rules.bb2020.procedures

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.ResetAvailableTeamRerolls
import com.jervisffb.engine.commands.SetDrive
import com.jervisffb.engine.commands.SetHalf
import com.jervisffb.engine.commands.SetKickingTeamAtHalfTime
import com.jervisffb.engine.commands.SetTurnMarker
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.reports.ReportStartingDrive
import com.jervisffb.engine.reports.ReportStartingHalf
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.skills.Duration

object GameHalf : Procedure() {
    override val initialNode: Node = Drive
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val currentHalf = state.halfNo + 1
        // At start of game use the kicking team from the pre-game sequence, otherwise alternate teams based
        // on who kicked off at last half.
        var kickingTeam = state.kickingTeam
        if (currentHalf > 1) {
            kickingTeam = state.kickingTeamInLastHalf.otherTeam()
        }
        return compositeCommandOf(
            SetHalf(currentHalf),
            SetDrive(0),
            SetKickingTeamAtHalfTime(kickingTeam),
            ResetAvailableTeamRerolls(state.homeTeam),
            ResetAvailableTeamRerolls(state.awayTeam),
            SetTurnMarker(state.homeTeam, 0),
            SetTurnMarker(state.awayTeam, 0),
            ReportStartingHalf(currentHalf),
        )
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        // Remove modifiers that only last this half
        val resetCommands = getResetTemporaryModifiersCommands(
            state,
            rules,
            Duration.END_OF_HALF
        )
        return compositeCommandOf(
            *resetCommands
        )
    }

    object Drive : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules) = GameDrive
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val drive: Int = state.driveNo + 1
            return compositeCommandOf(
                SetDrive(drive),
                ReportStartingDrive(drive),
            )
        }

        override fun onExitNode(state: Game, rules: Rules): Command {
            // Both teams ran out of time
            return if (state.homeTeam.turnMarker == rules.turnsPrHalf && state.awayTeam.turnMarker == rules.turnsPrHalf) {
                ExitProcedure()
            } else {
                GotoNode(Drive)
            }
        }
    }
}
