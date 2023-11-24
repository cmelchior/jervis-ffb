package dk.ilios.bowlbot.procedures

import dk.ilios.bowlbot.commands.Command
import dk.ilios.bowlbot.commands.ExitProcedure
import dk.ilios.bowlbot.commands.GotoNode
import dk.ilios.bowlbot.fsm.Node
import dk.ilios.bowlbot.fsm.ParentNode
import dk.ilios.bowlbot.fsm.Procedure
import dk.ilios.bowlbot.model.Game
import dk.ilios.bowlbot.rules.Rules

/**
 * Page 37 in the rulebook.
 */
object PreGame: Procedure() {
    override val initialNode: Node = TheFans
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object TheFans: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules) = DummyProcedure
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(TheWeather)
        }
    }

    object TheWeather: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules) = DummyProcedure
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(TakeOnJourneyMen)
        }
    }

    object TakeOnJourneyMen: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules) = DummyProcedure
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(Inducements)
        }
    }

    object Inducements: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules) = DummyProcedure
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(ThePrayersToNuffleTable)
        }
    }

    object ThePrayersToNuffleTable: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules) = DummyProcedure
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(DetermineKickingTeam)
        }
    }

    object DetermineKickingTeam: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules) = dk.ilios.bowlbot.procedures.DetermineKickingTeam
        override fun onExitNode(state: Game, rules: Rules): Command {
            return ExitProcedure()
        }
    }
}