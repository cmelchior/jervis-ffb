package dk.ilios.jervis.ui.viewmodel

import dk.ilios.jervis.actions.Undo
import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.serialize.JervisSerialization
import okio.Path

enum class Feature {
    REROLL_SUCCESSFUL_ACTIONS
}


class MenuViewModel {
    lateinit var controller: GameController
    lateinit var uiActionFactory: UiActionFactory

    // Default values .. figure out a way to persist these
    private var features: MutableMap<Feature, Boolean> = mutableMapOf(
        Feature.REROLL_SUCCESSFUL_ACTIONS to false,
    )

    fun saveGameState(destination: Path) {
        JervisSerialization.saveToFile(controller, destination)
    }

    fun undoAction() {
        controller.undoLastAction()
        uiActionFactory.userSelectedAction(Undo)
    }


    fun toggleFeature(rerollSuccessfulActions: Feature, enabled: Boolean) {
        features[rerollSuccessfulActions] = enabled
    }

    fun isFeatureEnabled(feature: Feature): Boolean {
        return features[feature] ?: false
    }
}
