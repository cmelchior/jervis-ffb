package com.jervisffb.net

import com.jervisffb.net.messages.ClientMessage
import com.jervisffb.net.messages.JoinGameAsCoachMessage
import com.jervisffb.net.messages.JoinGameAsSpectatorMessage
import com.jervisffb.net.messages.JoinGameMessage
import com.jervisffb.net.serialize.jervisNetworkSerializer
import com.jervisffb.utils.jervisLogger
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.ClosedReceiveChannelException

/**
 * Websocket close codes that are specific to Jervis.
 */
enum class JervisExitCode(val code: Short) {
    GAME_FINISHED(4000), // The Game ended successfully, no further reason to be connected to the server
    CLIENT_CLOSING(4001), // Client is disconnecting gracefully
    SERVER_CLOSING(4002), // Server is disconnecting gracefully (because it is shutting down)
    GAME_NOT_ACCEPTED(4003), // Game was declined by one of the players.
    UNEXPECTED_ERROR(4004), // An unexpected error happened on the server.
    NO_GAME_FOUND(4005), // No game with the given gameId exists.
    WRONG_STARTING_MESSAGE(4006), // The first message to /game wasn't a JoinGameMessage
    URL_NOT_FOUND(4007), // A 404 was thrown when trying to connect to the game url
}

/**
 * Class responsible for the websocket connections to connected clients.
 *
 */
class PlatformWebSocketServer(
    val server: LightServer,
) {

    companion object {
        val LOG = jervisLogger()
    }

    private lateinit var platformClient: Any


    fun start() {
        // Warning: Leaving the callback scope will automatically close the session.
        val newConnectionCallback: suspend (DefaultWebSocketSession, GameId?) -> Unit = { platformConnection: DefaultWebSocketSession, gameId: GameId? ->
            LOG.d { "[Server] New connection detected: $platformConnection" }

            // All games should either have been created either programmatically (for P2P games), a HTTP
            // request (FUMBBL) or through the /lobby API (Self-hosted server).
            // So when a websocket connection established, the first message is required to be a `JoinGameMessage` with a
            // `gameId` that exists. If not, the connection is terminated immediately. If the game exists, the websocket
            // session is added to the GameSession which takes over all responsibility from there.
            var connectionUsername: String? = null
            try {
                // For P2P games, the GameId is part of the URL. So for those cases, we
                // can abort early without receiving any messages.
                if (gameId != null && server.gameCache.getGame(gameId) == null) {
                    platformConnection.close(JervisExitCode.NO_GAME_FOUND, "GameId not found: ${gameId.value}")
                } else {
                    val message = platformConnection.incoming.receive() as Frame.Text
                    val json = message.readText()
                    val clientMessage = jervisNetworkSerializer.decodeFromString<ClientMessage>(json)
                    if (!closeConnectionIfInvalidFormat(platformConnection, clientMessage)) {
                        val (gameId, username) = readGameAndUser(clientMessage)
                        connectionUsername = username
                        val game = server.gameCache.getGame(gameId) ?: error("GameId not found: ${gameId?.value}")
                        val connection = JervisNetworkWebSocketConnection(username, platformConnection)
                        val clientConnection = game.addClient(connection, clientMessage as JoinGameMessage)
                        clientConnection.awaitDisconnect()
                        // TODO This will send messages out of order, which is a problem during shutdown.
                        game.removeClient(clientConnection)
                    }
                }
            } catch (ex: ClosedReceiveChannelException) {
                // The connection was closed while waiting for the first message
                // We just ignore this.
                LOG.d("New connection closed before receiving first message: $platformConnection")
            } catch (ex: Throwable) {
                if (ex is CancellationException) throw ex
                LOG.i("[Server] Connection ($platformConnection) closed due to an error:\n${ex.stackTraceToString()}")
                platformConnection.close(JervisExitCode.UNEXPECTED_ERROR, ex.stackTraceToString())
            }
            LOG.d("[Server] Connection closed: ${connectionUsername ?: platformConnection}")
        }
        platformClient = startEmbeddedServer(
            this.server,
            newConnectionCallback
        )
    }

    private fun readGameAndUser(message: ClientMessage): Pair<GameId, String> {
        return when (message) {
            is JoinGameAsCoachMessage -> Pair(message.gameId, message.username)
            is JoinGameAsSpectatorMessage -> Pair(message.gameId, message.username)
            else -> error("Invalid message type: ${message::class.simpleName}")
        }
    }

    // Check if the starting message has valid format. If not, an error will be sent back and the connection is closed
    // Checking valid players/teams is done in GameSession
    private suspend fun closeConnectionIfInvalidFormat(connection: DefaultWebSocketSession, message: ClientMessage): Boolean {
        val error = when (message) {
            is JoinGameAsCoachMessage -> {
                if (message.isHost && message.team == null) {
                    JervisExitCode.WRONG_STARTING_MESSAGE to "Team must be set when joining a game as a host."
                } else {
                    null
                }
            }
            is JoinGameAsSpectatorMessage -> null // Do not check for anything here
            else -> JervisExitCode.WRONG_STARTING_MESSAGE to "First message must be a JoinGameMessage: ${message::class.simpleName}"
        }

        return if (error != null) {
            connection.close(error.first, error.second)
            true
        } else {
            false
        }
    }

    fun stop() {
        stopEmbeddedServer(platformClient)
    }
}
