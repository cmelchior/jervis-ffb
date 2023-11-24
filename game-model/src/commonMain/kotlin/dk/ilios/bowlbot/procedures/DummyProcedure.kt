package dk.ilios.bowlbot.procedures

import dk.ilios.bowlbot.commands.Command
import dk.ilios.bowlbot.commands.ExitProcedure
import dk.ilios.bowlbot.fsm.ComputationNode
import dk.ilios.bowlbot.fsm.Node
import dk.ilios.bowlbot.fsm.Procedure
import dk.ilios.bowlbot.model.Game
import dk.ilios.bowlbot.rules.Rules

/**
 * Dummy procedure that does nothing.
 *
 * This can be used as a placeholder during development or testing.
 */
object DummyProcedure: Procedure() {
    override val initialNode: Node = Dummy
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    object Dummy: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command = ExitProcedure()
    }
}