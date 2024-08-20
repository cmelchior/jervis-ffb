package dk.ilios.jervis.procedures.actions.block

import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.ComputationNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.rules.Rules

/**
 * Implement Follow-Up as described on page 59 in the rulebook.
 *
 * One case isn't described in the rulebook, that is when a series
 * of chain-pushes results in the vacant place being occupied again.
 *
 * In that case, the attacker cannot follow up. This should be okay
 * due to this remark:
 * "At other times, a player may be prevented from following-up even
 * if they want to, perhaps due to a Skill the target of the Block action
 * possesses for example. In such cases, rules that prevent a player
 * from following-up always take precedence".
 */
object FollowUp: Procedure() {

    override val initialNode: Node
        get() = TODO("Not yet implemented")

    override fun onEnterProcedure(state: Game, rules: Rules): Command? {
        TODO ("Check for block context")
        TODO("Not yet implemented")
    }

    override fun onExitProcedure(state: Game, rules: Rules): Command? {
        TODO("Not yet implemented")
    }

    object AutomaticFollowUp: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            TODO("Not yet implemented")
        }
    }

    object ChooseToFollowUp: ActionNode() {
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            TODO("Not yet implemented")
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            TODO("Not yet implemented")
        }
    }
}
