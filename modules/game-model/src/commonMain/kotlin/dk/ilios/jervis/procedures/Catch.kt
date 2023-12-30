package dk.ilios.jervis.procedures

import dk.ilios.jervis.actions.Action
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.ContinueWhenReady
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.rules.Rules

/**
 * Resolve a player attempting to catch the ball.
 *
 * This can be used as a placeholder during development or testing.
 */
object Catch: Procedure() {
    override val initialNode: Node = AttemptCatch
    override fun onEnterProcedure(state: Game, rules: Rules): Command? {
        // Check that this is only called on a standing player with tacklezones
        return null
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    object AttemptCatch: ActionNode() {
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            return listOf(ContinueWhenReady)
        }

        override fun applyAction(action: Action, state: Game, rules: Rules): Command {
            return ExitProcedure()
        }
    }
}