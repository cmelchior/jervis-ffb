package com.jervisffb.engine.rules.common.procedures.actions.block

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.SetContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.context.ActivatePlayerContext
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.getResetPlayerTemporaryModifiersCommands
import com.jervisffb.engine.rules.common.skills.Duration

data class BreatheFireActionContext(
    val attacker: Player,
    val hasBreathed: Boolean = false,
): ProcedureContext

/**
 * Procedure for handling the Breathe Fire special action as described on
 * page 126 in the BB2025 rulebook.
 */
object BreatheFireAction : Procedure() {
    override val initialNode: Node = ResolveBreatheFire
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val activateContext = state.getContext<ActivatePlayerContext>()
        val context = BreatheFireActionContext(
            attacker = activateContext.player,
        )
        return SetContext(context)
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        val activatePlayerContext = state.getContext<ActivatePlayerContext>()
        val actionContext = state.getContext<BreatheFireActionContext>()
        return compositeCommandOf(
            SetContext(activatePlayerContext.copyWithMarkedAction(actionContext.hasBreathed)),
            RemoveContext<BreatheFireContext>(),
            RemoveContext<BreatheFireActionContext>(),
            *getResetPlayerTemporaryModifiersCommands(state, rules, activatePlayerContext.player, Duration.END_OF_ACTION),
        )
    }

    object ResolveBreatheFire: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val actionContext = state.getContext<BreatheFireActionContext>()
            val context = BreatheFireContext(
                attacker = actionContext.attacker
            )
            return SetContext(context)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = BreatheFireStep
        override fun onExitNode(state: Game, rules: Rules): Command {
            val actionContext = state.getContext<BreatheFireActionContext>()
            val breatheContext = state.getContext<BreatheFireContext>()
            return buildCompositeCommand {
                if (breatheContext.result != null) {
                    add(SetContext(actionContext.copy(
                        hasBreathed = true
                    )))
                }
                add(ExitProcedure())
            }
        }
    }
}
