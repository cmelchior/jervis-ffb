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
object PreGame: Procedure {
    override val initialNode: Node = TheFans

    object TheFans: ParentNode() {
        override val childProcedure: Procedure = DummyProcedure
        override fun onExit(state: Game, rules: Rules): Command {
            return GotoNode(TheWeather)
        }
    }

    object TheWeather: ParentNode() {
        override val childProcedure: Procedure = DummyProcedure
        override fun onExit(state: Game, rules: Rules): Command {
            return GotoNode(TakeOnJourneyMen)
        }
    }

    object TakeOnJourneyMen: ParentNode() {
        override val childProcedure: Procedure = DummyProcedure
        override fun onExit(state: Game, rules: Rules): Command {
            return GotoNode(Inducements)
        }
    }

    object Inducements: ParentNode() {
        override val childProcedure: Procedure = DummyProcedure
        override fun onExit(state: Game, rules: Rules): Command {
            return GotoNode(ThePrayersToNuffleTable)
        }
    }

    object ThePrayersToNuffleTable: ParentNode() {
        override val childProcedure: Procedure = DummyProcedure
        override fun onExit(state: Game, rules: Rules): Command {
            return GotoNode(DetermineKickingTeam)
        }
    }

    object DetermineKickingTeam: ParentNode() {
        override val childProcedure: Procedure = dk.ilios.bowlbot.procedures.DetermineKickingTeam
        override fun onExit(state: Game, rules: Rules): Command {
            return ExitProcedure()
        }
    }
}