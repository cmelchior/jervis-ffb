package dk.ilios.jervis.procedures

import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.fsm.ComputationNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.rules.Rules

/**
 * Procedure controlling a Move action as described on page XX in the rulebook.
 */
object JumpStep : Procedure() {
    override val initialNode: Node = CheckTargetSquare

    override fun onEnterProcedure(
        state: Game,
        rules: Rules,
    ): Command? = null

    override fun onExitProcedure(
        state: Game,
        rules: Rules,
    ): Command? = null

    object CheckTargetSquare : ComputationNode() {
        override fun apply(
            state: Game,
            rules: Rules,
        ): Command {
            TODO("Not yet implemented")
        }
    }
}
