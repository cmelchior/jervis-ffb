package com.jervisffb.engine.rules.bb2020.procedures.inducements

import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.SelectInducement
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.inducements.Timing
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.getAvailableAbilities
import com.jervisffb.engine.rules.bb2020.procedures.getAvailableCards
import com.jervisffb.engine.rules.bb2020.procedures.getAvailableSpells

data class ActivateInducementContext(
    val team: Team,
    val timing: Timing
): ProcedureContext

/**
 * This procedure is responsible for activating optional inducements at
 * a given trigger point in the game.
 *
 * A team might have many different types of inducements that all have
 * their own trigger. We need to find all the relevant ones and give
 * the player the option to activate them in any order they choose.
 */
object ActivateInducements : Procedure() {
    override val initialNode: Node = SelectInducement
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<ActivateInducementContext>()
    }

    object SelectInducement : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<ActivateInducementContext>().team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<ActivateInducementContext>()
            val currentTrigger = context.timing

            val inducements = context.team.run {
                val spells = wizards.getAvailableSpells(currentTrigger)
                val specialPlayCards = specialPlayCards.getAvailableCards(currentTrigger, state, rules)
                val infamousCoachAbilities = infamousCoachingStaff.getAvailableAbilities(currentTrigger, state, rules)
                spells + specialPlayCards + infamousCoachAbilities
            }.map { it.name }

            return if (inducements.isEmpty()) {
                listOf(ContinueWhenReady)
            } else {
                listOf(SelectInducement(inducements), CancelWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            TODO("Not yet implemented")
        }

    }
}
