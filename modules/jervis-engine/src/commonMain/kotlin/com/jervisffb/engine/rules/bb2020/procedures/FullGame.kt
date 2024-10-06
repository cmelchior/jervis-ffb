package com.jervisffb.engine.rules.bb2020.procedures

import compositeCommandOf
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.reports.ReportClosingGame
import com.jervisffb.engine.reports.ReportGameResult
import com.jervisffb.engine.reports.ReportGoingIntoExtraTime
import com.jervisffb.engine.reports.ReportStartingGame
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.skills.Duration

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
