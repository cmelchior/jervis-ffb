package com.jervisffb.ui.game.state.decorators

import com.jervisffb.engine.actions.RandomPlayersSelected
import com.jervisffb.engine.actions.SelectRandomPlayers
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.model.UiFieldPlayer
import com.jervisffb.ui.game.state.ManualActionProvider
import com.jervisffb.ui.menu.GameScreenModel

object SelectRandomPlayersDecorator : FieldActionDecorator<SelectRandomPlayers> {
    override fun decorate(
        actionProvider: ManualActionProvider,
        state: Game,
        descriptor: SelectRandomPlayers,
        owner: Team?,
        acc: UiSnapshotAccumulator
    ) {
        descriptor.players.forEach { playerId ->
            val selectedAction = onClickHandler@{ screenModel: GameScreenModel, player: UiFieldPlayer ->
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
            }
            acc.updatePlayer(playerId) {
                it.copy(selectedAction = selectedAction)
            }
        }
        acc.updateGameStatus {
            it.copy(
                centerBadgeText = "Select ${descriptor.count} random players",
                centerBadgeAction = { model ->
                    val action = RandomPlayersSelected(model.getSelectedPlayers())
                    actionProvider.userActionSelected(action)
                },
                centerBadgeEnabled = false
            )
        }
    }
}
