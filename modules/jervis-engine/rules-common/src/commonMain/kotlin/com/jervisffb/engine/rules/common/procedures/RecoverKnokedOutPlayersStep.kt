package com.jervisffb.engine.rules.common.procedures

import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetPlayerState
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerDogoutState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.reports.ReportRecoverPlayer
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf

data class RecoverKnockedOutPlayersContext(
    val playersHandled: PersistentSet<Player> = persistentSetOf(),
    val selectedPlayer: Player? = null,
    val recoverRoll: D6DieRoll? = null,
    val modifiers: List<DiceModifier> = emptyList(),
    val isSuccess: Boolean = false,
): ProcedureContext {
    fun reset(playerHandled: Player): RecoverKnockedOutPlayersContext {
        return this.copy(
            playersHandled = playersHandled.add(playerHandled),
            selectedPlayer = null,
            recoverRoll = null,
            modifiers = emptyList(),
            isSuccess = false,
        )
    }
}

/**
 * This procedure controls the "Recover Knocked-out Players" step that is part
 * of the End of Drive sequence.
 *
 * See page 83 in the BB2025 rulebook.
 *
 * Developer's Commentary:
 * The rulebook doesn't specify which order the players are rolled. However,
 * the order doesn't matter much given that there are no known optional effects
 * that can affect the outcome (Blitzer's Keg is required to use if present).
 *
 * But to future-proof the action protocol, we also let it be up to coaches
 * to determine the order. Similarly to how it is done by
 * [DealWithSecretWeaponsStep].
 *
 * To make the UX easier, selecting the player should be done automatically by
 * the UI, so if it ever turns out that the order matters, we only need to
 * modify the UI code.
 */
object RecoverKnockedOutPlayersStep: Procedure() {
    override val initialNode = ReceivingTeamSelectPlayerToRecover
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        return AddContext(RecoverKnockedOutPlayersContext())
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        return RemoveContext<RecoverKnockedOutPlayersContext>()
    }

    object ReceivingTeamSelectPlayerToRecover: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.receivingTeam
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<RecoverKnockedOutPlayersContext>()
            val availablePlayers = getPlayersToRecover(state.receivingTeam, context)
            return when (availablePlayers.isNotEmpty()) {
                true -> listOf(SelectPlayer.fromPlayers(availablePlayers))
                false -> listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<RecoverKnockedOutPlayersContext>()
            return when (action) {
                Continue -> GotoNode(KickingTeamSelectPlayerToRecover)
                is PlayerSelected -> {
                    compositeCommandOf(
                        UpdateContext(context.copy(selectedPlayer = action.getPlayer(state))),
                        GotoNode(RollToRecover),
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object KickingTeamSelectPlayerToRecover: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.kickingTeam
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<RecoverKnockedOutPlayersContext>()
            val availablePlayers = getPlayersToRecover(state.kickingTeam, context)
            return when (availablePlayers.isNotEmpty()) {
                true -> listOf(SelectPlayer.fromPlayers(availablePlayers))
                false -> listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<RecoverKnockedOutPlayersContext>()
            return when (action) {
                Continue -> ExitProcedure()
                is PlayerSelected -> {
                    compositeCommandOf(
                        UpdateContext(context.copy(selectedPlayer = action.getPlayer(state))),
                        GotoNode(RollToRecover),
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object RollToRecover: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = RecoverPlayerRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<RecoverKnockedOutPlayersContext>()
            val player = context.selectedPlayer ?: INVALID_GAME_STATE("Missing selected player")
            return compositeCommandOf(
                ReportRecoverPlayer(player, context.isSuccess),
                UpdateContext(context.reset(player)),
                if (context.isSuccess) SetPlayerState(player, PlayerDogoutState.RESERVE) else null,
                GotoNode(if (player.team == state.receivingTeam) ReceivingTeamSelectPlayerToRecover else KickingTeamSelectPlayerToRecover),
            )
        }
    }

    // HELPER FUNCTION

    private fun getPlayersToRecover(team: Team, context: RecoverKnockedOutPlayersContext): List<Player> {
        return team
            .filter { it.state == PlayerDogoutState.KNOCKED_OUT }
            .filter { it !in context.playersHandled }
    }
}
