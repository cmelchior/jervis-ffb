package dk.ilios.jervis.procedures

import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.GotoNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.rules.Rules

/**
 * This procedure is responsible for managing the Pregame sequence.
 *
 * See page 37 in the rulebook.
 */
object PreGame : Procedure() {
    override val initialNode: Node = TheFans

    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object TheFans : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules) = FanFactorRolls
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(TheWeather)
        }
    }

    object TheWeather : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules) = WeatherRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(TakeOnJourneyMen)
        }
    }

    object TakeOnJourneyMen : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules) = DummyProcedure
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(Inducements)
        }
    }

    object Inducements : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules) = DummyProcedure
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(ThePrayersToNuffleTable)
        }
    }

    object ThePrayersToNuffleTable : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules) = DummyProcedure
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(DetermineKickingTeam)
        }
    }

    object DetermineKickingTeam : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules) = dk.ilios.jervis.procedures.DetermineKickingTeam
        override fun onExitNode(state: Game, rules: Rules): Command {
            return ExitProcedure()
        }
    }
}
