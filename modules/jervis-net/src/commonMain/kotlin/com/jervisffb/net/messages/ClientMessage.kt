package com.jervisffb.net.messages

import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionId
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.serialize.SerializedTeam
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


@Serializable
sealed interface TeamInfo
@Serializable
data class P2PTeamInfo(val team: SerializedTeam): TeamInfo {
    constructor(team: Team) : this(SerializedTeam.serialize(team))
}
@Serializable
data class HostedTeamInfo(val teamId: TeamId): TeamInfo


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

data class InternalGameActionMessage(
    val clientIndex: GameActionId,
    val action: GameAction? = null,
): InternalClientMessage

// Last message before the server will shut down.
data class InternalShutdownMessage(
    val action: suspend () -> Unit,
): InternalClientMessage

@Serializable
sealed interface JoinGameMessage: ClientMessage {
    val gameId: GameId
    val username: String
    val password: String?
}

@Serializable
data class JoinGameAsCoachMessage(
    override val gameId: GameId,
    override val username: String,
    override val password: String?,
    val coachName: String,
    val isHost: Boolean,
    val team: TeamInfo? = null
): JoinGameMessage

// Join game with `gameId` as spectator. If it doesn't exist, return an error.
@Serializable
data class JoinGameAsSpectatorMessage(
    override val gameId: GameId,
    override val username: String,
    override val password: String?,
    val spectatorName: String,
): JoinGameMessage

//
@Serializable
data class TeamSelectedMessage(
    val team: TeamInfo,
): ClientMessage

// Client responds whether to accept the game or not
@Serializable
data class StartGameMessage(
    val startGame: Boolean,
): ClientMessage

// Client is gracefully leaving the game.
@Serializable
data class LeaveGameMessage(
    val id: GameId,
): ClientMessage

// Tell the server that the client has started its game engine and can start receiving events
@Serializable
data class GameStartedMessage(val id: GameId): ClientMessage

/**
 * This action has been selected and processed successfully on the server.
 *
 * @param clientIndex the id of the [com.jervisffb.engine.GameDelta] in the client model, that was created from the [action].
 * @param action the action processed at the given index.
 */
@Serializable
data class GameActionMessage(
    val clientIndex: GameActionId,
    val action: GameAction,
): ClientMessage

