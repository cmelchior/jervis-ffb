package com.jervisffb.engine

import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionId
import com.jervisffb.engine.model.Field
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rng.DiceRollGenerator
import com.jervisffb.engine.rng.UnsafeRandomDiceGenerator
import com.jervisffb.engine.rules.Rules
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Top-level class for running all game types. This class is responsible for
 * sending actions in and out of the rules engine.
 *
 * Subclasses of this interface are considered the entry point for the model
 * layer in the MVVM architecture.
 *
 * TODO Right now the UI just uses this to interact with the controller. Is this abstraction needed?
 */
interface GameRunner {
    val controller: GameEngineController
    val state: Game
    val rules: Rules
    val diceGenerator: DiceRollGenerator

    fun handleAction(action: GameAction)
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
        controller = GameEngineController(game)
    }
    override var state: Game = controller.state
    override var rules: Rules = controller.rules
    override val diceGenerator: DiceRollGenerator = UnsafeRandomDiceGenerator()

    private val _mutableGameStateUpdates = MutableSharedFlow<GameStateUpdate>()

    suspend fun gameFlow(): Flow<GameStateUpdate> {
        // Create engine controller
        return _mutableGameStateUpdates
    }


    override fun handleAction(action: GameAction) {
        controller.handleAction(action)
    }

    override fun handleAction(id: GameActionId, action: GameAction) {
        TODO("Is this needed?")
    }

    override fun getAvailableActions(): ActionRequest {
        return controller.getAvailableActions()
    }
}

/**
 * Runner for a single standalone game.
 */
