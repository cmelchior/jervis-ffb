package dk.ilios.jervis.ui.viewmodel

import dk.ilios.jervis.actions.Undo
import dk.ilios.jervis.ui.GameScreenModel

class ReplayViewModel(
    private val uiActionFactory: UiActionFactory,
    private val gameModel: GameScreenModel
) {

    val controller = gameModel.controller

    fun rewind() {
        while(controller.back()) { }
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