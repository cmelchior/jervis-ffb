package com.jervisffb.engine.common.procedures.tables.prayers

import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.common.commands.AddPlayerStatModifier
import com.jervisffb.engine.common.context.PrayersToNuffleRollContext
import com.jervisffb.engine.common.reports.ReportGameProgress
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.castAction
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerDogoutState
import com.jervisffb.engine.model.PlayerType
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.hasSkill
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.builder.GameVersion
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.rules.common.tables.GreasyCleatsStatModifier
import com.jervisffb.engine.utils.INVALID_GAME_STATE

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
            val requestedAction = context.team.otherTeam()
                // BB2020 Filters
                .filterNot { player ->
                    val validLocations = player.state == PlayerDogoutState.RESERVE || player.location.isOnPitch(rules)
                    rules.baseVersion == GameVersion.BB2020
                        && (
                            player.hasSkill(SkillType.LONER)
                                || !validLocations
                        )
                }
                // BB20205 Filters
                .filterNot { player ->
                    rules.baseVersion == GameVersion.BB2025
                        && (player.type == PlayerType.STAR_PLAYER)
                }
                .let {
                    when (it.isNotEmpty()) {
                        true -> com.jervisffb.engine.actions.SelectPlayer.fromPlayers(it)
                        false -> {
                            // This should only happen if _zero_ players are ready for the drive
                            ContinueWhenReady
                        }
                    }
                }
            return listOf(requestedAction)
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
                    val context = state.getContext<PrayersToNuffleRollContext>()
                    val duration = context.result?.duration ?: INVALID_GAME_STATE("Missing result: $context")
                    castAction<PlayerSelected>(action) {
                        compositeCommandOf(
                            AddPlayerStatModifier(it.getPlayer(state), GreasyCleatsStatModifier(duration)),
                            UpdateContext(state.getContext<PrayersToNuffleRollContext>().copy(resultApplied = true)),
                            ReportGameProgress("${it.getPlayer(state).name} received Greasy Cleats (-1 MA)"),
                            ExitProcedure()
                        )
                    }
                }
            }
        }
    }
}
