package dk.ilios.bowlbot.procedures

import compositeCommandOf
import dk.ilios.bowlbot.commands.Command
import dk.ilios.bowlbot.commands.ExitProcedure
import dk.ilios.bowlbot.commands.GotoNode
import dk.ilios.bowlbot.commands.SetDrive
import dk.ilios.bowlbot.commands.SetHalf
import dk.ilios.bowlbot.commands.SetTurn
import dk.ilios.bowlbot.fsm.Node
import dk.ilios.bowlbot.fsm.ParentNode
import dk.ilios.bowlbot.fsm.Procedure
import dk.ilios.bowlbot.logs.ReportStartingHalf
import dk.ilios.bowlbot.model.Game
import dk.ilios.bowlbot.rules.Rules

object FullGame: Procedure {
    override val initialNode: Node = PreGame

    object PreGame: ParentNode() {
        override val childProcedure: Procedure = DummyProcedure
        override fun onExit(state: Game, rules: Rules): Command {
            return GotoNode(RunGame)
        }
    }

    object RunGame: ParentNode() {
        override val childProcedure: Procedure = GameHalf
        override fun onEnter(state: Game, rules: Rules): Command {
            val half = state.halfNo + 1
            return compositeCommandOf(
                SetHalf(half),
                SetDrive(0),
                SetTurn(state.homeTeam, 0),
                SetTurn(state.awayTeam, 0),
                ReportStartingHalf(half)
            )
        }
        override fun onExit(state: Game, rules: Rules): Command {
            return if (state.halfNo < rules.halfsPrGame) {
                GotoNode(RunGame)
            } else {
                GotoNode(PostGame)
            }
        }
    }

    object PostGame: ParentNode() {
        override val childProcedure: Procedure = DummyProcedure
        override fun onExit(state: Game, rules: Rules): Command = ExitProcedure()
    }
}