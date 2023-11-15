package dk.ilios.bloodbowl.ui.model

import dk.ilios.bowlbot.controller.GameController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class GameProgress(val half: Int, val drive: Int, val turn: Int, val name: String)

class GameStatusViewModel(val controller: GameController) {

    fun progress(): Flow<GameProgress> {
        return controller.state.gameFlow.map { game ->
            GameProgress(game.halfNo, game.driveNo, game.activeTeam.turnData.currentTurn, game.activeTeam.name)
        }

    }
}