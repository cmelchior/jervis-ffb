package com.jervisffb.net

import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rng.DiceRollGenerator
import com.jervisffb.engine.rng.UnsafeRandomDiceGenerator

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

    fun start() {
        websocketServer.start()
    }

    suspend fun stop() {
        gameCache.shutdownAll()
        websocketServer.stop()
    }
}

