package dk.ilios.jervis.procedures

import compositeCommandOf
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.GotoNode
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.fsm.ComputationNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.context.ProcedureContext
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.tables.PrayerToNuffle
import kotlin.math.abs

/**
 * Context data required to track rolling on the Prayers of Nuffle.
 *
 * See XX in the rulebook.
 */
data class PrayersToNuffleRollContext(
    val team: Team,
    val rollsRemaining: Int,
    val result: PrayerToNuffle? = null,
    val resultApplied: Boolean = false
): ProcedureContext

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
            return GotoNode(CheckForPrayersToNuffle)
        }
    }

    object CheckForPrayersToNuffle: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val difference: Int = abs(state.homeTeam.teamValue - state.awayTeam.teamValue)
            val team = if (state.homeTeam.teamValue > state.awayTeam.teamValue) state.awayTeam else state.homeTeam
            val rolls = difference / 50_000 // 1 roll for every 50.000 TV difference
            return if (rolls > 0) {
                val context = PrayersToNuffleRollContext(team, rollsRemaining = rolls)
                return compositeCommandOf(
                    SetContext(context),
                    GotoNode(ThePrayersToNuffleTable)
                )
            } else {
                GotoNode(DetermineKickingTeam)
            }
        }
    }

    object ThePrayersToNuffleTable : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command? = null
        override fun getChildProcedure(state: Game, rules: Rules) = PrayersToNuffleRoll
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
