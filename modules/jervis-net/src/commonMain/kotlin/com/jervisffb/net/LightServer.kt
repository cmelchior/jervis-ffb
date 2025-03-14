package com.jervisffb.net

import com.jervisffb.engine.GameSettings
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.model.CoachId
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rng.DiceRollGenerator
import com.jervisffb.engine.rng.UnsafeRandomDiceGenerator
import com.jervisffb.engine.rules.Rules
import com.jervisffb.utils.jervisLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * This class represents the server used in P2P scenarios and will be started and controlled
 * by the host.
 */
class LightServer(
    gameName: String,
    rules: Rules, // Rules for the game
    hostCoach: CoachId, // Host Coach is always known
    hostTeam: Team, // Host Team is always known
    clientCoach: CoachId? = null, // If set, only this client coach can join
    clientTeam: Team? = null, // If set, only this client team can join
    initialActions: List<GameAction> = emptyList(),
    testMode: Boolean = false, // If `true`, event handling is done in a deterministic manner
) {
    companion object {
        val LOG = jervisLogger()
    }

    val diceRollGenerator: DiceRollGenerator = UnsafeRandomDiceGenerator()
    val gameCache = GameCache()
    private val websocketServer = PlatformWebSocketServer(this)

    init {
        // A add pre-determined game (created by the Host setting up the server)
        val session = GameSession(
            this,
            GameSettings(rules, initialActions),
            GameId(gameName),
            null,
            hostCoach,
            hostTeam,
            clientCoach,
            clientTeam,
            testMode,
        )
        gameCache.safeAddGame(session)
    }

    /**
     * @throws Exception if the address is already in use
     */
    suspend fun start() {
        websocketServer.start()
    }

    suspend fun stop() {
        // TODO Stopping the server in tests seems to deadlock, need to figure out why
        //  For now running shutting down on a separate thread seems to work
        withContext(Dispatchers.Default) {
            gameCache.shutdownAll()
            websocketServer.stop()
        }
        LOG.i { "[Server] Server stopped" }
    }
}

