package com.jervisffb.ui.game.viewmodel

import com.jervisffb.engine.actions.GameAction
import com.jervisffb.ui.game.UiGameController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * View model responsible for "unknown actions" coming from the Rules Engine.
 *
 * These actions are actions that are not otherwise handled and we need a generic
 * way to show them to users so we do not accidentially risk blocking the UI
 * indefinitely.
 *
 * In an ideal world, no actions are "unknown" and is thus only assumed to produce
 * events during development.
 */
class ActionSelectorViewModel(
    private val uiState: UiGameController,
) {
    // TODO This is causing race conditions. How?
    val availableActions: Flow<List<GameAction>> = uiState.uiStateFlow.map { it.unknownActions }

    fun actionSelected(action: GameAction) {
        uiState.userSelectedAction(action)
    }
}
