package com.jervisffb.net.messages

import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TeamId
import com.jervisffb.net.GameId
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketExtension
import io.ktor.websocket.WebSocketSession
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.serialization.Serializable
import kotlin.coroutines.CoroutineContext

val DUMMY_SESSION = object : WebSocketSession {
    override val coroutineContext: CoroutineContext = TODO()
    override val extensions: List<WebSocketExtension<*>> = emptyList()
    override val incoming: ReceiveChannel<Frame> = TODO()
    override var masking: Boolean = false
    override var maxFrameSize: Long = 0L
    override val outgoing: SendChannel<Frame> = TODO()
    override suspend fun flush() { /* No-op */ }
    override fun terminate() { /* No-op */ }
}

/**
 * Interface describing all messages sent from a Client to the Server.
 */
@Serializable
sealed interface ClientMessage: NetMessage

// Interface describing commands being used to communicate internally on the server
// These should not be allowed to be created from Clients, but are only available
// inside the server when mapping public ClientMessages.
sealed interface InternalClientMessage: ClientMessage

data class InternalJoinMessage(
    val action: suspend () -> Unit,
//    val joinMessage: JoinGameMessage
): InternalClientMessage


@Serializable
sealed interface JoinGameMessage: ClientMessage {
    val gameId: GameId
    val username: String
    val password: String?
}

@Serializable
data class JoinGameAsPlayerMessage(
    override val gameId: GameId,
    override val username: String,
    override val password: String?,
    val team: Team? = null, // Standalone mode will send the entire team here
    val teamId: TeamId? = null // Hosted mode will just send the teamId, and the server can then look up the team.
): JoinGameMessage {
}

// Join game with `gameId` as spectator. If it doesn't exist, return an error.
@Serializable
data class JoinGameAsSpectatorMessage(
    override val gameId: GameId,
    override val username: String,
    override val password: String?,
    val spectatorName: String,
): JoinGameMessage


// Client is accepting to start the game
@Serializable
data class StartGameMessage(
    val id: GameId,
): ClientMessage

// Client is gracefully leaving the game.
@Serializable
data class LeaveGameMessage(
    val id: GameId,
): ClientMessage

@Serializable
data class GameActionMessage(
    val action: GameAction,
): ClientMessage

