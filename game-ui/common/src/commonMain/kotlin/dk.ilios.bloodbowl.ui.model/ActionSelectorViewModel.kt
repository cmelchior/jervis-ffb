package dk.ilios.bloodbowl.ui.model

import dk.ilios.bowlbot.controller.GameController

class ActionSelectorViewModel(val controller: GameController) {

    fun start() {
        controller.start()
    }
}