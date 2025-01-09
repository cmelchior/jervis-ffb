package com.jervisffb.ui.viewmodel

import com.jervisffb.engine.actions.GameAction
import com.jervisffb.ui.UiGameController
import com.jervisffb.ui.dialogs.UserInputDialog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Class responsible for handling and showing model dialogs.
 */
class DialogsViewModel(private val uiState: UiGameController) {
    val diceRollGenerator = uiState.gameRunner.controller.diceRollGenerator

    fun buttonActionSelected(action: GameAction) {
        uiState.userSelectedAction(action)
    }
    val availableActions: Flow<UserInputDialog?> = uiState.uiStateFlow.map { it.dialogInput }
}
