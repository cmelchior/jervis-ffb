package dk.ilios.jervis.procedures.bb2020.prayers

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.Continue
import dk.ilios.jervis.actions.ContinueWhenReady
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.actions.SelectPlayer
import dk.ilios.jervis.commands.AddPlayerStatModifier
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.PlayerState
import dk.ilios.jervis.model.context.assertContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.procedures.PrayersToNuffleRollContext
import dk.ilios.jervis.reports.ReportGameProgress
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.tables.PrayerStatModifier

/**
 * Procedure for handling the Prayer to Nuffle "Greasy Cleats" as described on page 39
 * of the rulebook.
 */
object GreasyCleats : Procedure() {
    override val initialNode: Node = SelectPlayer
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<PrayersToNuffleRollContext>()
    }

    object SelectPlayer: ActionNode() {
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val context = state.getContext<PrayersToNuffleRollContext>()
            val availablePlayers = context.team.otherTeam()
                .filter { it.state == PlayerState.STANDING }
                .map { SelectPlayer(it) }

            return availablePlayers.ifEmpty {
                // This should only happen if _zero_ players are ready for the drive
                listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when(action) {
                Continue -> {
                   compositeCommandOf(
                       ReportGameProgress("No players are eligible to receive Greasy Cleats"),
                       ExitProcedure()
                   )
                }
                else -> {
                    checkTypeAndValue<PlayerSelected>(state, rules, action, this) {
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
