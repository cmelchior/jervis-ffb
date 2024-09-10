package dk.ilios.jervis.procedures.inducements.miscellaneousmayhem

import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.context.assertContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.procedures.inducements.ActivateInducementContext
import dk.ilios.jervis.rules.Rules

/**
 * Procedure handling the effect of using the "Assassination Attempt" Miscellaneous Mayhem card.
 */
object AssassinationAttempt: Procedure() {
    override val initialNode: Node = SelectPlayer
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        TODO()
    }
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<ActivateInducementContext>()
    }

    object SelectPlayer : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<ActivateInducementContext>().team
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            TODO("Not yet implemented")
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            TODO("Not yet implemented")
        }
    }

    object PlacePlayer : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<ActivateInducementContext>().team
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            TODO("Not yet implemented")
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            TODO("Not yet implemented")
        }
    }
}
