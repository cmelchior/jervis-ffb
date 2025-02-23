package com.jervisffb.net

import com.jervisffb.engine.GameSettings
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rng.DiceRollGenerator
import com.jervisffb.engine.rng.UnsafeRandomDiceGenerator
import com.jervisffb.engine.rules.StandardBB2020Rules
import com.jervisffb.utils.jervisLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LightServer(
    hostTeam: Team,
    gameName: String,
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
            StandardBB2020Rules(),
            GameSettings(),
            GameId(gameName),
            null,
            listOf(hostTeam),
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

