package com.jervisffb.ui.game.viewmodel

import com.jervisffb.ui.game.UiGameController
import com.jervisffb.ui.game.UiGameStatusUpdate
import com.jervisffb.ui.game.UiTeamInfoUpdate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * View model for the top status bar that contains Team Info, inducements and general game status
 */
class GameStatusViewModel(val controller: UiGameController) {
    fun progress(): Flow<UiGameStatusUpdate> {
        return controller.uiStateFlow.map { uiSnapshot -> uiSnapshot.gameStatus }
    }

    fun homeTeamInfoFlow(): Flow<UiTeamInfoUpdate> {
        return controller.uiStateFlow.map { uiSnapshot ->
            uiSnapshot.gameStatus.homeTeamInfo
        }
    }

    fun awayTeamInfoFlow(): Flow<UiTeamInfoUpdate> {
        return controller.uiStateFlow.map { uiSnapshot ->
            uiSnapshot.gameStatus.awayTeamInfo
        }
    }
}
