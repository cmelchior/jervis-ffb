package dk.ilios.jervis.ui.model

import dk.ilios.jervis.controller.GameController

class ActionSelectorViewModel(val controller: GameController) {

    fun start() {
        controller.start()
    }
}