package com.jervisffb.ui.viewmodel

import com.jervisffb.engine.actions.GameAction
import com.jervisffb.ui.userinput.UiActionFactory
import com.jervisffb.ui.userinput.UserInput
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * View model for the unknown action selector part of the UI. Eventually, this should be removed.
 */
class ActionSelectorViewModel(
    private val uiActionFactory: UiActionFactory,
) {
    val availableActions: Flow<UserInput> = uiActionFactory.unknownActions

    fun start() {
        uiActionFactory.scope.launch {
            uiActionFactory.start(this)
        }
    }

    init {
        start()
    }

    fun actionSelected(action: GameAction) {
        uiActionFactory.userSelectedAction(action)
    }
}
