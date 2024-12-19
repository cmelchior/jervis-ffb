package com.jervisffb.net

import com.jervisffb.net.messages.ClientMessage
import com.jervisffb.net.messages.JervisErrorCode
import com.jervisffb.net.messages.ServerError
import com.jervisffb.net.messages.ServerMessage
import com.jervisffb.net.serialize.jervisNetworkSerializer
import com.jervisffb.utils.getHttpClient
import com.jervisffb.utils.jervisLogger
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.websocket.CloseReason
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.encodeToString
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Class for controlling the websocket connection to a Jervis Game Host or Server.
 *
 * It only controls sending/receiving messages. It is up to users of this class
 * to know which messages to send and receive.
 */
class JervisClientWebSocketConnection(
    private val gameId: GameId,
    private val url: String = "ws://127.0.0.1:8080/game",

) {
    companion object {
        val LOG = jervisLogger()
    }

    private val scope = CoroutineScope(CoroutineName("JervisClientWebSocket-${gameId.value}") + Dispatchers.Default)

    private var session: DefaultWebSocketSession? = null

    // Messages sent from the server. Users of this class
    // are required to listen to the channel.
    private val incomingChannel: Channel<ServerMessage> = Channel(capacity = Channel.UNLIMITED)

    // Messages that should be sent to the server
    private val outgoingChannel: Channel<ClientMessage> = Channel(capacity = Channel.UNLIMITED)

    // Track the underlying close reason from the websocket connection (if any)
    private var jervisCloseReason = CompletableDeferred<CloseReason>()

    private var closeDone = CompletableDeferred<Unit>()

    // Returns `true` if the connection is still think it is connected to the host.
    val isActive: Boolean
        get() = session != null && !jervisCloseReason.isCompleted

    fun start() {
        if (session != null) throw IllegalStateException("WebSocketClientConnection is already started.")
        val client = getHttpClient()
        jervisCloseReason = CompletableDeferred()
        scope.launch {
            val session = client.webSocketSession(url).also {
                this@JervisClientWebSocketConnection.session = it
            }
            val job1 = launch { monitorDisconnect(session) }
            val job2 = launch { monitorOutgoingClientMessages() }
            val job3 = launch { monitorIncomingServerMessages(session) }
            joinAll(job1, job2, job3)
        }.invokeOnCompletion { error: Throwable? ->
            closeDone.complete(Unit)
            if (error != null && error !is CancellationException) {
                throw error
            }
        }
    }

    private suspend fun monitorDisconnect(session: DefaultWebSocketSession?) {
        try {
            val reason = session?.closeReason?.await() ?: CloseReason(JervisExitCode.UNEXPECTED_ERROR.code, "No server close reason.")
            jervisCloseReason.complete(reason)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            jervisCloseReason.complete(CloseReason(JervisExitCode.UNEXPECTED_ERROR.code, e.stackTraceToString()))
        } finally {
            closeFromServer() // Also cleanup internal channels and scopes
        }
    }

    private suspend fun monitorIncomingServerMessages(session: DefaultClientWebSocketSession) {
        try {
            for (inMessage in session.incoming) {
                    when (inMessage) {
                        is Frame.Text -> {
                            val serverMessage = jervisNetworkSerializer.decodeFromString<ServerMessage>(inMessage.readText())
                            incomingChannel.send(serverMessage)
                        }
                        else -> TODO("Unsupported type: $inMessage")
                    }
            }
        } catch (ex: Throwable) {
            if (ex is CancellationException) throw ex
            val error = ServerError(JervisErrorCode.READ_MESSAGE_ERROR, ex.stackTraceToString())
            // TODO How to handle errors here?
            throw ex
        }
    }

    private suspend fun JervisClientWebSocketConnection.monitorOutgoingClientMessages() {
        try {
            for (outMessage in outgoingChannel) {
                val messageJson = jervisNetworkSerializer.encodeToString(outMessage)
                session?.outgoing?.send(Frame.Text(messageJson))
            }
        } catch (ex: Throwable) {
            if (ex is CancellationException) throw ex
            LOG.e { ex.stackTraceToString() }
        }
    }

    /**
     * Wait for the next message from the server.
     * Returns `null` if the connection is closed while waiting.
     */
    suspend fun receiveOrNull(): ServerMessage? = incomingChannel.receiveCatching().getOrNull()

    /**
     * Send a message to the server. Messages might not be sent immediately, so there is no
     * guarantee that the message has been sent when the method returns.
     */
    suspend fun send(command: ClientMessage) = outgoingChannel.send(command)

    /**
     * Close the connection and cleanup all internal resources. Provided exit code is only
     * used if the connection isn't already closed, in which case the server exit code
     * takes precedence.
     */
    suspend fun close(exitCode: JervisExitCode = JervisExitCode.CLIENT_CLOSING, message: String = "Client is closing.") {
        session?.incoming?.cancel()
        session?.close(exitCode, message)
        session = null
        // No-op if already set by the server terminating the connection
        jervisCloseReason.complete(CloseReason(exitCode.code, message))
        incomingChannel.cancel(cause = CancellationException("Client is closing."))
        outgoingChannel.close()
        scope.cancel(cause = CancellationException("Client is closing."))
        closeDone.await()
        jervisLogger().d { "Closing JervisClientWebSocketConnection: $this"  }
    }

    suspend fun closeFromServer() {
        session = null
        incomingChannel.close()
        outgoingChannel.close()
        jervisLogger().d { "JervisClientWebSocketConnection was closed due to a server disconnect: $this"  }
    }

    suspend fun awaitDisconnect(timeout: Duration = 10.seconds): CloseReason {
        return try {
            withTimeout(timeout) {
                val reason = jervisCloseReason.await()
                reason
            }
        } catch (ex: TimeoutCancellationException) {
            CloseReason(CloseReason.Codes.INTERNAL_ERROR.code, "Timeout waiting for disconnect: $timeout")
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getCloseReason(): CloseReason? {
        return if (jervisCloseReason.isCompleted && !jervisCloseReason.isCancelled) {
            jervisCloseReason.getCompleted()
        } else {
            null
        }
    }
}
