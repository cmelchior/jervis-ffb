package com.jervisffb.net

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Class responsible for tracking all games known to the server.
 */
class GameCache {
    val gamesLock = Mutex()
    private val games = mutableMapOf<GameId, GameSession>()

    fun getGame(id: GameId): GameSession? = games[id]

    suspend fun addGame(session: GameSession) {
        gamesLock.withLock {
            games[session.gameId] = session
        }
    }

    fun safeAddGame(session: GameSession) {
        games[session.gameId] = session
    }

    suspend fun shutdownAll() {
        gamesLock.withLock {
            games.forEach { (id, session) ->
                session.shutdownGame(JervisExitCode.SERVER_CLOSING, "Server is shutting down.")
            }
        }
    }
}
