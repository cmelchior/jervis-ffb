package com.jervisffb.ui.viewmodel

import com.jervisffb.ui.UiGameController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class GameProgress(
    val half: Int,
    val drive: Int,
    val homeTeam: String,
    val homeTeamTurn: Int,
    val awayTeam: String,
    val awayTeamTurn: Int,
    val homeTeamScore: Int = 0,
    val awayTeamScore: Int = 0,
)

class GameStatusViewModel(val controller: UiGameController) {
    fun progress(): Flow<GameProgress> {
        return controller.uiStateFlow.map { uiSnapshot ->
            val game = uiSnapshot.game
            GameProgress(
                game.halfNo,
                game.driveNo,
                game.homeTeam.name,
                game.homeTeam.turnMarker,
                game.awayTeam.name,
                game.awayTeam.turnMarker,
                game.homeScore,
                game.awayScore,
            )
        }
    }
}
