package com.jervisffb.ui.game.viewmodel

import com.jervisffb.engine.actions.GameAction
import com.jervisffb.ui.game.UiGameController
import com.jervisffb.ui.game.dialogs.UserInputDialog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Class responsible for handling and showing model dialogs.
 */
class DialogsViewModel(private val uiState: UiGameController) {
    val diceRollGenerator = uiState.diceGenerator

    fun buttonActionSelected(action: GameAction) {
        uiState.userSelectedAction(action)
    }
    val availableActions: Flow<UserInputDialog?> = uiState.uiStateFlow.map { it.dialogInput }
}
