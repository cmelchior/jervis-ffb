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
import dk.ilios.jervis.commands.fsm.ExitProcedure
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
import dk.ilios.jervis.reports.ReportGameProgress
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.Duration
import dk.ilios.jervis.rules.skills.Loner
import dk.ilios.jervis.rules.skills.Pro

/**
 * Procedure for handling the Prayer to Nuffle "Blessed Statue of Nuffle" as described on page 39
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
            val context = state.getContext<PrayersToNuffleRollContext>()
            val availablePlayers = context.team
                .filter { it.state == PlayerState.STANDING }
                .filter { !it.hasSkill<Loner>() && !it.hasSkill<Pro>() }
                .map { SelectPlayer(it) }

            return availablePlayers.ifEmpty {
                listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                is Continue -> {
                    compositeCommandOf(
                        ReportGameProgress("No players are able to receive Blessed Statue of Nuffle"),
                        ExitProcedure(),
                    )
                }
                else -> {
                    return checkTypeAndValue<PlayerSelected>(state, rules, action, this) {
                        val context = state.getContext<PrayersToNuffleRollContext>()
                        val player = it.getPlayer(state)
                        compositeCommandOf(
                            AddPlayerSkill(player, Pro(isTemporary = true, expiresAt = Duration.END_OF_GAME)),
                            SetContext(context.copy(resultApplied = true)),
                            ReportGameProgress("${player.name} received Blessed Statue of Nuffle (Pro)"),
                            ExitProcedure(),
                        )
                    }
                }
            }        }
    }
}
