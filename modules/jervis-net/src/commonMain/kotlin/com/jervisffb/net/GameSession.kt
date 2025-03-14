package com.jervisffb.net

import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.GameSettings
import com.jervisffb.engine.model.Coach
import com.jervisffb.engine.model.CoachId
import com.jervisffb.engine.model.Field
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Spectator
import com.jervisffb.engine.model.SpectatorId
import com.jervisffb.engine.model.Team
import com.jervisffb.net.handlers.ClientMessageHandler
import com.jervisffb.net.handlers.GameActionHandler
import com.jervisffb.net.handlers.GameStartedHandler
import com.jervisffb.net.handlers.InternalGameActionMessageHandler
import com.jervisffb.net.handlers.InternalJoinHandler
import com.jervisffb.net.handlers.LeaveGameHandler
import com.jervisffb.net.handlers.StartGameHandler
import com.jervisffb.net.handlers.TeamSelectedHandler
import com.jervisffb.net.messages.ClientMessage
import com.jervisffb.net.messages.GameActionMessage
import com.jervisffb.net.messages.GameStartedMessage
import com.jervisffb.net.messages.GameState
import com.jervisffb.net.messages.InternalClientMessage
import com.jervisffb.net.messages.InternalGameActionMessage
import com.jervisffb.net.messages.InternalJoinMessage
import com.jervisffb.net.messages.JervisErrorCode
import com.jervisffb.net.messages.JoinGameAsCoachMessage
import com.jervisffb.net.messages.JoinGameAsSpectatorMessage
import com.jervisffb.net.messages.JoinGameMessage
import com.jervisffb.net.messages.LeaveGameMessage
import com.jervisffb.net.messages.P2PClientState
import com.jervisffb.net.messages.P2PHostState
import com.jervisffb.net.messages.P2PTeamInfo
import com.jervisffb.net.messages.ReceivedMessage
import com.jervisffb.net.messages.ServerError
import com.jervisffb.net.messages.SpectatorState
import com.jervisffb.net.messages.StartGameMessage
import com.jervisffb.net.messages.TeamSelectedMessage
import com.jervisffb.net.serialize.jervisNetworkSerializer
import com.jervisffb.utils.jervisLogger
import io.ktor.utils.io.CancellationException
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.readText
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString


/**
 * Main class for controlling the entire lifecycle around a single game.
 *
 * This takes all incoming websocket connections and start consuming messages
 * from them. All these messages are placed in a single message queue, which
 * means that
 *
 * This class is not thread-safe. So all modifications should ideally happen
 * through incomingMessages.
 */
