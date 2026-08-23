package com.jervisffb.engine.common.procedures.tables.prayers

import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.actions.SelectSkill
import com.jervisffb.engine.actions.SkillSelected
import com.jervisffb.engine.commands.AddPlayerSkill
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.common.context.IntensiveTrainingContext
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
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Procedure for handling the Prayer to Nuffle "Intensive Training".
 *
 * See page 39 in the BB2020 rulebook.
 * See page 143 in the BB2025 rulebook.
 */
object IntensiveTraining : Procedure() {
    override val initialNode: Node = SelectPlayer
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<PrayersToNuffleRollContext>()

    object SelectPlayer : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<PrayersToNuffleRollContext>().team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val availablePlayers = state.getContext<PrayersToNuffleRollContext>().team
                // BB2020 Filters
                .filterNot { player ->
                    rules.baseVersion == GameVersion.BB2020
                        && player.state != PlayerDogoutState.RESERVE
                        && player.hasSkill(SkillType.LONER)
                }
                // BB20205 Filters
                .filterNot { player ->
                    rules.baseVersion == GameVersion.BB2025
                        && player.type == PlayerType.STAR_PLAYER
                }

            return when (availablePlayers.isNotEmpty()) {
                true -> listOf(com.jervisffb.engine.actions.SelectPlayer.fromPlayers(availablePlayers))
                false -> listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                is Continue -> {
                    compositeCommandOf(
                        ReportGameProgress("No players are able to receive Intensive Training"),
                        ExitProcedure(),
                    )
                }
                else -> {
                    castAction<PlayerSelected>(action) {
                        compositeCommandOf(
                            AddContext(IntensiveTrainingContext(it.getPlayer(state))),
                            GotoNode(SelectSkill)
                        )
                    }
                }
            }
        }
    }

    object SelectSkill : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<IntensiveTrainingContext>().player.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<IntensiveTrainingContext>()
            return listOf(SelectSkill(skills = context.player.position.primary.flatMap {
                rules.skillSettings.getAvailableSkillsIds(it)
            }))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castAction<SkillSelected>(action) {
                val prayerContext = state.getContext<PrayersToNuffleRollContext>()
                val context = state.getContext<IntensiveTrainingContext>()
                val duration = prayerContext.result?.duration ?: INVALID_GAME_STATE("No prayer result: $context")
                val skill = rules.createSkill(context.player, it.skill, expiresAt = duration)
                return compositeCommandOf(
                    RemoveContext(context),
                    AddPlayerSkill(context.player, skill),
                    ReportGameProgress("${context.player.name} receives ${skill.name} due to Intensive Training"),
                    ExitProcedure()
                )
            }
        }
    }
}
