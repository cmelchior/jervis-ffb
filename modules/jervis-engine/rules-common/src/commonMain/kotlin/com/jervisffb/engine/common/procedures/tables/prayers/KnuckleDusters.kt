package com.jervisffb.engine.common.procedures.tables.prayers

import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.commands.AddPlayerSkill
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
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
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Procedure for handling the Prayer to Nuffle "Knuckle Dusters".
 *
 *  * See page 39 in the BB2020 rulebook.
 *  * See page 143 in the BB2025 rulebook.
 */
object KnuckleDusters : Procedure() {
    override val initialNode: Node = ChoosePlayer
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<PrayersToNuffleRollContext>()
    }

    object ChoosePlayer : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<PrayersToNuffleRollContext>().team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<PrayersToNuffleRollContext>()
            val requestedAction = context.team
                // BB2020 Filters
                .filterNot { player ->
                    val validLocations = player.state == PlayerDogoutState.RESERVE || player.location.isOnPitch(rules)
                    rules.baseVersion == GameVersion.BB2020
                        && (
                            player.hasSkill(SkillType.LONER)
                                || player.hasSkill(SkillType.MIGHTY_BLOW)
                                || !validLocations
                        )
                }
                // BB20205 Filters
                .filterNot { player ->
                    rules.baseVersion == GameVersion.BB2025
                        && (player.type == PlayerType.STAR_PLAYER || player.hasSkill(SkillType.MIGHTY_BLOW))
                }
                .let {
                    when (it.isNotEmpty()) {
                        true -> SelectPlayer.fromPlayers(it)
                        false -> ContinueWhenReady
                    }
                }
            return listOf(requestedAction)
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                is Continue -> {
                    compositeCommandOf(
                        ReportGameProgress("No players are able to receive Iron Man"),
                        ExitProcedure(),
                    )
                }
                else -> {
                    castAction<PlayerSelected>(action) {
                        val context = state.getContext<PrayersToNuffleRollContext>()
                        val player = it.getPlayer(state)
                        compositeCommandOf(
                            AddPlayerSkill(
                                player = player,
                                skill = rules.createSkill(
                                    player = player,
                                    skill = SkillType.MIGHTY_BLOW.idAdjustment(1),
                                    expiresAt = context.result?.duration ?: INVALID_GAME_STATE("Missing duration: $context"),
                                ),
                            ),
                            ReportGameProgress("${player.name} received Knuckle Dusters"),
                            ExitProcedure(),
                        )
                    }
                }
            }
        }
    }
}
