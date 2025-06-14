package com.jervisffb.engine.rules.bb2020.procedures.actions.block

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetPlayerState
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.SetContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.reports.ReportPowResult
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.KnockedDown
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.RiskingInjuryContext

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
            val context = state.getContext<BlockContext>()
            return if (context.defender.location.isOnField(rules)) {
                val injuryContext = RiskingInjuryContext(context.defender, context.isUsingMultiBlock)
                compositeCommandOf(
                    SetPlayerState(context.defender, PlayerState.KNOCKED_DOWN, hasTackleZones = false),
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
