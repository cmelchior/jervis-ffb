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
import com.jervisffb.engine.model.context.ChainsawContext
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.getResetPlayerTemporaryModifiersCommands
import com.jervisffb.engine.rules.common.skills.Duration

data class ChainsawActionContext(
    val attacker: Player,
    val hasUsedChainsaw: Boolean = false,
): ProcedureContext

/**
 * Procedure for handling the Chainsaw special action as described on
 * page 126 in the BB2025 rulebook.
 */
object ChainsawAction : Procedure() {
    override val initialNode: Node = ResolveChainsaw
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val activateContext = state.getContext<ActivatePlayerContext>()
        val context = ChainsawActionContext(
            attacker = activateContext.player,
        )
        return AddContext(context)
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        val activatePlayerContext = state.getContext<ActivatePlayerContext>()
        val actionContext = state.getContext<ChainsawActionContext>()
        return compositeCommandOf(
            UpdateContext(activatePlayerContext.copyWithMarkedAction(actionContext.hasUsedChainsaw)),
            RemoveContext<ChainsawContext>(),
            RemoveContext<ChainsawActionContext>(),
            *getResetPlayerTemporaryModifiersCommands(state, rules, activatePlayerContext.player, Duration.END_OF_ACTION),
        )
    }

    object ResolveChainsaw: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val actionContext = state.getContext<ChainsawActionContext>()
            val context = ChainsawContext(
                attacker = actionContext.attacker,
                attackerOriginalCoordinates = actionContext.attacker.coordinates,
            )
            return AddContext(context)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ChainsawBlockStep
        override fun onExitNode(state: Game, rules: Rules): Command {
            val actionContext = state.getContext<ChainsawActionContext>()
            val chainsawContext = state.getContext<ChainsawContext>()
            return buildCompositeCommand {
                if (chainsawContext.kickbackRoll != null) {
                    add(UpdateContext(actionContext.copy(
                        hasUsedChainsaw = true
                    )))
                }
                add(ExitProcedure())
            }
        }
    }
}
