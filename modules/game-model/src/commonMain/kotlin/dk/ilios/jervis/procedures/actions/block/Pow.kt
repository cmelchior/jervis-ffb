package dk.ilios.jervis.procedures.actions.block

import compositeCommandOf
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.GotoNode
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.SetPlayerState
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.PlayerState
import dk.ilios.jervis.procedures.injury.RiskingInjuryRoll
import dk.ilios.jervis.procedures.injury.RiskingInjuryRollContext
import dk.ilios.jervis.reports.ReportPowResult
import dk.ilios.jervis.rules.Rules

object Pow: Procedure() {
    override val initialNode: Node = ResolvePush

    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val newContext = createPushContext(state)
        return SetContext(Game::pushContext, newContext)
    }

    override fun onExitProcedure(state: Game, rules: Rules): Command? {
        val context = state.pushContext!!
        return compositeCommandOf(
            SetContext(Game::pushContext, null),
            ReportPowResult(context.pusher, context.pushee)
        )
    }

    // Push the player, including chain pushes
    object ResolvePush: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = PushStep

        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.blockRollResultContext!!
            return if (context.defender.location.isOnField(rules)) {
                val injuryContext = RiskingInjuryRollContext(context.defender)
                compositeCommandOf(
                    SetPlayerState(context.defender, PlayerState.KNOCKED_DOWN),
                    SetContext(Game::riskingInjuryRollsContext, injuryContext),
                    GotoNode(ResolvePlayerDown)
                )
            } else {
                ExitProcedure()
            }
        }
    }

    // If the player is still on the field, resolve them going down.
    // Otherwise, it was resolved as part of the Chain Push
    object ResolvePlayerDown: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            return RiskingInjuryRoll
        }

        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                SetContext(Game::riskingInjuryRollsContext, null),
                ExitProcedure()
            )
        }
    }

}
