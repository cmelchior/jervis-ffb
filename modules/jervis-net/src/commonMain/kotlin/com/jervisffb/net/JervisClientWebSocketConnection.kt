package com.jervisffb.net

import com.jervisffb.net.messages.ClientMessage
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import okio.ProtocolException
import kotlin.concurrent.Volatile

/**
 * Class for controlling the websocket connection to a Jervis Game Host or Server.
 *
 * It only controls sending/receiving messages. It is up to users of this class
 * to know which messages to send and receive.
 */
class JervisClientWebSocketConnection(
    private val gameId: GameId,
    private val url: String = "ws://127.0.0.1:8080/game",
    private val coachName: String,
) {
    companion object {
        val LOG = jervisLogger()
    }

    private val scope = CoroutineScope(
        SupervisorJob() + CoroutineName("JervisClientWebSocket-${gameId.value}") + Dispatchers.Default
    )

    // Mutex guarding all attempts at closing the connection.
    private val lifecycleMutex = Mutex()

    // Coroutine job that controls the WebSocket connection.
    private var connectionJob: Job? = null

    @Volatile
    private var session: DefaultWebSocketSession? = null

    private var closeRequested = false

    // Messages sent from the server. Users of this class
    // are required to listen to the channel.
    private val incomingChannel: Channel<ServerMessage> = Channel(capacity = Channel.UNLIMITED)

    // Messages that should be sent to the server
    private val outgoingChannel: Channel<ClientMessage> = Channel(capacity = Channel.UNLIMITED)

    // Track the underlying close reason from the websocket connection (if any)
    private var jervisCloseReason = CompletableDeferred<CloseReason>()

    // Returns `true` if the connection is still think it is connected to the host.
    val isActive: Boolean
        get() = session != null && !jervisCloseReason.isCompleted

    /**
     * Start the connection. This method should never throw. If an exception occurs,
     * it should be reported back through [awaitDisconnect] with an appropriate
     * close reason.
     */
    fun start() {
        if (session != null || connectionJob != null) {
            throw IllegalStateException("WebSocketClientConnection is already started.")
        }
        val client = getHttpClient()
        jervisCloseReason = CompletableDeferred()
        closeRequested = false
        connectionJob = scope.launch {
            try {
                val connectedSession = client.webSocketSession(url)
                val closeImmediately = lifecycleMutex.withLock {
                    when (closeRequested) {
                        true -> true
                        false -> {
                            this@JervisClientWebSocketConnection.session = connectedSession
                            false
                        }
                    }
                }

                if (closeImmediately) {
                    connectedSession.close(JervisExitCode.CLIENT_CLOSING, "Client is closing.")
                    return@launch
                }

                val incomingMessagesJob = launch { monitorIncomingServerMessages(connectedSession) }
                val disconnectJob = launch { monitorDisconnect(connectedSession, incomingMessagesJob) }
                val outgoingMessagesJob = launch { monitorOutgoingClientMessages(connectedSession) }
                joinAll(incomingMessagesJob, disconnectJob, outgoingMessagesJob)
            } catch (ex: ProtocolException) {
                // Unsure if ProtocolException is thrown in other cases than 404, so just to be sure
                if (!isCloseRequested()) {
                    LOG.e { "[Server] ${ex.stackTraceToString()}" }
                    if (ex.message?.contains("404 Not Found") == true) {
                        jervisCloseReason.complete(CloseReason(JervisExitCode.URL_NOT_FOUND.code, ex.message ?: ""))
                    } else {
                        jervisCloseReason.complete(CloseReason(JervisExitCode.UNEXPECTED_ERROR.code, ex.message ?: ""))
                    }
                }
            } catch (ex: CancellationException) {
                // These are special and should always propagate
                throw ex
            } catch (ex: Throwable) {
                // Wrong use of ws/wss will end up here as an SSLException
                if (!isCloseRequested()) {
                    LOG.e { "[Client-${coachName}] Unexpected error in running the WebSocket connection: ${ex.stackTraceToString()}" }
                    jervisCloseReason.complete(CloseReason(JervisExitCode.UNEXPECTED_ERROR.code, ex.message ?: ""))
                }
            }
        }
    }

    private suspend fun monitorDisconnect(session: DefaultWebSocketSession?, incomingMessagesJob: Job) {
        try {
            val reason = session?.closeReason?.await() ?: CloseReason(JervisExitCode.UNEXPECTED_ERROR.code, "No server close reason.")
            // A close frame can be observed before the coroutine reading the incoming channel has
            // forwarded all preceding text frames. Wait for it to drain before reporting the
            // disconnect or closing the application-facing channel.
            incomingMessagesJob.join()
            jervisCloseReason.complete(reason)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            if (!isCloseRequested()) throw e
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
        } catch (ex: ClosedSendChannelException) {
            LOG.d { "Connection was closed. Cannot handle any more messages" }
        } catch (ex: Throwable) {
            if (ex is CancellationException) throw ex
            if (!isCloseRequested()) throw ex
        }
    }

    private suspend fun JervisClientWebSocketConnection.monitorOutgoingClientMessages(session: DefaultWebSocketSession) {
        try {
            for (outMessage in outgoingChannel) {
                val messageJson = jervisNetworkSerializer.encodeToString(outMessage)
                LOG.i { "[Client-$coachName] Sending message: $messageJson" }
                session.outgoing.send(Frame.Text(messageJson))
                LOG.i { "[Client-$coachName] Sent message: $messageJson" }
            }
        } catch (ex: Throwable) {
            if (ex is CancellationException) throw ex
            if (!isCloseRequested()) LOG.e { ex.stackTraceToString() }
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
        val (currentSession, job) = lifecycleMutex.withLock {
            closeRequested = true
            val currentSession = session
            session = null
            currentSession to connectionJob
        }

        currentSession?.close(exitCode, message)
        currentSession?.incoming?.cancel()

        // If the server terminated the connection, this is a no-op and the server close reason wins.
        jervisCloseReason.complete(CloseReason(exitCode.code, message))
        incomingChannel.cancel(cause = CancellationException("Client is closing."))
        outgoingChannel.close()
        job?.cancelAndJoin()
        scope.cancel(cause = CancellationException("Client is closing."))
        LOG.d { "[Client-$coachName] Closing connection: $this"  }
    }

    private suspend fun closeFromServer() {
        val closedByClient = lifecycleMutex.withLock {
            session = null
            closeRequested
        }
        incomingChannel.close()
        outgoingChannel.close()
        if (!closedByClient) {
            LOG.d { "[Client-$coachName] Connection was closed due to a server disconnect: $this"  }
        }
    }

    private suspend fun isCloseRequested(): Boolean = lifecycleMutex.withLock { closeRequested }

    /**
     * Wait for the connection to terminate.
     *
     * @param timeout how long to wait.
     */
    suspend fun awaitDisconnect(): CloseReason {
        return jervisCloseReason.await()
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
