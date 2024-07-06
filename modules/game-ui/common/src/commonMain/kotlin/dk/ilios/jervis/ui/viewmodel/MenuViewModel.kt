package dk.ilios.jervis.ui.viewmodel

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.serialize.JervisSerialization
import okio.Path

class MenuViewModel {
    var controller: GameController? = null

    fun saveGameState(destination: Path) {
        JervisSerialization.saveToFile(controller!!, destination)
    }
}
