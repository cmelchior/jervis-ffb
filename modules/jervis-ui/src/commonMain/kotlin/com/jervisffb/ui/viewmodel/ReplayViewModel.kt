package com.jervisffb.ui.viewmodel

import com.jervisffb.ui.GameScreenModel

class ReplayViewModel(
    private val uiActionFactory: UiActionFactory,
    private val gameModel: GameScreenModel,
) {
    val controller = gameModel.controller

    fun rewind() {
        while (controller.back()) { }
    }

    fun back() {
        controller.back()
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
}
