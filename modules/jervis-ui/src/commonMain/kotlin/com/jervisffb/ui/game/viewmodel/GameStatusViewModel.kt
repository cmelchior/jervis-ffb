package com.jervisffb.ui.game.viewmodel

import com.jervisffb.ui.game.UiGameController
import com.jervisffb.ui.game.UiGameStatusUpdate
import com.jervisffb.ui.game.UiTeamInfoUpdate
import com.jervisffb.ui.menu.GameScreenModel
import com.jervisffb.ui.menu.LocalPitchDataWrapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * View model for the top status bar that contains Team Info, inducements and general game status
 */
class GameStatusViewModel(
    val screenModel: GameScreenModel,
    val localPitchData: LocalPitchDataWrapper,
    val controller: UiGameController
) {

    fun progress(): Flow<UiGameStatusUpdate> {
        return controller.uiStateFlow.map { uiSnapshot -> uiSnapshot.status }
    }

    fun homeTeamInfoFlow(): Flow<UiTeamInfoUpdate> {
        return controller.uiStateFlow.map { uiSnapshot ->
            uiSnapshot.homeTeamInfo
        }
    }

    fun awayTeamInfoFlow(): Flow<UiTeamInfoUpdate> {
        return controller.uiStateFlow.map { uiSnapshot ->
            uiSnapshot.awayTeamInfo
        }
    }

    // Which "game status" message to show. It is shown below the "Game Status" button
    // and just above the pitch.
    fun messageFlow(): Flow<String?> {
        return controller.uiStateFlow.map {
            it.gameStatusText
        }
    }
}
