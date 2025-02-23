package com.jervisffb.ui.game.viewmodel

import com.jervisffb.ui.game.UiGameController
import com.jervisffb.ui.game.state.UiActionProvider
import com.jervisffb.ui.menu.GameScreenModel

// TODO Need to figure out what to do with this when the ui controller has multiple action providers
class ReplayControllerViewModel(
    private val uiState: UiGameController,
    private val gameModel: GameScreenModel,
) {
    private val actionProvider: UiActionProvider = uiState.actionProvider
//    val controller = gameModel.gameRunner

    fun startActions() {
        TODO("FIgure out how to start the random action provider")
        // actionProvider.startActionProvider()
    }

    fun pauseActions() {
        TODO("FIgure out how to pause the random action provider")
        // actionProvider.pauseActionProvider()
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
        TODO()
        // actionProvider.startActionProvider()
    }
}
