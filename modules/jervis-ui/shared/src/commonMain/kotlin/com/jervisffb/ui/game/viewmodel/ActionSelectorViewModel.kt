package com.jervisffb.ui.game.viewmodel

import com.jervisffb.engine.actions.GameAction
import com.jervisffb.ui.game.UiGameController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
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
    val availableActions: Flow<List<GameAction>> = uiState.uiStateFlow.map { it.unknownActions }

    // The first snapshot is the state the game starts in. Challenge panels should not switch
    // away from their information tab in that case. We don't expect any extra actions, at least
    // not critical ones, so the challenge info is more important.
    // In all other cases we do want to show the actions panel. It is a constant source of bug-reports
    // as people don't notice them.
    val availableActionUpdates: Flow<List<GameAction>> = availableActions.drop(1)

    fun actionSelected(action: GameAction) {
        uiState.userSelectedAction(action)
    }
}
