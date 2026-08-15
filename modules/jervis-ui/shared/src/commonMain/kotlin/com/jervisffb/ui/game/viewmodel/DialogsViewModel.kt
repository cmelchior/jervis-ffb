package com.jervisffb.ui.game.viewmodel

import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionId
import com.jervisffb.engine.model.Player
import com.jervisffb.ui.game.UiGameController
import com.jervisffb.ui.game.dialogs.UserInputDialog
import com.jervisffb.ui.game.model.ModelRef
import com.jervisffb.ui.menu.GameScreenModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

/**
 * Class responsible for handling and showing model dialogs.
 */
class DialogsViewModel(
    val screenViewModel: GameScreenModel,
    private val uiState: UiGameController
) {
    val diceRollGenerator = uiState.diceGenerator

    // Called from the UI when a game action is created
    fun userActionSelected(id: GameActionId, action: GameAction) {
        uiState.userSelectedAction(id, action)
    }
    val dialogData: Flow<UserInputDialog?> = uiState.uiStateFlow.map { it.dialogInput }
    val contextMenu: Flow<ModelRef<Player>?> = screenViewModel.contextMenuFlow.onEach {
        if (it != null) {
            screenViewModel.sharedPitchData.pointerBus.notifyExitPitch()
        }
    }
}
