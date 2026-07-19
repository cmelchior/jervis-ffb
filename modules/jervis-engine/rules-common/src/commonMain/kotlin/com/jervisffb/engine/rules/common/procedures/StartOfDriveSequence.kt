package com.jervisffb.engine.rules.common.procedures

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetActiveTeam
import com.jervisffb.engine.commands.SetCurrentBall
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.reports.ReportSetupKickingTeam
import com.jervisffb.engine.reports.ReportSetupReceivingTeam
import com.jervisffb.engine.reports.ReportStartingKickOff
import com.jervisffb.engine.rules.Rules

/**
 * This runs the Start of Drive procedure.
 * See page 40 in the rulebook.
 * See page 92 for Master Chef.
 * See page 79 for Leader.
 *
 * The sequence is:
 * 1. Setups: Kicking Team, then Receiving Team.
 * 2. Kick-Off.
 * 3. Roll for Master Chef (if start of half)
 * 4. Kick-Off Event
 */
object StartOfDriveSequence : Procedure() {
    override val initialNode: Node = SetupKickingTeam
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object SetupKickingTeam : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                AddContext(SetupTeamContext(state.kickingTeam)),
                ReportSetupKickingTeam(state.kickingTeam),
            )
        }
        override fun getChildProcedure(state: Game, rules: Rules) = SetupTeam
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                RemoveContext<SetupTeamContext>(),
                GotoNode(SetupReceivingTeam)
            )
        }
    }

    object SetupReceivingTeam : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules) = SetupTeam
        override fun onEnterNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                AddContext(SetupTeamContext(state.receivingTeam)),
                ReportSetupReceivingTeam(state.receivingTeam),
            )
        }
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                RemoveContext<SetupTeamContext>(),
                GotoNode(KickOff)
            )
        }
    }

    object KickOff : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules) = TheKickOff
        override fun onEnterNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                // Only one ball should exist at kick-off
                SetCurrentBall(state.balls.single()),
                ReportStartingKickOff(state.kickingTeam)
            )
        }
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(MasterChef)
        }
    }

    object MasterChef: ParentNode() {
        override fun skipNodeFor(state: Game, rules: Rules): Node? {
            // Only roll for master chef at the beginning of 1st and 2nd half
            return if (!rules.isStartOfHalf(state)) {
                KickOffEvent
            } else {
                null
            }
        }
        override fun getChildProcedure(state: Game, rules: Rules) = DummyProcedure
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(KickOffEvent)
        }
    }

    object KickOffEvent : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules) = TheKickOffEvent
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                SetCurrentBall(null),
                SetActiveTeam(state.receivingTeam),
                ExitProcedure(),
            )
        }
    }
}
