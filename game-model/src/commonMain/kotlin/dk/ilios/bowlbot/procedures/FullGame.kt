package dk.ilios.bowlbot.procedures

import dk.ilios.bowlbot.commands.Command
import dk.ilios.bowlbot.commands.ExitProcedure
import dk.ilios.bowlbot.commands.GotoNode
import dk.ilios.bowlbot.commands.NoOpCommand
import dk.ilios.bowlbot.fsm.ActionNode
import dk.ilios.bowlbot.fsm.Node
import dk.ilios.bowlbot.fsm.ParentNode
import dk.ilios.bowlbot.fsm.Procedure
import dk.ilios.bowlbot.model.Game

object FullGame: Procedure {
    override val initialNode: Node = PreGame

    object PreGame: ParentNode() {
        override val childProcedure: Procedure = DummyProcedure
        override fun onExit(state: Game): Command = GotoNode(RunGame)
    }

    object RunGame: ParentNode() {
        override val childProcedure: Procedure = DummyProcedure
        override fun onExit(state: Game): Command = GotoNode(PostGame)
    }

    object PostGame: ParentNode() {
        override val childProcedure: Procedure = DummyProcedure
        override fun onExit(state: Game): Command = ExitProcedure()
    }
}