package com.jervisffb.ui.game.viewmodel

import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionId
import com.jervisffb.ui.game.UiGameController
import com.jervisffb.ui.game.dialogs.UserInputDialog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Class responsible for handling and showing model dialogs.
 */
class DialogsViewModel(private val uiState: UiGameController) {
    val diceRollGenerator = uiState.diceGenerator
    val nextActionId: GameActionId
        get() = uiState.engineController.nextActionIndex()

    fun buttonActionSelected(id: GameActionId, action: GameAction) {
        uiState.userSelectedAction(id, action)
    }
    val availableActions: Flow<UserInputDialog?> = uiState.uiStateFlow.map { it.dialogInput }
}
