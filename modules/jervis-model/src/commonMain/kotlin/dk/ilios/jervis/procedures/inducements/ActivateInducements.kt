package dk.ilios.jervis.procedures.inducements

import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.CancelWhenReady
import dk.ilios.jervis.actions.ContinueWhenReady
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.context.ProcedureContext
import dk.ilios.jervis.model.context.assertContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.model.inducements.Timing
import dk.ilios.jervis.procedures.getAvailableAbilities
import dk.ilios.jervis.procedures.getAvailableCards
import dk.ilios.jervis.procedures.getAvailableSpells
import dk.ilios.jervis.rules.Rules

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
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val context = state.getContext<ActivateInducementContext>()
            val currentTrigger = context.timing

            val inducements = context.team.run {
                val spells = wizards.getAvailableSpells(currentTrigger)
                val specialPlayCards = specialPlayCards.getAvailableCards(currentTrigger, state, rules)
                val infamousCoachAbilities = infamousCoachingStaff.getAvailableAbilities(currentTrigger, state, rules)
                spells + specialPlayCards + infamousCoachAbilities
            }.map { dk.ilios.jervis.actions.SelectInducement(it.name) } + listOf(CancelWhenReady)

            return inducements.ifEmpty {
                listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            TODO("Not yet implemented")
        }

    }
}
