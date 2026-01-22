package com.jervisffb.ui.game.viewmodel

import com.jervisffb.ui.game.UiGameController
import com.jervisffb.ui.game.state.ReplayActionProvider
import com.jervisffb.ui.menu.GameScreenModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class ReplayState {
    STARTED,
    PAUSED,
}

// TODO Need to figure out what to do with this when the ui controller has multiple action providers
class ReplayControllerViewModel(
    private val uiState: UiGameController,
    private val gameModel: GameScreenModel,
) {
    val state: StateFlow<ReplayState>
        field = MutableStateFlow(ReplayState.PAUSED)

    private val actionProvider = uiState.actionProvider as ReplayActionProvider
    private var hasStarted = false

    fun start() {
        state.value = ReplayState.STARTED
        if (hasStarted) {
            actionProvider.pauseActionProvider()
        }
        actionProvider.startActionProvider()
        hasStarted = true
    }

    fun pause() {
        state.value = ReplayState.PAUSED
        actionProvider.pauseActionProvider()
    }
}
