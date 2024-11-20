package com.jervisffb.net

import com.jervisffb.net.messages.ClientMessage
import com.jervisffb.net.messages.JervisErrorCode
import com.jervisffb.net.messages.ServerError
import com.jervisffb.net.messages.ServerMessage
import com.jervisffb.net.serialize.jervisNetworkSerializer
import com.jervisffb.utils.getHttpClient
import io.ktor.client.plugins.websocket.webSocket
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
import kotlinx.coroutines.Job
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
 * Class for controlling the websocket connection to a Jervis Host or Server.
 *
 * It only controls sending/receiving messages. It is up to users of this class
 * to know which messages to send and receive.
 */
class WebSocketClientConnection(
    private val gameId: GameId,
    private val url: String = "ws://127.0.0.1:8080/game",

) {
    private val scope = CoroutineScope(Job() + CoroutineName("JervisClientWebSocket-${gameId.value}") + Dispatchers.Default)

    private var session: DefaultWebSocketSession? = null

    // Messages sent from the server. Users of this class
    // are required to listen to the channel.
    private val incomingChannel: Channel<ServerMessage> = Channel(capacity = Channel.UNLIMITED)

    // Messages that should be sent to the server
    private val outgoingChannel: Channel<ClientMessage> = Channel(capacity = Channel.UNLIMITED)

    // Track the underlying close reason from the websocket connection (if any)
    private var closeReason = CompletableDeferred<CloseReason>()

    // Returns `true` if the connection is still think it is connected to the host.
    val isActive: Boolean
        get() = session != null && !closeReason.isCompleted

    fun start() {
        if (session != null) throw IllegalStateException("WebSocketClientConnection is already started.")
        val client = getHttpClient()
        closeReason = CompletableDeferred()
        scope.launch {
            client.webSocket(url) {
                session = this
                val job1 = launch { monitorConnectionClosing() }
                val job2 = launch { monitorOutgoingMessages() }
                val job3 = launch { monitorIncomingMessages() }
                joinAll(job1, job2, job3)
            }
        }.invokeOnCompletion { error: Throwable? ->
            if (error != null && error !is CancellationException) {
                throw error
            }
        }
    }

    private suspend fun monitorConnectionClosing() {
        try {
            val reason = session?.closeReason?.await() ?: CloseReason(JervisExitCode.UNEXPECTED_ERROR.code, "No server close reason.")
            closeReason.complete(reason)
            println("Server Disconnected: $reason")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            closeReason.complete(CloseReason(JervisExitCode.UNEXPECTED_ERROR.code, e.stackTraceToString()))
            println("Server Disconnected: $e")
        } finally {
            close() // Also cleanup internal channels and scopes
        }
    }

    private suspend fun monitorIncomingMessages() {
        for (inMessage in session!!.incoming) {
            try {
                when (inMessage) {
                    is Frame.Text -> {
                        println("[$session] Received: ${inMessage.readText()}")
                        val serverMessage = jervisNetworkSerializer.decodeFromString<ServerMessage>(inMessage.readText())
                        incomingChannel.send(serverMessage)
                    }
                    else -> TODO("Unsupported type: $inMessage")
                }
            } catch (ex: Throwable) {
                if (ex is CancellationException) throw ex
                println("Error: $ex")
                val error = ServerError(JervisErrorCode.READ_MESSAGE_ERROR, ex.stackTraceToString())
                // TODO How to handle errors here?
                throw ex
            }
        }
        println("Closing in-channel")
    }

    private suspend fun WebSocketClientConnection.monitorOutgoingMessages() {
        try {
            for (outMessage in outgoingChannel) {
                val messageJson = jervisNetworkSerializer.encodeToString(outMessage)
                println("[$session] Sending: $messageJson")
                session?.outgoing?.send(Frame.Text(messageJson))
            }
        } catch (ex: Throwable) {
            if (ex is CancellationException) throw ex
            println(ex)
        }
        println("Closing out-channel")
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
    fun close(exitCode: JervisExitCode = JervisExitCode.CLIENT_CLOSING, message: String = "Client is closing.") {
        session = null
        // No-op if already set by server terminating connection
        closeReason.complete(CloseReason(exitCode.code, message))
        incomingChannel.close()
        outgoingChannel.close()
        scope.cancel()
    }

    suspend fun awaitDisconnect(timeout: Duration = 10.seconds): CloseReason {
        return try {
            withTimeout(timeout) {
                println("Wait for disconnect")
                val reason = closeReason.await()
                println("[$session] Disconnected: $reason")
                reason
            }
        } catch (ex: TimeoutCancellationException) {
            CloseReason(CloseReason.Codes.INTERNAL_ERROR.code, "Timeout waiting for disconnect: $timeout")
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getCloseReason(): CloseReason? {
        return if (closeReason.isCompleted && !closeReason.isCancelled) {
            closeReason.getCompleted()
        } else {
            null
        }
    }
}
