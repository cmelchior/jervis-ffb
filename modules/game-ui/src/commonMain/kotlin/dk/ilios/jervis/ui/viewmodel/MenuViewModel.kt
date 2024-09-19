package dk.ilios.jervis.ui.viewmodel

import dk.ilios.jervis.actions.FieldSquareSelected
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.actions.Undo
import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.ext.playerNo
import dk.ilios.jervis.model.locations.FieldCoordinate
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.procedures.SetupTeamContext
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
        val team = controller!!.state.getContext<SetupTeamContext>().team
        val setupActions = Setups.setups[id]!!.flatMap { (playerNo, fieldCoordinate) ->
            listOf(
                PlayerSelected(team[playerNo].id),
                if (team.isAwayTeam()) FieldSquareSelected(fieldCoordinate.swapX(controller!!.rules)) else FieldSquareSelected(fieldCoordinate)
            )
        }
        uiActionFactory.userSelectedMultipleActions(setupActions, delayEvent = false)
    }
}

object Setups {
    const val SETUP_5_5_1: String = "5-5-1"
    const val SETUP_3_4_4: String = "3-4-4"
    val setups = mutableMapOf(

        // Offensive
        SETUP_5_5_1 to mapOf(
            2.playerNo to FieldCoordinate(12, 5),
            3.playerNo to FieldCoordinate(12, 6),
            1.playerNo to FieldCoordinate(12, 7),
            4.playerNo to FieldCoordinate(12, 8),
            5.playerNo to FieldCoordinate(12, 9),
            6.playerNo to FieldCoordinate(11, 3),
            7.playerNo to FieldCoordinate(11, 11),
            8.playerNo to FieldCoordinate(11, 1),
            9.playerNo to FieldCoordinate(11, 13),
            10.playerNo to FieldCoordinate(8, 7),
            11.playerNo to FieldCoordinate(3, 7),
        ),

        // Defensive
        SETUP_3_4_4 to mapOf(
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



