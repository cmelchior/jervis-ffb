package dk.ilios.jervis.procedures.bb2020.prayersofnuffle

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.actions.SelectPlayer
import dk.ilios.jervis.commands.AddPlayerSkill
import dk.ilios.jervis.commands.AddPrayersToNuffle
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.PlayerState
import dk.ilios.jervis.model.context.assertContext
import dk.ilios.jervis.model.hasSkill
import dk.ilios.jervis.procedures.PrayersToNuffleRollContext
import dk.ilios.jervis.reports.SimpleLogEntry
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.Loner
import dk.ilios.jervis.rules.skills.Pro
import dk.ilios.jervis.rules.skills.ResetPolicy
import dk.ilios.jervis.rules.tables.PrayerToNuffle

/**
 * Procedure for handling the Prayer of Nuffle "Blessed Statue of Nuffle" as described on page 39
 * of the rulebook.
 */
object BlessedStatueOfNuffle : Procedure() {
    override val initialNode: Node = SelectPlayer
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<PrayersToNuffleRollContext>()
    }

    object SelectPlayer: ActionNode() {
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            return state.activeTeam
                .filter { it.state == PlayerState.STANDING && !it.hasSkill<Loner>() }
                .map { SelectPlayer(it) }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkType<PlayerSelected>(action) {
                val player = it.getPlayer(state)
                return compositeCommandOf(
                    AddPlayerSkill(player, Pro(isTemporary = true, expiresAt = ResetPolicy.END_OF_GAME)),
                    SimpleLogEntry("${player.name} received Blessed Statue of Nuffle (Pro)"),
                    ExitProcedure()
                )
            }
        }
    }
}