class GameSession(
    val server: LightServer,
    val gameSettings: GameSettings,
    val gameId: GameId, // Unique identifier for this Game. It is required to be unique across all games on the server
    val password: Password?, // Optional password for accessing the game. This is in addition to any user auth.
    val hostCoach: CoachId,
    val hostTeam: Team,
    val clientCoach: CoachId? = null,
    val clientTeam: Team? = null,
    val testMode: Boolean = false
) {

    companion object {
        val LOG = jervisLogger()
    }

    private val sessionClosed = CompletableDeferred<Unit>()

    private val messageHandlers = mapOf(
        InternalJoinMessage::class to InternalJoinHandler(this),
//        JoinGameAsPlayerMessage::class to JoinGameAsPlayerHandler(server),
//        JoinGameAsSpectatorMessage::class to JoinGameAsSpectatorMessageHandler(server),
        StartGameMessage::class to StartGameHandler(this),
        LeaveGameMessage::class to LeaveGameHandler(this),
        TeamSelectedMessage::class to TeamSelectedHandler(this),
        GameActionMessage::class to GameActionHandler(this),
        GameStartedMessage::class to GameStartedHandler(this),
        InternalGameActionMessage::class to InternalGameActionMessageHandler(this)
    )
    val handler = CoroutineExceptionHandler { _, exception ->
        println("GameSession threw an exception: $exception")
    }

    // TODO Should probably be single-threaded
    val scope = CoroutineScope(Job() + CoroutineName("GameSession-${gameId.value}") + Dispatchers.Default + handler)

    // All sessions associated with this game, post messages to this queue
    // This ensures that we only update the game state from a single thread
    private val incomingMessages = Channel<ReceivedMessage>(Channel.UNLIMITED)
    val out = ServerCommunication(this, scope, parallelizeSend = !testMode)

    var state: GameState = GameState.PLANNED
    private var plannedAt: Instant = Clock.System.now()

    val coaches: MutableList<JoinedP2PCoach> = mutableListOf()
    val spectators: MutableList<JoinedSpectator> = mutableListOf()
    var hostState: P2PHostState = P2PHostState.START
    var clientState: P2PClientState = P2PClientState.START
    var spectatorState: SpectatorState = SpectatorState.START

    val homeTeam: Team?
        // For now, the Host is always the Home team
        get() { return coaches.filterIsInstance<JoinedP2PHost>().firstOrNull()?.team }
    val awayTeam: Team?
        get() { return coaches.filterIsInstance<JoinedP2PClient>().firstOrNull()?.team }
    val host: JoinedP2PHost?
        get() { return coaches.filterIsInstance<JoinedP2PHost>().firstOrNull() }
    val client: JoinedP2PClient?
        get() { return coaches.filterIsInstance<JoinedP2PClient>().firstOrNull() }

    var game: GameEngineController? = null

    init {
        startSession()
    }

    suspend fun addClient(connection: JervisNetworkWebSocketConnection, message: JoinGameMessage): JoinedClient {
        var newClient: JoinedClient? = null
        var exit: Boolean = false
        val mutex = CompletableDeferred<Unit>()
        val command = InternalJoinMessage(
            action = {
                try {
                    when (message) {
                        is JoinGameAsCoachMessage -> {
                            // For now, host is always valid since they will join immediately after
                            // the server is started, and we just assume the client is always valid as well
                            // In the case of a game starting again, we ignore any teams sent and just
                            // reuse the ones from the save game.
                            val client = if (message.isHost) {
                                val homeTeam = if (gameSettings.initialActions.isNotEmpty()) hostTeam else (message.team as P2PTeamInfo?)?.team
                                JoinedP2PHost(
                                    connection = connection,
                                    coach = Coach(CoachId((coaches.size + 1).toString()), message.coachName),
                                    state = P2PHostState.JOIN_SERVER,
                                    team = homeTeam
                                )
                            } else {
                                val awayTeam = if (gameSettings.initialActions.isNotEmpty()) clientTeam else (message.team as P2PTeamInfo?)?.team
                                JoinedP2PClient(
                                    connection = connection,
                                    coach = Coach(CoachId((coaches.size + 1).toString()), message.coachName),
                                    state = P2PClientState.JOIN_SERVER,
                                    team = awayTeam
                                )
                            }

                            // Send GameSync before adding the client. This way, both joining and current clients
                            // have the same flow of state. Otherwise, the joining client would get a sync with itself
                            // already present and then straight after receive a CoachJoin with itself again.
                            out.sendGameStateSync(client, this)
                            newClient = client
                            coaches.add(client)
                            startClientHandler(client)
                            // For now, the host is always marked as the "Home Team"
                            out.sendCoachJoined(client.coach, message.isHost)
                            // Host is required to send team as part of join message. This is checked before coming here.
                            client.team?.let { team ->
                                out.sendTeamJoined(message.isHost, team)
                            }
                            if (message.isHost) {
                                hostState = P2PHostState.WAIT_FOR_CLIENT
                                out.sendHostStateUpdate(hostState)
                            } else {
                                if (gameSettings.initialActions.isNotEmpty()) {
                                    val msg = ReceivedMessage(connection, TeamSelectedMessage(P2PTeamInfo(clientTeam!!)))
                                    incomingMessages.send(msg)
                                } else {
                                    clientState = P2PClientState.SELECT_TEAM
                                    out.sendClientStateUpdate(clientState)
                                }
                            }
                        }

                        is JoinGameAsSpectatorMessage -> {
                            val client = JoinedSpectator(
                                connection = connection,
                                spectator = Spectator(SpectatorId((spectators.size + 1).toString()), message.spectatorName),
                            )
                            newClient = client
                            spectators.add(client)
                            startClientHandler(client)
                            out.sendSpectatorJoined(client.spectator)
                            out.sendGameStateSync(client, this)
                        }
                    }
                } finally {
                    mutex.complete(Unit)
                }
            }
        )

        // Block waiting for message to be processed. Needed to prevent WebSocketSession going out
        // of scope, which would close it.
        // If an exception is thrown, the mutex will still be released, but `newClient` will throw,
        // this will be caught futher up and still resulting in the connection being closed.
        incomingMessages.send(ReceivedMessage(connection, command))
        mutex.await()
        return newClient!!
    }

    suspend fun sendInternalMessage(connection: JervisNetworkWebSocketConnection?, message: InternalClientMessage) {
        incomingMessages.send(ReceivedMessage(connection, message))
    }

    private fun startClientHandler(client: JoinedClient) {
        // Launch a coroutine that consumes all messages from the client and
        // put them on the shared message queue for this game session.
        scope.launch {
            for (message in client.connection.incoming) {
                try {
                    when (message) {
                        is Frame.Text -> {
                            val clientMessage = jervisNetworkSerializer.decodeFromString<ClientMessage>(message.readText())
                            incomingMessages.send(ReceivedMessage(client.connection, clientMessage))
                        }
//                    is Frame.Binary -> TODO()
//                    is Frame.Close -> TODO()
//                    is Frame.Ping -> TODO()
//                    is Frame.Pong -> TODO()
                        else -> TODO("Unsupported type: $message")
                    }
                } catch (ex: Throwable) {
                    if (ex is CancellationException) throw ex
                    println("Error: $ex")
                    val error = ServerError(JervisErrorCode.READ_MESSAGE_ERROR, ex.stackTraceToString())
                    sendError(client, error)
                }
            }
        }
    }

    private suspend fun sendError(client: JoinedClient, error: ServerError) {
        try {
            val message = jervisNetworkSerializer.encodeToString(error)
            client.connection.outgoing.send(Frame.Text(message))
        } catch (ex: Throwable) {
            if (ex is CancellationException) throw ex
            // Something went wrong sending the message to the client
            // We should probably remove the client if that happens.
            TODO(ex.stackTraceToString())
        }
    }

    private suspend fun handleMessage(message: ReceivedMessage) {
        val clientMessage = message.message
        getHandler(clientMessage)?.let { handler ->
            try {
                handler.handleMessage(clientMessage, message.connection)
                saveGameProgress(message)
            } catch (ex: Throwable) {
                if (ex is CancellationException) throw ex
                // All known errors scenarios should have been handled through
                // ServerCommunication.sendError(). So if we get here, it means
                // an exception happened during the game that was unexpected and
                // unhandled. To be on the safe side, we treat this as an
                // unrecoverable error and shut down the game forcing a complete
                // restart.
                shutdownGame(JervisExitCode.UNEXPECTED_ERROR, ex.stackTraceToString())
            }
        } ?: out.sendError(message.connection, message.message, JervisErrorCode.PROTOCOL_ERROR, "No handler found for message: $clientMessage")
    }

    private fun saveGameProgress(message: ReceivedMessage) {
        // Message was successfully handled, store current game state before processing
        // the next.
        // TODO Not implemented yet.
    }

    suspend fun removeClient(client: JoinedClient) {
        // What should the state be if a player leaves in the middle
        coaches.remove(client)
        when (client) {
            is JoinedSpectator -> out.sendSpectatorLeft(client.spectator)
            is JoinedP2PCoach -> out.sendCoachLeft(client.coach)
        }
    }

    fun isReadyToStart(): Boolean {
        return coaches.size == 2 && coaches.all { it.hasAcceptedGame }
    }

    private fun startSession() {
        scope.launch {
            state = GameState.JOINING
            for (message in incomingMessages) {
                LOG.d { "Received message from ${message.connection?.username}: $message" }
                handleMessage(message)
            }
        }.invokeOnCompletion {
            sessionClosed.complete(Unit)
            if (it != null && it !is CancellationException) {
                throw it
            }
        }
    }

    fun startGame() {
        if (!coaches.all { it.hasAcceptedGame }) {
            throw IllegalStateException("Not all players are ready to start the game.")
        }
        if (state != GameState.STARTING) {
            throw IllegalStateException("Wrong game state: $state")
        }
        if (game != null) {
            throw IllegalStateException("Game is already running.")
        }
        state = GameState.ACTIVE
        val rules = gameSettings.gameRules
        game = GameEngineController(
            Game(
                rules,
                coaches[0].team!!,
                coaches[1].team!!,
                Field(rules.fieldWidth, rules.fieldHeight),
            ),
            gameSettings.initialActions
        ).also {
            it.startManualMode()
        }
    }

    /**
     * Gracefully stop the game session and terminate all connections.
     * The session will attempt to process all queued up events before
     * fully stopping.
     *
     * This method can be called outside the normal control of handling messages.
     */
    suspend fun shutdownGame(exitCode: JervisExitCode, reason: String) {
        scope.cancel()
        incomingMessages.close()
        sessionClosed.await() // Allow incoming message queue to drain // drainQueuedMessages(incomingMessages)

        // TODO Send close in parallel
        coaches.toList().forEach {
            it.disconnect(exitCode, reason)
        }
        spectators.toList().forEach {
            it.disconnect(exitCode, reason)
        }
    }

    private fun drainQueuedMessages(incomingMessages: Channel<ReceivedMessage>) {
        val messages = mutableListOf<ReceivedMessage>()
        while (true) {
            val result = incomingMessages.tryReceive() // Non-blocking receive
            if (result.isSuccess) {
                messages.add(result.getOrNull()!!)
            } else {
                break // Exit when the channel is empty
            }
        }
    }

    fun containsSession(session: WebSocketSession): Boolean {
        // TODO Optimize this lookup?
        return coaches.any { it.connection == session } || spectators.any { it.connection == session }
    }

    fun getPlayerClient(session: WebSocketSession): JoinedP2PCoach? {
        return coaches.firstOrNull { it.connection == session }
    }

    private fun <T: ClientMessage> getHandler(type: T): ClientMessageHandler<T>? {
        val handler =  messageHandlers[type::class]?.let {
            @Suppress("UNCHECKED_CAST")
            it as ClientMessageHandler<T>
        }
        return handler
    }
}
