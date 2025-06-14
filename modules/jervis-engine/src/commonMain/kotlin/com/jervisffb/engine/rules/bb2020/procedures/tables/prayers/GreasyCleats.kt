package com.jervisffb.engine.rules.bb2020.procedures.tables.prayers

import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.commands.AddPlayerStatModifier
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.SetContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.checkTypeAndValue
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.reports.ReportGameProgress
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.PrayersToNuffleRollContext
import com.jervisffb.engine.rules.bb2020.tables.PrayerStatModifier

/**
 * Procedure for handling the Prayer to Nuffle "Greasy Cleats" as described on page 39
 * of the rulebook.
 */
object GreasyCleats : Procedure() {
    override val initialNode: Node = SelectPlayer
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<PrayersToNuffleRollContext>()

    object SelectPlayer: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team? = null
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<PrayersToNuffleRollContext>()
            val availablePlayers = context.team.otherTeam()
                .filter { it.state == PlayerState.RESERVE || it.location.isOnField(rules) }
                .map { SelectPlayer(it) }

            return availablePlayers.ifEmpty {
                // This should only happen if _zero_ players are ready for the drive
                listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                Continue -> {
                    compositeCommandOf(
                        ReportGameProgress("No players are eligible to receive Greasy Cleats"),
                        ExitProcedure()
                    )
                }
                else -> {
                    checkTypeAndValue<PlayerSelected>(state, action) {
                        compositeCommandOf(
                            AddPlayerStatModifier(it.getPlayer(state), PrayerStatModifier.GREASY_CLEATS),
                            SetContext(state.getContext<PrayersToNuffleRollContext>().copy(resultApplied = true)),
                            ReportGameProgress("${it.getPlayer(state).name} received Greasy Cleats (-1 MA)"),
                            ExitProcedure()
                        )
                    }
                }
            }
        }
    }
}
