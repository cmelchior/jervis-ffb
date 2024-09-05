package dk.ilios.jervis.ui.viewmodel

import dk.ilios.jervis.controller.GameController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class GameProgress(
    val half: Int,
    val drive: Int,
    val activeTeam: String,
    val activeTeamTurn: Int,
    val inactiveTeam: String,
    val inactiveTeamTurn: Int,
)

class GameStatusViewModel(val controller: GameController) {
    fun progress(): Flow<GameProgress> {
        return controller.state.gameFlow.map { game ->
            GameProgress(
                game.halfNo,
                game.driveNo,
                game.activeTeam.name,
                game.activeTeam.turnData.turnMarker,
                game.inactiveTeam.name,
                game.inactiveTeam.turnData.turnMarker,
            )
        }
    }
}
