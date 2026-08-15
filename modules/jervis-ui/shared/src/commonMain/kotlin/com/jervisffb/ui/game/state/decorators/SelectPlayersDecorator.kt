package com.jervisffb.ui.game.state.decorators

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.PlayersSelected
import com.jervisffb.engine.actions.SelectPlayers
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.model.GuardedBadgeAction
import com.jervisffb.ui.game.model.GuardedPlayerAction
import com.jervisffb.ui.game.model.UiPitchPlayer
import com.jervisffb.ui.game.model.UiPlayerAction
import com.jervisffb.ui.game.state.ManualActionProvider
import com.jervisffb.ui.menu.GameScreenModel

object SelectPlayersDecorator : PitchActionDecorator<SelectPlayers> {
    override fun decorate(
        actionProvider: ManualActionProvider,
        state: Game,
        descriptor: SelectPlayers,
        owner: Team?,
        acc: UiSnapshotAccumulator
    ) {
        val selectedAction = UiPlayerAction(descriptor, GuardedPlayerAction(acc) onClickHandler@{ _, screenModel: GameScreenModel, player: UiPitchPlayer ->
            val enablePlayer = !player.isTemporarySelected.value
            if (enablePlayer && screenModel.selectedPlayersInUi.size == descriptor.count) return@onClickHandler
            player.isTemporarySelected.value = enablePlayer
            // Track selected players
            if (enablePlayer) {
                screenModel.selectedPlayersInUi.add(player.id)
            } else {
                screenModel.selectedPlayersInUi.remove(player.id)
            }
            screenModel.isGameStatusBoxEnabled.value = true
            screenModel.gameStatusBoxTitle.value = "End Player Selection (${screenModel.selectedPlayersInUi.size})"
        })
        descriptor.players.forEach { playerId ->
            acc.updatePlayer(playerId) {
                it.copy(selectedAction = selectedAction)
            }
        }
        acc.updateGameStatus {
            it.copy(
                centerBadgeText = "End Player Selection (0)", // "Select up to ${descriptor.count} players",
                centerBadgeAction = GuardedBadgeAction(acc) { id, model: GameScreenModel ->
                    val players = model.getSelectedPlayers()
                    val action = when (players.isNotEmpty()) {
                        true -> PlayersSelected(model.getSelectedPlayers())
                        false -> Cancel
                    }
                    actionProvider.userActionSelected(id, action)
                },
                centerBadgeEnabled = true
            )
        }
    }
}
