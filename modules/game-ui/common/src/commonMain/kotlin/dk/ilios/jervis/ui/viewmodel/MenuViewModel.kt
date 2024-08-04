package dk.ilios.jervis.ui.viewmodel

import dk.ilios.jervis.actions.Undo
import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.serialize.JervisSerialization
import okio.Path

class MenuViewModel {
    lateinit var controller: GameController
    lateinit var uiActionFactory: UiActionFactory

    fun saveGameState(destination: Path) {
        JervisSerialization.saveToFile(controller, destination)
    }

    fun undoAction() {
        controller.revert()
        uiActionFactory.userSelectedAction(Undo)
    }
}
