package com.jervisffb.ui.game.state.decorators

import com.jervisffb.engine.actions.RandomPlayersSelected
import com.jervisffb.engine.actions.SelectRandomPlayers
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.model.GuardedBadgeAction
import com.jervisffb.ui.game.model.GuardedPlayerAction
import com.jervisffb.ui.game.model.UiPitchPlayer
import com.jervisffb.ui.game.model.UiPlayerAction
import com.jervisffb.ui.game.state.ManualActionProvider
import com.jervisffb.ui.menu.GameScreenModel

object SelectRandomPlayersDecorator : PitchActionDecorator<SelectRandomPlayers> {
    override fun decorate(
        actionProvider: ManualActionProvider,
        state: Game,
        descriptor: SelectRandomPlayers,
        owner: Team?,
        acc: UiSnapshotAccumulator
    ) {
        val selectedAction = UiPlayerAction(descriptor, GuardedPlayerAction(acc, true) onClickHandler@{ _, screenModel: GameScreenModel, player: UiPitchPlayer ->
            val enablePlayer = !player.isTemporarySelected.value
            if (enablePlayer && screenModel.selectedPlayersInUi.size == descriptor.count) return@onClickHandler
            player.isTemporarySelected.value = enablePlayer
            // Track selected players
            if (enablePlayer) {
                screenModel.selectedPlayersInUi.add(player.id)
            } else {
                screenModel.selectedPlayersInUi.remove(player.id)
            }
            // Enable/Disable "end" button
            if (screenModel.selectedPlayersInUi.size == descriptor.count) {
                screenModel.isGameStatusBoxEnabled.value = true
            } else {
                screenModel.isGameStatusBoxEnabled.value = false
            }
            // Configure button title
            if (screenModel.selectedPlayersInUi.size < descriptor.count) {
                screenModel.gameStatusBoxTitle.value = "Select ${descriptor.count - screenModel.selectedPlayersInUi.size} random players"
            } else {
                screenModel.gameStatusBoxTitle.value = "Finish selecting players"
            }
        })
        descriptor.players.forEach { playerId ->
            acc.updatePlayer(playerId) {
                it.copy(selectedAction = selectedAction)
            }
        }
        acc.updateGameStatus {
            it.copy(
                centerBadgeText = "Select ${descriptor.count} random players",
                centerBadgeAction = GuardedBadgeAction(acc) { id, model ->
                    val action = RandomPlayersSelected(model.getSelectedPlayers())
                    actionProvider.userActionSelected(id, action)
                },
                centerBadgeEnabled = false
            )
        }
    }
}
