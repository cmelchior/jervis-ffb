package com.jervisffb.ui.game.viewmodel

import com.jervisffb.ui.game.UiGameController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class GameProgress(
    val half: Int,
    val drive: Int,
    val turnMax: Int,
    val homeTeam: String,
    val homeTeamTurn: Int,
    val awayTeam: String,
    val awayTeamTurn: Int,
    val homeTeamScore: Int = 0,
    val awayTeamScore: Int = 0,
    val centerBadgeText: String = "",
    val centerBadgeAction: (() -> Unit)? = null,
)

class GameStatusViewModel(val controller: UiGameController) {
    fun progress(): Flow<GameProgress> {
        return controller.uiStateFlow.map { uiSnapshot ->
            val game = uiSnapshot.game
            val rules = game.rules
            GameProgress(
                game.halfNo,
                game.driveNo,
                if (game.halfNo > rules.halfsPrGame) rules.turnsInExtraTime else rules.turnsPrHalf,
                game.homeTeam.name,
                game.homeTeam.turnMarker,
                game.awayTeam.name,
                game.awayTeam.turnMarker,
                game.homeScore,
                game.awayScore,
                uiSnapshot.centerBadgeText,
                uiSnapshot.centerBadgeAction
            )
        }
    }
}
