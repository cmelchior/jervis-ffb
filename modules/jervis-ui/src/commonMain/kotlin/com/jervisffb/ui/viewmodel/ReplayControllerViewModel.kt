package com.jervisffb.ui.viewmodel

import com.jervisffb.ui.screen.GameScreenModel
import com.jervisffb.ui.state.ReplayActionProvider
import com.jervisffb.ui.UiGameController

class ReplayControllerViewModel(
    private val uiState: UiGameController,
    private val gameModel: GameScreenModel,
) {
    private val actionProvider: ReplayActionProvider = uiState.actionProvider as ReplayActionProvider
    val controller = gameModel.controller

    fun startActions() {
        actionProvider.startActionProvider()
    }

    fun pauseActions() {
        actionProvider.pauseActionProvider()
    }

    fun rewind() {
//        while (controller.back()) { }
    }

    fun back() {
//        controller.back()
    }

    fun forward() {
        controller.forward()
    }

    fun stopReplay() {
        controller.disableReplayMode()
    }

    fun enableReplay() {
        controller.enableReplayMode()
    }

    fun start() {
        actionProvider.startActionProvider()
    }
}
