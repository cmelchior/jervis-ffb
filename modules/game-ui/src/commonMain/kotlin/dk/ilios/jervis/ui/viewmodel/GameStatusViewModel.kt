package dk.ilios.jervis.ui.viewmodel

import dk.ilios.jervis.controller.GameController
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

class GameStatusViewModel(val controller: GameController) {
    fun progress(): Flow<GameProgress> {
        return controller.state.gameFlow.map { game ->
            GameProgress(
                game.halfNo,
                game.driveNo,
                game.homeTeam.name,
                game.homeTeam.turnData.turnMarker,
                game.awayTeam.name,
                game.awayTeam.turnData.turnMarker,
                game.homeGoals,
                game.awayGoals,
            )
        }
    }
}
