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
import com.jervisffb.engine.rules.common.procedures.actions.blitz.BlitzAction
import com.jervisffb.engine.rules.common.procedures.getResetTemporaryModifiersCommands
import com.jervisffb.engine.rules.common.skills.Duration

data class StabActionContext(
    val attacker: Player,
    val hasStabbed: Boolean = false,
): ProcedureContext

/**
 * Procedure for handling Stab as a standalone special action.
 *
 * See page 86 in the BB2020 rulebook.
 * See page 136 in the BB2025 rulebook.
 *
 * [StabStep] contains the actual Stab logic and is shared by this class and
 * [BlitzAction].
 */
object StabAction : Procedure() {
    override val initialNode: Node = ResolveStab
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val activateContext = state.getContext<ActivatePlayerContext>()
        val context = StabActionContext(
            attacker = activateContext.player,
        )
        return SetContext(context)
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        val activatePlayerContext = state.getContext<ActivatePlayerContext>()
        val actionContext = state.getContext<StabActionContext>()
        return compositeCommandOf(
            SetContext(activatePlayerContext.copy(
                markActionAsUsed = (actionContext.hasStabbed))
            ),
            RemoveContext<StabContext>(),
            RemoveContext<StabActionContext>(),
            *getResetTemporaryModifiersCommands(state, rules, Duration.END_OF_ACTION),
        )
    }

    object ResolveStab: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val actionContext = state.getContext<StabActionContext>()
            val context = StabContext(
                attacker = actionContext.attacker
            )
            return SetContext(context)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = StabStep
        override fun onExitNode(state: Game, rules: Rules): Command {
            val actionContext = state.getContext<StabActionContext>()
            val stabContext = state.getContext<StabContext>()
            return buildCompositeCommand {
                if (stabContext.stabResult != null) {
                    add(SetContext(actionContext.copy(
                        hasStabbed = true
                    )))
                }
                add(ExitProcedure())
            }
        }
    }
}
