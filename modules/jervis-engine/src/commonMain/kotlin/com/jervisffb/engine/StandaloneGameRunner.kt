package com.jervisffb.engine

import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionId
import com.jervisffb.engine.model.Field
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rng.UnsafeRandomDiceGenerator
import com.jervisffb.engine.rules.Rules
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Top-level class for all game types. This class is responsible for the entire
 * game lifecycle, so depending on the game type, this could include any or all
 * of the following:
 * - Selecting coaches
 * - Selecting teams
 * - Waiting for players to connect
 * - Handle communication with
 *
 * Subclasses of this interface are considered the entry point for the model
 * layer in the MVVM architecture.
 */
interface GameRunner {
    val controller: GameEngineController
    val state: Game
    val rules: Rules

    fun handleAction(id: GameActionId, action: GameAction)
    fun getAvailableActions(): ActionRequest


    // Chat
    // Combine System Logs + Game Logs


}


/**
 * Things to worry about:
 *
 * - Selecting teams
 *      - Both clients select teams
 *      -
 *
 * - Running the Game
 *      - Time-outs waiting for an action.
 *          - Select a default action
 *          - The default action should show up in the logs
 *      - Client Leaving the Game (pausing it)
 *      - Handling Client Action (might not be valid)
 *      - Sending Client Action to Server (when determined to be valid)
 *      - Handle Action sent from Server (always valid)
 *      -
 *
 * - Connection issues
 *     - Loosing connection to server
 *     - Other client loosing connection to server.
 */
enum class GameRunnerState {
    CONNECTING,
    STARTED,
    CLOSED,
}


data class GameStateUpdate(
    val state: Game,
    val delta: GameDelta,
    val nextActions: ActionRequest,
)

/**
 * Model class for Hotseat games, i.e., games where both teams are being
 * played on the same screen.
 */
class HotSeatGameRunner(
    gameRules: Rules,
    val homeTeam: Team,
    val awayTeam: Team,
) : GameRunner {

    override val controller: GameEngineController

    init {
        val game = Game(gameRules, homeTeam, awayTeam, Field.createForRuleset(gameRules))
        controller = GameEngineController(game, diceGenerator = UnsafeRandomDiceGenerator())
    }

    private val _mutableGameStateUpdates = MutableSharedFlow<GameStateUpdate>()

    suspend fun gameFlow(): Flow<GameStateUpdate> {
        // Create engine controller
        return _mutableGameStateUpdates
    }

    override var state: Game = controller.state
    override var rules: Rules = controller.rules

    override fun handleAction(id: GameActionId, action: GameAction) {
        TODO("Not yet implemented")
    }

    override fun getAvailableActions(): ActionRequest {
        TODO("Not yet implemented")
    }
}




/**
 * Runner for a single standalone game.
 * A [GameRunner] encapsulates the entire lifecycle of a game, including the
 * lifecycle of clients, i.e.
 */
//class StandaloneGameRunner : GameRunner {
//
//
//
//
//
//    fun gameState(): Flow<StateDelta> {
//
//    }
//}



//enum class ClientState {
//    PENDING,
//    CONNECTED,
//    DISCONNECTED,
//}
//
//class Client {
//    var ping: Int?
//    var state: ClientState
//    var team: Team,
//}



class ServerGamRunner

// Start Model
    // Add


class P2PClientGameRunner

class P2PServerGameRunner

class StandalonGameRunner {




}
