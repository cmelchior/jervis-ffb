package com.jervisffb.engine.rules.common.procedures.actions.block

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.UpdateContext
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

data class ChompActionContext(
    val attacker: Player,
    val hasChomped: Boolean = false,
): ProcedureContext

/**
 * Procedure for handling the Chomp special action as described on
 * page 131 in the BB2025 rulebook.
 */
object ChompAction : Procedure() {
    override val initialNode: Node = ResolveChomp
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val activateContext = state.getContext<ActivatePlayerContext>()
        val context = ChompActionContext(
            attacker = activateContext.player,
        )
        return AddContext(context)
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        val activatePlayerContext = state.getContext<ActivatePlayerContext>()
        val actionContext = state.getContext<ChompActionContext>()
        return compositeCommandOf(
            UpdateContext(activatePlayerContext.copyWithMarkedAction(actionContext.hasChomped)),
            RemoveContext<ChompContext>(),
            RemoveContext<ChompActionContext>(),
            *getResetPlayerTemporaryModifiersCommands(state, rules, activatePlayerContext.player, Duration.END_OF_ACTION),
        )
    }

    object ResolveChomp: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val actionContext = state.getContext<ChompActionContext>()
            val context = ChompContext(
                attacker = actionContext.attacker,
            )
            return AddContext(context)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ChompStep
        override fun onExitNode(state: Game, rules: Rules): Command {
            val actionContext = state.getContext<ChompActionContext>()
            val chompContext = state.getContext<ChompContext>()
            return buildCompositeCommand {
                if (chompContext.chompRoll != null) {
                    add(UpdateContext(actionContext.copy(
                        hasChomped = true
                    )))
                }
                add(ExitProcedure())
            }
        }
    }
}
