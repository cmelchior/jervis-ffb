package com.jervisffb.net

import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rng.DiceRollGenerator
import com.jervisffb.engine.rng.UnsafeRandomDiceGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LightServer(
    host: Team,
    gameName: String,
    testMode: Boolean = false, // Ensure that handling events are done in a deterministic manner
) {
    val diceRollGenerator: DiceRollGenerator = UnsafeRandomDiceGenerator()
    val gameCache = GameCache()
    private val websocketServer = PlatformWebSocketServer(this)

    init {
        val session = GameSession(
            this,
            GameId(gameName),
            null,
            emptyList(),
            testMode,
        )
        gameCache.safeAddGame(session)
    }

    suspend fun start() {
        websocketServer.start()
    }

    suspend fun stop() {
        // TODO Stopping the server in tests seems to deadlock, need to figure out why
        //  For now running shutting down on a seperate thread seems to work
        withContext(Dispatchers.Default) {
            gameCache.shutdownAll()
            websocketServer.stop()
        }
    }
}

