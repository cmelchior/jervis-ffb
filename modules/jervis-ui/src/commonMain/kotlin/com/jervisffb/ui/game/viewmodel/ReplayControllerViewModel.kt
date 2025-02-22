package com.jervisffb.ui.game.viewmodel

import com.jervisffb.ui.game.UiGameController
import com.jervisffb.ui.game.state.ReplayActionProvider
import com.jervisffb.ui.menu.GameScreenModel

// TODO Need to figure out what to do with this when the ui controller has multiple action providers
class ReplayControllerViewModel(
    private val uiState: UiGameController,
    private val gameModel: GameScreenModel,
) {
    private val actionProvider: ReplayActionProvider = uiState.currentActionProvider as ReplayActionProvider
//    val controller = gameModel.gameRunner

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
