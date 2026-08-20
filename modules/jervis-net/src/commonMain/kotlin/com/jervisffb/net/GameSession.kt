@file:OptIn(ExperimentalTime::class)

package com.jervisffb.net

import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.GameSettings
import com.jervisffb.engine.model.Coach
import com.jervisffb.engine.model.CoachId
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Spectator
import com.jervisffb.engine.model.SpectatorId
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.serialization.SerializedTeam
import com.jervisffb.net.handlers.AcceptGameHandler
import com.jervisffb.net.handlers.ClientMessageHandler
import com.jervisffb.net.handlers.CloseHostedServerHandler
import com.jervisffb.net.handlers.GameActionHandler
import com.jervisffb.net.handlers.GameStartedHandler
import com.jervisffb.net.handlers.InternalGameActionMessageHandler
import com.jervisffb.net.handlers.InternalJoinHandler
import com.jervisffb.net.handlers.LeaveGameHandler
import com.jervisffb.net.handlers.TeamSelectedHandler
import com.jervisffb.net.messages.AcceptGameMessage
import com.jervisffb.net.messages.ClientMessage
import com.jervisffb.net.messages.CloseHostedServerMessage
import com.jervisffb.net.messages.GameActionMessage
import com.jervisffb.net.messages.GameStartedMessage
import com.jervisffb.net.messages.GameState
import com.jervisffb.net.messages.InternalClientMessage
import com.jervisffb.net.messages.InternalGameActionMessage
import com.jervisffb.net.messages.InternalJoinMessage
import com.jervisffb.net.messages.JoinGameAsCoachMessage
import com.jervisffb.net.messages.JoinGameAsSpectatorMessage
import com.jervisffb.net.messages.JoinGameMessage
import com.jervisffb.net.messages.LeaveGameMessage
import com.jervisffb.net.messages.P2PClientState
import com.jervisffb.net.messages.P2PHostState
import com.jervisffb.net.messages.P2PTeamInfo
import com.jervisffb.net.messages.ProtocolErrorServerError
import com.jervisffb.net.messages.ReadMessageServerError
import com.jervisffb.net.messages.ReceivedMessage
import com.jervisffb.net.messages.ServerError
import com.jervisffb.net.messages.SpectatorState
import com.jervisffb.net.messages.TeamSelectedMessage
import com.jervisffb.net.serialize.jervisNetworkSerializer
import com.jervisffb.utils.jervisLogger
import com.jervisffb.utils.multiThreadDispatcher
import com.jervisffb.utils.singleThreadDispatcher
import io.ktor.utils.io.CancellationException
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.readText
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.Uuid


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
    val testMode: Boolean = false,
    val random: Random = Random.Default,
) {

    companion object {
        val LOG = jervisLogger()
    }

    private val sessionClosed = CompletableDeferred<Unit>()

    private val messageHandlers = mapOf(
        InternalJoinMessage::class to InternalJoinHandler(this),
//        JoinGameAsPlayerMessage::class to JoinGameAsPlayerHandler(server),
//        JoinGameAsSpectatorMessage::class to JoinGameAsSpectatorMessageHandler(server),
        AcceptGameMessage::class to AcceptGameHandler(this),
        CloseHostedServerMessage::class to CloseHostedServerHandler(this),
        LeaveGameMessage::class to LeaveGameHandler(this),
        TeamSelectedMessage::class to TeamSelectedHandler(this),
        GameActionMessage::class to GameActionHandler(this),
        GameStartedMessage::class to GameStartedHandler(this),
        InternalGameActionMessage::class to InternalGameActionMessageHandler(this)
    )
    val handler = CoroutineExceptionHandler { _, exception ->
        println("GameSession threw an exception: $exception")
    }

    // Is single-threaded so control exactly how messages go in and out.
    // This is required so we do not accidentially start handling incoming
    // messages out of order.
    // Hmm, that is not true. Input is being synchronized elsewhere...test it
    val networkScope = CoroutineScope(Job() + CoroutineName("GameSession-${gameId.value}[Network]") + multiThreadDispatcher("GameNetworkThread-${gameId.value}") + handler)
    val gameEventScope = CoroutineScope(Job() + CoroutineName("GameSession-${gameId.value}[Network]") + singleThreadDispatcher("GameActionThread-${gameId.value}") + handler)

    // All sessions associated with this game, post messages to this queue
    // This ensures that we only update the game state from a single thread
    private val incomingMessages = Channel<ReceivedMessage>(Channel.UNLIMITED)
    val out = ServerCommunication(this, networkScope, parallelizeSend = !testMode)

    var state: GameState = GameState.PLANNED
    private var plannedAt: Instant = Clock.System.now()

    val coaches: MutableList<JoinedP2PCoach> = mutableListOf()
    val spectators: MutableList<JoinedSpectator> = mutableListOf()
    var hostState: P2PHostState = P2PHostState.JOIN_SERVER
    var clientState: P2PClientState = P2PClientState.SELECT_TEAM
    var spectatorState: SpectatorState = SpectatorState.JOIN_HOST

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
                            val coach = Coach(CoachId(Uuid.random().toHexString()), message.coachName, message.coachType)
                            val client = createConnectedCoach(message, coach, connection)

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

                            // TODO The below branchs should be refactored into something more managable.
                            // A coach can arrive with a team already set: the Host always does, and
                            // the Client does in the save game flow. Deriving the state from the
                            // teams the session knows about, rather than assuming a coach joins into
                            // an empty game, is what keeps the two cases below from going wrong.
                            if (gameSettings.initialActions.isNotEmpty() && !message.isHost) {
                                // Save game: the Client plays the team stored in the save file, so
                                // put it through the normal selection path rather than picking one.
                                val serializedTeam = SerializedTeam.serialize(clientTeam!!)
                                val msg = ReceivedMessage(connection, TeamSelectedMessage(P2PTeamInfo(serializedTeam)))
                                incomingMessages.send(msg)
                            } else if (homeTeam != null && awayTeam != null) {
                                // Both teams are known the moment this coach joined, so the game is
                                // ready to be accepted. Nothing else covers this: the check for all
                                // teams being selected only runs when a `TeamSelectedMessage`
                                // arrives, so the coaches would otherwise sit and wait for a step
                                // that had already happened.
                                requestGameStartIfAllTeamsSelected()
                            } else if (message.isHost) {
                                hostState = P2PHostState.WAIT_FOR_CLIENT
                                out.sendHostStateUpdate(hostState)
                                // A Client that arrived before the Host was left waiting rather than being asked to
                                // pick a team (note, this should never happen in normal scenarios as the Host joins
                                // immediately after starting the server, but it could happen if the URL is known
                                // beforehand and the client is aggressively pushing a "join" button).
                                // Either way, it can only pick once it can see which team the Host took.
                                val joinedClient = this@GameSession.client
                                if (joinedClient != null && joinedClient.team == null) {
                                    clientState = P2PClientState.SELECT_TEAM
                                    out.sendClientStateUpdate(clientState)
                                }
                            } else if (homeTeam != null) {
                                clientState = P2PClientState.SELECT_TEAM
                                out.sendClientStateUpdate(clientState)
                            } else {
                                // Otherwise the Host has not joined its own server yet. The game
                                // exists as soon as the server is listening, so a Client can get
                                // here first. Asking it to pick now would show a team list where
                                // the Host's team is not marked as taken, so leave it waiting and
                                // let the Host's own join send the update.
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

    private fun createConnectedCoach(
        message: JoinGameAsCoachMessage,
        coach: Coach,
        connection: JervisNetworkWebSocketConnection
    ): JoinedP2PCoach {
        return when (message.isHost) {
            true -> {
                // Message is from a Host
                val homeTeam: Team? = when (gameSettings.initialActions.isNotEmpty()) {
                    true -> hostTeam
                    false -> {
                        (message.team as P2PTeamInfo?)?.team?.let {
                            SerializedTeam.deserialize(gameSettings.gameRules, it, coach)
                        }
                    }
                }
                JoinedP2PHost(
                    connection = connection,
                    coach = coach,
                    state = P2PHostState.JOIN_SERVER,
                    team = homeTeam
                )
            }
            false -> {
                // Message is from a Client
                val awayTeam: Team? = when (gameSettings.initialActions.isNotEmpty()) {
                    true -> clientTeam
                    false -> {
                        (message.team as P2PTeamInfo?)?.team?.let {
                            SerializedTeam.deserialize(gameSettings.gameRules, it, coach)
                        }
                    }
                }
                JoinedP2PClient(
                    connection = connection,
                    coach = coach,
                    state = P2PClientState.JOIN_SERVER,
                    team = awayTeam
                )
            }
        }
    }

    /**
     * Ask both coaches to confirm the game, but only once both teams are known.
     * This is the point where a game moves from [GameState.JOINING] to
     * [GameState.STARTING], and it can be reached either by a coach selecting a
     * team or by a coach joining with one already set.
     */
    suspend fun requestGameStartIfAllTeamsSelected() {
        val home = homeTeam
        val away = awayTeam
        if (home == null || away == null) {
            state = GameState.JOINING
            return
        }
        state = GameState.STARTING
        // Always Home (Host) first, away (Client) second.
        out.sendStartingGameRequest(
            gameId,
            gameSettings.gameRules,
            gameSettings.initialActions,
            listOf(home, away),
        )
        hostState = P2PHostState.ACCEPT_GAME
        clientState = P2PClientState.ACCEPT_GAME
        out.sendHostStateUpdate(hostState)
        out.sendClientStateUpdate(clientState)
    }

    suspend fun sendInternalMessage(connection: JervisNetworkWebSocketConnection?, message: InternalClientMessage) {
        incomingMessages.send(ReceivedMessage(connection, message))
    }

    private fun startClientHandler(client: JoinedClient) {
        // Launch a coroutine that consumes all messages from the client and
        // put them on the shared message queue for this game session.
        gameEventScope.launch {
            for (message in client.connection.incoming) {
                try {
                    when (message) {
                        is Frame.Text -> {
                            val clientMessage = jervisNetworkSerializer.decodeFromString<ClientMessage>(message.readText())
                            LOG.i { "[Server] [${client.connection.username}] Received message: $clientMessage" }
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
                    LOG.e { ex.stackTraceToString() }
                    val error = ReadMessageServerError(ex.stackTraceToString())
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
                LOG.e { "Unexpected error, stopping game:\n$ex. ${ex.stackTraceToString()}" }
                if (ex is CancellationException) throw ex
                // All known errors scenarios should have been handled through
                // ServerCommunication.sendError(). So if we get here, it means
                // an exception happened during the game that was unexpected and
                // unhandled. To be on the safe side, we treat this as an
                // unrecoverable error and shut down the game forcing a complete
                // restart.
                LOG.e { "Unexpected error, stopping game:\n$ex. ${ex.stackTraceToString()}" }
                shutdownGame(JervisExitCode.UNEXPECTED_ERROR, ex.stackTraceToString())
            }
        } ?: out.sendError(message.connection, ProtocolErrorServerError("No handler found for message: $clientMessage"))
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
            is JoinedSpectator -> {
                out.sendSpectatorLeft(client.spectator)
            }
            is JoinedP2PCoach -> {
                when (client) {
                    is JoinedP2PClient -> clientState = P2PClientState.SELECT_TEAM
                    is JoinedP2PHost -> hostState = P2PHostState.WAIT_FOR_CLIENT
                }
                out.sendCoachLeft(client.coach)
            }
        }
    }

    fun isReadyToStart(): Boolean {
        return coaches.size == 2 && coaches.all { it.hasAcceptedGame }
    }

    private fun startSession() {
        gameEventScope.launch {
            state = GameState.JOINING
            for (message in incomingMessages) {
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
    fun shutdownGame(exitCode: JervisExitCode, reason: String): Job {
        // Launching on `networkScope, because callers might be on `gameEventScope`.
        // Waiting for `sessionClosed` from that scope would deadlock the event loop before
        // it gets a chance to observe the closed channel.
        return networkScope.launch {
            LOG.i { "[Server] Shutting down game '${gameId.value}' ($exitCode): $reason"}
            incomingMessages.close()
            sessionClosed.join() // Allow incoming message queue to drain // drainQueuedMessages(incomingMessages)

            // TODO Send close in parallel
            coaches.toList().forEach {
                LOG.i { "[Server] Disconnecting: ${it.connection.username}" }
                it.disconnect(exitCode, reason)
            }
            spectators.toList().forEach {
                LOG.i { "[Server] Disconnecting: ${it.connection.username}" }
                it.disconnect(exitCode, reason)
            }
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
