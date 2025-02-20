package com.jervisffb.ui.game.viewmodel

import com.jervisffb.ui.game.UiGameController
import com.jervisffb.ui.game.state.ReplayActionProvider
import com.jervisffb.ui.menu.GameScreenModel

class ReplayControllerViewModel(
    private val uiState: UiGameController,
    private val gameModel: GameScreenModel,
) {
    private val actionProvider: ReplayActionProvider = uiState.actionProvider as ReplayActionProvider
    val controller = gameModel.gameRunner

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
//        controller.forward()
    }

    fun stopReplay() {
        // controller.disableReplayMode()
    }

    fun enableReplay() {
        // controller.enableReplayMode()
    }

    fun start() {
        actionProvider.startActionProvider()
    }
}
