package com.jervisffb.engine.rules.bb2020.procedures.tables.prayers

import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.commands.AddPlayerSkill
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetContext
import com.jervisffb.engine.commands.compositeCommandOf
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
import com.jervisffb.engine.model.hasSkill
import com.jervisffb.engine.reports.ReportGameProgress
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.PrayersToNuffleRollContext
import com.jervisffb.engine.rules.bb2020.skills.Duration
import com.jervisffb.engine.rules.bb2020.skills.Loner
import com.jervisffb.engine.rules.bb2020.skills.Stab

/**
 * Procedure for handling the Prayer to Nuffle "Stiletto" as described on page 39
 * of the rulebook.
 */
object Stiletto : Procedure() {
    override val initialNode: Node = ChoosePlayer
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<PrayersToNuffleRollContext>()

    object ChoosePlayer : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<PrayersToNuffleRollContext>().team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<PrayersToNuffleRollContext>()
            val availablePlayers = context.team
                .filter { it.state == PlayerState.RESERVE  || it.location.isOnField(rules) }
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
                        ReportGameProgress("No players are able to receive Stiletto"),
                        ExitProcedure(),
                    )
                }
                else -> {
                    checkTypeAndValue<PlayerSelected>(state, rules, action) {
                        val context = state.getContext<PrayersToNuffleRollContext>()
                        val player = it.getPlayer(state)
                        return compositeCommandOf(
                            AddPlayerSkill(player,
                                Stab(
                                    isTemporary = true,
                                    expiresAt = Duration.END_OF_DRIVE
                                )
                            ),
                            SetContext(context.copy(resultApplied = true)),
                            ReportGameProgress("${player.name} received Stiletto"),
                            ExitProcedure(),
                        )
                    }
                }
            }
        }
    }
}
