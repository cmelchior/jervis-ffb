package dk.ilios.jervis.procedures.bb2020.prayers

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.Continue
import dk.ilios.jervis.actions.ContinueWhenReady
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.actions.SelectPlayer
import dk.ilios.jervis.commands.AddPlayerSkill
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.PlayerState
import dk.ilios.jervis.model.context.assertContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.model.hasSkill
import dk.ilios.jervis.procedures.PrayersToNuffleRollContext
import dk.ilios.jervis.reports.LogCategory
import dk.ilios.jervis.reports.SimpleLogEntry
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.Loner
import dk.ilios.jervis.rules.skills.Duration
import dk.ilios.jervis.rules.skills.Stab

/**
 * Procedure for handling the Prayer to Nuffle "Stiletto" as described on page 39
 * of the rulebook.
 */
object Stiletto : Procedure() {
    override val initialNode: Node = ChoosePlayer
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<PrayersToNuffleRollContext>()
    }

    object ChoosePlayer : ActionNode() {
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val context = state.getContext<PrayersToNuffleRollContext>()
            val availablePlayers = context.team
                .filter { it.state == PlayerState.STANDING }
                .filter { !it.hasSkill<Loner>() && !it.hasSkill<Stab>() }
                .map { SelectPlayer(it) }

            return availablePlayers.ifEmpty {
                listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                is Continue -> {
                   compositeCommandOf(
                       SimpleLogEntry("No players are able to receive Stiletto", category = LogCategory.GAME_PROGRESS),
                       ExitProcedure(),
                   )
                }
                else -> {
                    checkTypeAndValue<PlayerSelected>(state, rules, action, this) {
                        val context = state.getContext<PrayersToNuffleRollContext>()
                        val player = it.getPlayer(state)
                        return compositeCommandOf(
                            AddPlayerSkill(player, Stab(isTemporary = true, expiresAt = Duration.END_OF_DRIVE)),
                            SetContext(context.copy(resultApplied = true)),
                            SimpleLogEntry("${player.name} received Stiletto", category = LogCategory.GAME_PROGRESS),
                            ExitProcedure(),
                        )
                    }
                }
            }
        }
    }
}
