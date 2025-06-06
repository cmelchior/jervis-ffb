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
import com.jervisffb.engine.rules.bb2020.skills.Pro
import com.jervisffb.engine.rules.bb2020.skills.SkillType

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
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<PrayersToNuffleRollContext>().team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<PrayersToNuffleRollContext>()
            val availablePlayers = context.team
                .filter { it.state == PlayerState.RESERVE || it.location.isOnField(rules) }
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
                    return checkTypeAndValue<PlayerSelected>(state, action) {
                        val context = state.getContext<PrayersToNuffleRollContext>()
                        val player = it.getPlayer(state)
                        compositeCommandOf(
                            AddPlayerSkill(
                                player,
                                rules.createSkill(player, SkillType.PRO.id(), Duration.END_OF_GAME)
                            ),
                            SetContext(context.copy(resultApplied = true)),
                            ReportGameProgress("${player.name} received Blessed Statue of Nuffle (Pro)"),
                            ExitProcedure(),
                        )
                    }
                }
            }
        }
    }
}
