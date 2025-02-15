package com.jervisffb.net

import com.jervisffb.engine.model.Coach
import com.jervisffb.engine.model.Spectator
import com.jervisffb.engine.model.Team
import com.jervisffb.net.messages.P2PClientState
import com.jervisffb.net.messages.P2PHostState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.selects.select

enum class ClientState {
    SELECTING_TEAM,
    ACCEPTING_GAME,
    READY
}

sealed class JoinedClient {
    abstract val connection: JervisNetworkWebSocketConnection
    private val sessionClosedSignal = CompletableDeferred<Unit>()

    suspend fun disconnect(exitCode: JervisExitCode, reason: String) {
        connection.close(exitCode, reason)
        sessionClosedSignal.complete(Unit)
    }
    suspend fun awaitDisconnect() {
        select {
            sessionClosedSignal.onAwait { false }
            connection.closeReason.onAwait { true }
        }
    }
}

sealed class JoinedP2PCoach: JoinedClient() {
    abstract val coach: Coach
    abstract var team: Team?
    var hasAcceptedGame: Boolean = false
}

// Joined a P2P game as a Client
class JoinedP2PClient(
    override val connection: JervisNetworkWebSocketConnection,
    override val coach: Coach,
    override var team: Team? = null,
    var state: P2PClientState,
): JoinedP2PCoach()

// Joined a P2P game as a Host
class JoinedP2PHost(
    override val connection: JervisNetworkWebSocketConnection,
    override val coach: Coach,
    override var team: Team? = null,
    var state: P2PHostState,
): JoinedP2PCoach()

// Joined either a hosted or P2P game as a spectator
data class JoinedSpectator(
    override val connection: JervisNetworkWebSocketConnection,
    val spectator: Spectator,
): JoinedClient()
