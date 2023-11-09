package dk.ilios.bloodbowl.ui.model

import dk.ilios.bowlbot.controller.GameController

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