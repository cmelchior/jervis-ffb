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

data class ProjectileVomitActionContext(
    val attacker: Player,
    val hasVomited: Boolean = false,
): ProcedureContext

/**
 * Procedure for handling the Projectile Vomit special action as described on
 * page 133 in the BB2025 rulebook.
 */
object ProjectileVomitAction : Procedure() {
    override val initialNode: Node = ResolveProjectileVomit
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val activateContext = state.getContext<ActivatePlayerContext>()
        val context = ProjectileVomitActionContext(
            attacker = activateContext.player,
        )
        return AddContext(context)
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        val activatePlayerContext = state.getContext<ActivatePlayerContext>()
        val actionContext = state.getContext<ProjectileVomitActionContext>()
        return compositeCommandOf(
            UpdateContext(activatePlayerContext.copyWithMarkedAction(actionContext.hasVomited)),
            RemoveContext<ProjectileVomitContext>(),
            RemoveContext<ProjectileVomitActionContext>(),
            *getResetPlayerTemporaryModifiersCommands(state, rules, activatePlayerContext.player, Duration.END_OF_ACTION),
        )
    }

    object ResolveProjectileVomit: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val actionContext = state.getContext<ProjectileVomitActionContext>()
            val context = ProjectileVomitContext(
                attacker = actionContext.attacker
            )
            return AddContext(context)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ProjectileVomitStep
        override fun onExitNode(state: Game, rules: Rules): Command {
            val actionContext = state.getContext<ProjectileVomitActionContext>()
            val vomitContext = state.getContext<ProjectileVomitContext>()
            return buildCompositeCommand {
                if (vomitContext.injuryResult != null) {
                    add(UpdateContext(actionContext.copy(
                        hasVomited = true
                    )))
                }
                add(ExitProcedure())
            }
        }
    }
}
