package com.jervisffb.net

import com.jervisffb.engine.model.Team
import io.ktor.websocket.WebSocketSession
import kotlinx.coroutines.CompletableDeferred

enum class ClientState {
    SELECTING_TEAM,
    ACCEPTING_GAME,
    READY
}

sealed class JoinedClient {
    abstract val connection: WebSocketSession
    abstract val username: String
    private val sessionClosedSignal = CompletableDeferred<Unit>()
    suspend fun disconnect(exitCode: JervisExitCode, reason: String) {
        connection.close(exitCode, reason)
        sessionClosedSignal.complete(Unit)
    }
    suspend fun awaitDisconnect() {
        sessionClosedSignal.await()
    }
}

class JoinedPlayerClient(
    override val connection: WebSocketSession,
    override val username: String,
    var state: ClientState,
    var team: Team?,
): JoinedClient()

data class JoinedSpectatorClient(
    override val connection: WebSocketSession,
    override val username: String,
): JoinedClient()
