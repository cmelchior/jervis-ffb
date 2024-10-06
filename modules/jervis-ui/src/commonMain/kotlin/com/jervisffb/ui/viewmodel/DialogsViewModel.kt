package com.jervisffb.ui.viewmodel

import com.jervisffb.engine.actions.GameAction
import kotlinx.coroutines.flow.Flow

/**
 * Class responsible for handling and showing model dialogs.
 */
class DialogsViewModel(val uiActionFactory: UiActionFactory) {
    val diceRollGenerator = uiActionFactory.model.controller.diceRollGenerator

    fun buttonActionSelected(action: GameAction) {
        uiActionFactory.userSelectedAction(action)
    }

    val availableActions: Flow<UserInputDialog?> = uiActionFactory.dialogActions
}
