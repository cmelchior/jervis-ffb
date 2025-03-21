package com.jervisffb.ui.game.viewmodel

import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.Undo
import com.jervisffb.engine.ext.playerNo
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.bb2020.procedures.SetupTeamContext
import com.jervisffb.engine.serialize.JervisSerialization
import com.jervisffb.ui.SoundEffect
import com.jervisffb.ui.SoundManager
import com.jervisffb.ui.game.UiGameController
import com.jervisffb.ui.menu.BackNavigationHandler
import com.jervisffb.ui.menu.TeamActionMode
import com.jervisffb.utils.canBeHost
import com.jervisffb.utils.singleThreadDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okio.Path

enum class Feature {
    DO_NOT_REROLL_SUCCESSFUL_ACTIONS,
    SELECT_KICKING_PLAYER,
    END_PLAYER_ACTION_IF_ONLY_OPTON,
    SELECT_BLOCK_TYPE_IF_ONLY_OPTON,
}

class MenuViewModel {
    var controller: GameEngineController? = null
    lateinit var uiState: UiGameController

    val p2pHostAvaiable: Boolean = canBeHost()

    private val _showSettingsDialog = MutableStateFlow(false)

    val navigatorContext = CoroutineScope(
        CoroutineName("ScreenNavigator")
            + CoroutineExceptionHandler { _, exception -> throw exception }
            + singleThreadDispatcher("menuScope")
    )

    // Default values .. figure out a way to persist these
    private var features: MutableMap<Feature, Boolean> = mutableMapOf(
        Feature.DO_NOT_REROLL_SUCCESSFUL_ACTIONS to true,
        Feature.SELECT_KICKING_PLAYER to true,
        Feature.END_PLAYER_ACTION_IF_ONLY_OPTON to true,
        Feature.SELECT_BLOCK_TYPE_IF_ONLY_OPTON to true
    )

    fun backToLastScreen() {
        BackNavigationHandler.execute()
    }

    fun openSettings(bool: Boolean = true) {
        _showSettingsDialog.value = bool
    }

    fun showSettingsDialog(): StateFlow<Boolean> = _showSettingsDialog

    fun saveGameState(destination: Path) {
        JervisSerialization.saveToFile(controller!!, destination)
    }

    fun undoAction() {
        val team = when (uiState.uiMode) {
            TeamActionMode.HOME_TEAM -> uiState.state.homeTeam.id
            TeamActionMode.AWAY_TEAM -> uiState.state.awayTeam.id
            TeamActionMode.ALL_TEAMS -> null // No team restrictions when undoing on a Client controlling both teams
        }
        if (uiState.gameController.isUndoAvailable(team = team)) {
            uiState.userSelectedAction(Undo)
        } else {
            SoundManager.play(SoundEffect.ERROR)
        }
    }

    fun toggleFeature(rerollSuccessfulActions: Feature, enabled: Boolean) {
        features[rerollSuccessfulActions] = enabled
    }

    fun isFeatureEnabled(feature: Feature): Boolean {
        val isUndoing = controller?.lastActionWasUndo() ?: false
        return !isUndoing && (features[feature] ?: false)
    }

    fun loadSetup(id: String) {
        val allowedTeam = when (uiState.uiMode) {
            TeamActionMode.HOME_TEAM -> uiState.state.homeTeam.id
            TeamActionMode.AWAY_TEAM -> uiState.state.awayTeam.id
            TeamActionMode.ALL_TEAMS -> null // No team restrictions when undoing on a Client controlling both teams
        }
        if (allowedTeam != null && controller?.state?.activeTeam?.id == allowedTeam) {
            // This client is not considered the "active" client, which means it isn't allowed
            // to load setup formations
            SoundManager.play(SoundEffect.ERROR)
            return
        }

        val game = controller ?: error("No game controller was found")
        val team = game.state.getContext<SetupTeamContext>().team
        val setupActions = Setups.setups[id]!!.flatMap { (playerNo, fieldCoordinate) ->
            listOf(
                PlayerSelected(team[playerNo].id),
                if (team.isAwayTeam()) FieldSquareSelected(fieldCoordinate.swapX(game.rules)) else FieldSquareSelected(fieldCoordinate)
            )
        }
        uiState.userSelectedMultipleActions(setupActions, delayEvent = false)
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



