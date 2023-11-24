package dk.ilios.bowlbot.procedures

import dk.ilios.bowlbot.commands.Command
import dk.ilios.bowlbot.commands.ExitProcedure
import dk.ilios.bowlbot.commands.GotoNode
import dk.ilios.bowlbot.fsm.Node
import dk.ilios.bowlbot.fsm.ParentNode
import dk.ilios.bowlbot.fsm.Procedure
import dk.ilios.bowlbot.model.Game
import dk.ilios.bowlbot.rules.Rules

object FullGame: Procedure() {
    override val initialNode: Node = PreGameSequence
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object PreGameSequence: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules) = PreGame
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(RunGame)
        }
    }

    object RunGame: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules) = GameHalf
        override fun onExitNode(state: Game, rules: Rules): Command {
            return if (state.halfNo < rules.halfsPrGame) {
                GotoNode(RunGame)
            } else {
                GotoNode(PostGameSequence)
            }
        }
    }

    object PostGameSequence: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules) = DummyProcedure
        override fun onExitNode(state: Game, rules: Rules): Command = ExitProcedure()
    }
}