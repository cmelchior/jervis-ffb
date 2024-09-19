package dk.ilios.jervis.procedures.actions.block

import compositeCommandOf
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.RemoveContext
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.SetPlayerState
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.commands.fsm.GotoNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.PlayerState
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.procedures.tables.injury.KnockedDown
import dk.ilios.jervis.procedures.tables.injury.RiskingInjuryContext
import dk.ilios.jervis.reports.ReportPowResult
import dk.ilios.jervis.rules.Rules

object Pow: Procedure() {
    override val initialNode: Node = ResolvePush
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val pushContext = createPushContext(state)
        return compositeCommandOf(
            ReportPowResult(pushContext.firstPusher, pushContext.firstPushee),
            SetContext(pushContext)
        )
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        return RemoveContext<PushContext>()
    }

    // Push the player, including chain pushes
    object ResolvePush: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = PushStep

        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<BlockRollContext>()
            return if (context.defender.location.isOnField(rules)) {
                val injuryContext = RiskingInjuryContext(context.defender)
                compositeCommandOf(
                    SetPlayerState(context.defender, PlayerState.KNOCKED_DOWN),
                    SetContext(injuryContext),
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
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = KnockedDown
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                RemoveContext<RiskingInjuryContext>(),
                ExitProcedure()
            )
        }
    }
}
