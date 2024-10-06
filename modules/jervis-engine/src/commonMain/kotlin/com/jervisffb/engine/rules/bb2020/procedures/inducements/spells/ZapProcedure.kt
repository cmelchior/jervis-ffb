package com.jervisffb.engine.rules.bb2020.procedures.inducements.spells

import com.jervisffb.engine.actions.ActionDescriptor
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.bb2020.procedures.inducements.ActivateInducementContext
import com.jervisffb.engine.rules.Rules

/**
 * Procedure handling the effect of using the "Zap!" Spell available to
 * Hireling Sports Wizards.
 */
object ZapProcedure: Procedure() {
    override val initialNode: Node = SelectPlayer
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        TODO()
    }
    override fun isValid(state: Game, rules: Rules) = state.assertContext<ActivateInducementContext>()

    object SelectPlayer : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<ActivateInducementContext>().team
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            TODO("Not yet implemented")
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            TODO("Not yet implemented")
        }
    }
}
