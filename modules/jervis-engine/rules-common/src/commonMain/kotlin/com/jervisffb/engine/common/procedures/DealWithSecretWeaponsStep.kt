package com.jervisffb.engine.common.procedures

import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.commands.Command
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
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.common.reports.ReportSpottedByRef
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.common.procedures.actions.foul.BeingSentOff
import com.jervisffb.engine.common.procedures.actions.foul.BeingSentOffContext
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE

data class DealWithSecretWeaponsContext(
    val selectedPlayer: Player? = null,
): ProcedureContext

/**
 * This procedure controls the Deal with Secret Weapons step that is part of
 * the End of Drive sequence.
 *
 * See page 82 in the BB2025 rulebook.
 *
 * Developer's Commentary:
 * The rulebook doesn't specify the order of players if multiple Secret Weapons
 * were on the Pitch. So Jervis is doing it by first going through all Receiving
 * players and then all Kicking players. This order is somewhat arbitrary.
 */
object DealWithSecretWeaponsStep: Procedure() {
    override val initialNode = ReceivingTeamSelectPlayerToSendOff
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        return AddContext(DealWithSecretWeaponsContext())
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        return RemoveContext<DealWithSecretWeaponsContext>()
    }

    object ReceivingTeamSelectPlayerToSendOff: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.receivingTeam
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val availablePlayers = getPlayersToSendOff(state.receivingTeam, rules)
            return when (availablePlayers.isNotEmpty()) {
                true -> listOf(SelectPlayer.fromPlayers(availablePlayers))
                false -> listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<DealWithSecretWeaponsContext>()
            return when (action) {
                Continue -> GotoNode(KickingTeamSelectPlayerToSendOff)
                is PlayerSelected -> {
                    compositeCommandOf(
                        UpdateContext(context.copy(selectedPlayer = action.getPlayer(state))),
                        GotoNode(HandlePlayerBeingSentOff),
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object KickingTeamSelectPlayerToSendOff: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.kickingTeam
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val availablePlayers = getPlayersToSendOff(state.kickingTeam, rules)
            return when (availablePlayers.isNotEmpty()) {
                true -> listOf(SelectPlayer.fromPlayers(availablePlayers))
                false -> listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<DealWithSecretWeaponsContext>()
            return when (action) {
                Continue -> ExitProcedure()
                is PlayerSelected -> {
                    compositeCommandOf(
                        UpdateContext(context.copy(selectedPlayer = action.getPlayer(state))),
                        GotoNode(HandlePlayerBeingSentOff),
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object HandlePlayerBeingSentOff: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<DealWithSecretWeaponsContext>()
            val player = context.selectedPlayer ?: INVALID_GAME_STATE("Missing selected player")
            val hasBribes = player.team.bribes.any { !it.used }
            val sentOffContext = BeingSentOffContext(player, isBribeAvailable = hasBribes)
            return compositeCommandOf(
                ReportSpottedByRef(sentOffContext, usingSecretWeapon = true),
                AddContext(sentOffContext)
            )
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = BeingSentOff
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<DealWithSecretWeaponsContext>()
            val player = context.selectedPlayer ?: INVALID_GAME_STATE("Missing selected player")
            return compositeCommandOf(
                UpdateContext(context.copy(selectedPlayer = null)),
                rules.setSecretWeaponOnPitchDuringDrive(player, onPitch = false),
                RemoveContext<BeingSentOffContext>(),
                GotoNode(if (state.receivingTeam == player.team) ReceivingTeamSelectPlayerToSendOff else KickingTeamSelectPlayerToSendOff),
            )
        }
    }

    // HELPER FUNCTION
    private fun getPlayersToSendOff(team: Team, rules: Rules): List<Player> {
        return team.filter { rules.wasSecretWeaponOnPitchDuringDrive(it) }
    }
}
