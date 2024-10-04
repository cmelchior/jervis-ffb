package dk.ilios.jervis.procedures

import compositeCommandOf
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.commands.fsm.GotoNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.reports.ReportClosingGame
import dk.ilios.jervis.reports.ReportGameResult
import dk.ilios.jervis.reports.ReportGoingIntoExtraTime
import dk.ilios.jervis.reports.ReportStartingGame
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.Duration

object FullGame : Procedure() {
    override val initialNode: Node = PreGameSequence
    override fun onEnterProcedure(state: Game, rules: Rules): Command = ReportStartingGame(state, rules)
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        return ReportClosingGame(state)
    }

    object PreGameSequence : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules) = PreGame
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(RunGame)
        }
    }

    object RunGame : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules) = GameHalf
        override fun onExitNode(state: Game, rules: Rules): Command {
            return if (state.halfNo < rules.halfsPrGame) {
                GotoNode(RunGame)
            } else if (rules.hasExtraTime && state.homeScore == state.awayScore) {
                compositeCommandOf(
                    ReportGoingIntoExtraTime(state),
                    GotoNode(RunExtraTime),
                )
            } else {
                val resetCommands = getResetTemporaryModifiersCommands(state, rules, Duration.END_OF_GAME)
                return compositeCommandOf(
                    ReportGameResult(state, false, false),
                    *resetCommands,
                    GotoNode(PostGameSequence)
                )
            }
        }
    }

    object RunExtraTime: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ExtraTime
        override fun onExitNode(state: Game, rules: Rules): Command {
            val resetCommands = getResetTemporaryModifiersCommands(state, rules, Duration.END_OF_GAME)
            return compositeCommandOf(
                *resetCommands,
                GotoNode(PostGameSequence)
            )
        }
    }

    object PostGameSequence : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules) = DummyProcedure
        override fun onExitNode(state: Game, rules: Rules): Command = ExitProcedure()
    }
}
