package com.jervisffb.engine.rules.bb2020.procedures

import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.tables.PrayerToNuffle
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

    // For FUMBBL, we need to check `inducementPrayersAvailableForUnderdog` and possible `inducementPrayersUseLeagueTable`
    object Inducements : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules) = BuyInducements
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
        override fun getChildProcedure(state: Game, rules: Rules) =
            com.jervisffb.engine.rules.bb2020.procedures.DetermineKickingTeam
        override fun onExitNode(state: Game, rules: Rules): Command {
            return ExitProcedure()
        }
    }
}
