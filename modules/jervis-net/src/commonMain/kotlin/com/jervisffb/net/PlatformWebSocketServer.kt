package com.jervisffb.net

import com.jervisffb.net.messages.ClientMessage
import com.jervisffb.net.messages.JoinGameMessage
import com.jervisffb.net.serialize.jervisNetworkSerializer
import com.jervisffb.utils.jervisLogger
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
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
        // Warning: Leaving the scope will automatically close the session.
        val newConnectionCallback: suspend (WebSocketSession) -> Unit = { connection: WebSocketSession ->
            LOG.d { "New connection detected: $connection" }
            // All games should either have been created either programmatically (for standalone games), a HTTP
            // request (FUMBBL) or through the /lobby API (Self-hosted server). So when a websocket connection is
            // established, the first message is required to be a `JoinGameMessage` with a `gameId` that exists. If not,
            // the connection is terminated immediately. If the game exists, the websocket session is added to the
            // GameSession which takes over all responsibility from there.
            try {
                val message = connection.incoming.receive() as Frame.Text
                val json = message.readText()
                val clientMessage = jervisNetworkSerializer.decodeFromString<ClientMessage>(json)
                if (clientMessage !is JoinGameMessage) {
                    connection.close(JervisExitCode.WRONG_STARTING_MESSAGE, "First message must be a JoinGameMessage: ${message::class.simpleName}")
                } else {
                    val gameId = clientMessage.gameId
                    val game = server.gameCache.getGame(gameId)
                    if (game == null) {
                        connection.close(JervisExitCode.NO_GAME_FOUND, "GameId not found: ${gameId.value}")
                    } else {
                        val clientConnection = game.addClient(connection, clientMessage)
                        clientConnection.awaitDisconnect()
                    }
                }
            } catch (ex: ClosedReceiveChannelException) {
                // The connection was closed while waiting for the first message
                // We just ignore this.
                LOG.d("New connection closed before receiving first message: $connection")
            } catch (ex: Throwable) {
                if (ex is CancellationException) throw ex
                LOG.i("Server Connection closed due to an error: $connection")
                connection.close(JervisExitCode.UNEXPECTED_ERROR, ex.stackTraceToString())
            }
            LOG.d("Server Connection closed: $connection")
        }
        platformClient = startEmbeddedServer(
            this.server,
            newConnectionCallback
        )
    }

    fun stop() {
        stopEmbeddedServer(platformClient)
    }
}
