package com.jervisffb.engine.bb2025.procedures.inducements.desperatemeasures

import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.bb2025.inducements.effects.Hangover
import com.jervisffb.engine.bb2025.modifiers.hangover
import com.jervisffb.engine.bb2025.reports.ReportHangover
import com.jervisffb.engine.commands.AddPlayerStatusEffect
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.commands.SetPlayerState
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.common.commands.RemoveSpecialPlayCard
import com.jervisffb.engine.common.context.ApplyInducementEffectsContext
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerDogoutState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.Dogout
import com.jervisffb.engine.model.modifiers.PlayerStatusEffect
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import com.jervisffb.engine.utils.requireGameState

/**
 * Responsible for applying the "Hangover" Desperate Measure.
 *
 * See page 15 in Spike 22.
 */
object Hangover: Procedure() {
    override val initialNode: Node = SelectPlayer
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        val context = state.getContext<ApplyInducementEffectsContext>()
        val team = context.selectedTeam ?: INVALID_GAME_STATE("Missing team")
        val card = context.selectedTeam?.specialPlayCards?.find { it is Hangover } ?: INVALID_GAME_STATE("Missing hangover inducement")
        return RemoveSpecialPlayCard(team, card)
    }
    override fun isValid(state: Game, rules: Rules) {
        val context = state.getContext<ApplyInducementEffectsContext>()
        requireGameState(context.selectedInducement is Hangover) { "Wrong inducement: $context" }
    }

    object SelectPlayer: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team {
            return state.getContext<ApplyInducementEffectsContext>().selectedTeam ?: INVALID_GAME_STATE("Missing team")
        }
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<ApplyInducementEffectsContext>()
            val team = context.selectedTeam?.otherTeam() ?: INVALID_GAME_STATE("Missing team")
            // All players can be used
            val eligiblePlayers = team.toList()
            return when (eligiblePlayers.isNotEmpty()) {
                true -> listOf(com.jervisffb.engine.actions.SelectPlayer.fromPlayers(eligiblePlayers))
                false -> listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                Continue -> ExitProcedure()
                is PlayerSelected -> {
                    val player = action.getPlayer(state)
                    compositeCommandOf(
                        SetPlayerLocation(player, Dogout),
                        SetPlayerState(player, PlayerDogoutState.RESERVE),
                        AddPlayerStatusEffect(player, PlayerStatusEffect.hangover(Duration.END_OF_DRIVE)),
                        ReportHangover(player),
                        ExitProcedure()
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }
}
