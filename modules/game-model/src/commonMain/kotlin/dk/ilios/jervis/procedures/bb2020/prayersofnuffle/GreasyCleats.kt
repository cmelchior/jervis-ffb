package dk.ilios.jervis.procedures.bb2020.prayersofnuffle

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.actions.SelectPlayer
import dk.ilios.jervis.commands.AddPlayerStatModifier
import dk.ilios.jervis.commands.AddPrayersToNuffle
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.PlayerState
import dk.ilios.jervis.model.context.assertContext
import dk.ilios.jervis.procedures.PrayersToNuffleRollContext
import dk.ilios.jervis.reports.SimpleLogEntry
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.tables.PrayerStatModifier
import dk.ilios.jervis.rules.tables.PrayerToNuffle

/**
 * Procedure for handling the Prayer of Nuffle "Greasy Cleats" as described on page 39
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
            return state.activeTeam.otherTeam()
                .filter { it.state == PlayerState.STANDING }
                .map { SelectPlayer(it) }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkType<PlayerSelected>(action) {
                return compositeCommandOf(
                    AddPlayerStatModifier(it.getPlayer(state), PrayerStatModifier.GREASY_CLEATS),
                    SimpleLogEntry("${it.getPlayer(state).name} received Greasy Cleats (-1 MA)"),
                    ExitProcedure()
                )
            }
        }
    }
}
