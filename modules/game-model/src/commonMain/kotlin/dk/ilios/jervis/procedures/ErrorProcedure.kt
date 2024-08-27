package dk.ilios.jervis.procedures

import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.fsm.ComputationNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.utils.INVALID_GAME_STATE

/**
 * Dummy procedure that throws an error if called.
 *
 * This can be used as a placeholder during development or testing.
 */
object ErrorProcedure : Procedure() {
    override val initialNode: Node = Dummy

    override fun onEnterProcedure(
        state: Game,
        rules: Rules,
    ): Command? {
        INVALID_GAME_STATE("Error: This procedure should not be called.")
    }

    override fun onExitProcedure(
        state: Game,
        rules: Rules,
    ): Command? = null

    object Dummy : ComputationNode() {
        override fun apply(
            state: Game,
            rules: Rules,
        ): Command = ExitProcedure()
    }
}
