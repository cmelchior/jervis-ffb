package dk.ilios.jervis.ui.model

import dk.ilios.jervis.controller.GameController

class ReplayViewModel(val controller: GameController) {

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