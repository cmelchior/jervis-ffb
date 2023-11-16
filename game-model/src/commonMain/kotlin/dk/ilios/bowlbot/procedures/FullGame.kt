package dk.ilios.bowlbot.procedures

import compositeCommandOf
import dk.ilios.bowlbot.commands.Command
import dk.ilios.bowlbot.commands.ExitProcedure
import dk.ilios.bowlbot.commands.GotoNode
import dk.ilios.bowlbot.commands.SetActiveTeam
import dk.ilios.bowlbot.commands.SetDrive
import dk.ilios.bowlbot.commands.SetHalf
import dk.ilios.bowlbot.commands.SetKickingTeam
import dk.ilios.bowlbot.commands.SetKickingTeamAtHalfTime
import dk.ilios.bowlbot.commands.SetTurnNo
import dk.ilios.bowlbot.fsm.Node
import dk.ilios.bowlbot.fsm.ParentNode
import dk.ilios.bowlbot.fsm.Procedure
import dk.ilios.bowlbot.logs.ReportStartingHalf
import dk.ilios.bowlbot.model.Game
import dk.ilios.bowlbot.rules.Rules

object FullGame: Procedure {
    override val initialNode: Node = PreGameSequence

    object PreGameSequence: ParentNode() {
        override val childProcedure: Procedure = PreGame
        override fun onExit(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                GotoNode(RunGame)
            )
        }
    }

    object RunGame: ParentNode() {
        override val childProcedure: Procedure = GameHalf
        override fun onEnter(state: Game, rules: Rules): Command {
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
                SetActiveTeam(kickingTeam.otherTeam()),
                SetTurnNo(state.homeTeam, 0),
                SetTurnNo(state.awayTeam, 0),
                ReportStartingHalf(currentHalf)
            )
        }
        override fun onExit(state: Game, rules: Rules): Command {
            return if (state.halfNo < rules.halfsPrGame) {
                GotoNode(RunGame)
            } else {
                GotoNode(PostGameSequence)
            }
        }
    }

    object PostGameSequence: ParentNode() {
        override val childProcedure: Procedure = DummyProcedure
        override fun onExit(state: Game, rules: Rules): Command = ExitProcedure()
    }
}