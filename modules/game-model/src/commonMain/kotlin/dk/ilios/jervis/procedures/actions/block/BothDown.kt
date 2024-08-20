package dk.ilios.jervis.procedures.actions.block

import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.rules.Rules

object BothDown: Procedure() {
    override val initialNode: Node
        get() = TODO("Not yet implemented")

    override fun onEnterProcedure(state: Game, rules: Rules): Command? {
        TODO("Not yet implemented")
    }

    override fun onExitProcedure(state: Game, rules: Rules): Command? {
        TODO("Not yet implemented")
    }
}
