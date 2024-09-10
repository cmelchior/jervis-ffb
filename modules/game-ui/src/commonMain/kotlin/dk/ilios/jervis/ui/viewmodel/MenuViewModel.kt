package dk.ilios.jervis.ui.viewmodel

import dk.ilios.jervis.actions.FieldSquareSelected
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.actions.Undo
import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.ext.playerNo
import dk.ilios.jervis.model.FieldCoordinate
import dk.ilios.jervis.serialize.JervisSerialization
import okio.Path

enum class Feature {
    ROLL_DICE,
    DO_NOT_REROLL_SUCCESSFUL_ACTIONS,
    SELECT_KICKING_PLAYER
}

class MenuViewModel {
    var controller: GameController? = null
    lateinit var uiActionFactory: UiActionFactory

    // Default values .. figure out a way to persist these
    private var features: MutableMap<Feature, Boolean> = mutableMapOf(
        Feature.DO_NOT_REROLL_SUCCESSFUL_ACTIONS to true,
        Feature.SELECT_KICKING_PLAYER to true,
    )

    fun saveGameState(destination: Path) {
        JervisSerialization.saveToFile(controller!!, destination)
    }

    fun undoAction() {
        uiActionFactory.userSelectedAction(Undo)
    }

    fun toggleFeature(rerollSuccessfulActions: Feature, enabled: Boolean) {
        features[rerollSuccessfulActions] = enabled
    }

    fun isFeatureEnabled(feature: Feature): Boolean {
        val isUndoing = controller?.lastActionWasUndo ?: false
        return !isUndoing && (features[feature] ?: false)
    }

    fun loadSetup(id: String) {
        val state = controller!!.state
        val setupActions = Setups.setups[id]!!.flatMap { (playerNo, fieldCoordinate) ->
            listOf(
                PlayerSelected(state.activeTeam[playerNo].id),
                FieldSquareSelected(fieldCoordinate)
            )
        }
        uiActionFactory.userSelectedMultipleActions(setupActions, delayEvent = false)
    }
}

object Setups {
    val setups = mutableMapOf(
        // Away
        "5-5-1" to mapOf(
            2.playerNo to FieldCoordinate(13, 5),
            3.playerNo to FieldCoordinate(13, 6),
            1.playerNo to FieldCoordinate(13, 7),
            4.playerNo to FieldCoordinate(13, 8),
            5.playerNo to FieldCoordinate(13, 9),
            6.playerNo to FieldCoordinate(14, 3),
            7.playerNo to FieldCoordinate(14, 11),
            8.playerNo to FieldCoordinate(14, 1),
            9.playerNo to FieldCoordinate(14, 13),
            10.playerNo to FieldCoordinate(16, 7),
            11.playerNo to FieldCoordinate(21, 7),
        ),
        // Home
        "3-4-4" to mapOf(
            1.playerNo to FieldCoordinate(12, 6),
            2.playerNo to FieldCoordinate(12, 7),
            3.playerNo to FieldCoordinate(12, 8),
            4.playerNo to FieldCoordinate(10, 1),
            5.playerNo to FieldCoordinate(10, 4),
            6.playerNo to FieldCoordinate(10, 10),
            7.playerNo to FieldCoordinate(10, 13),
            8.playerNo to FieldCoordinate(9, 1),
            9.playerNo to FieldCoordinate(9, 4),
            10.playerNo to FieldCoordinate(9, 10),
            11.playerNo to FieldCoordinate(9, 13),
        ),
    )
}



