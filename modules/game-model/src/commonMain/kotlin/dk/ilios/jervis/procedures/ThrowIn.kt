package dk.ilios.jervis.procedures

import compositeCommandOf
import dk.ilios.jervis.actions.Action
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.ContinueWhenReady
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.SetBallLocation
import dk.ilios.jervis.commands.SetBallState
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.ComputationNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.rules.Rules

/**
 * Resolve a Throw In after a ball went out of bounds.
 *
 * See page 51 in the rulebook.
 *
 * TODO Currently implementation just finds the first empty field
 */
object ThrowIn: Procedure() {
    override val initialNode: Node = SelectDirection
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    // TODO For now just select an empty field
    object SelectDirection: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val square = state.field.filter { it.isEmpty() }.random()
            return compositeCommandOf(
                SetBallLocation(square.coordinates),
                SetBallState.onGround(),
                ExitProcedure()
            )
        }
    }
}