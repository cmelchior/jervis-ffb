package com.jervisffb.net

import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.model.Field
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.rules.BB2020Rules
import com.jervisffb.engine.rules.Rules
import com.jervisffb.net.handlers.ClientMessageHandler
import com.jervisffb.net.handlers.InternalJoinHandler
import com.jervisffb.net.handlers.LeaveGameHandler
import com.jervisffb.net.handlers.StartGameHandler
import com.jervisffb.net.messages.ClientMessage
import com.jervisffb.net.messages.InternalJoinMessage
import com.jervisffb.net.messages.JervisErrorCode
import com.jervisffb.net.messages.JoinGameAsPlayerMessage
import com.jervisffb.net.messages.JoinGameAsSpectatorMessage
import com.jervisffb.net.messages.JoinGameMessage
import com.jervisffb.net.messages.LeaveGameMessage
import com.jervisffb.net.messages.ReceivedMessage
import com.jervisffb.net.messages.ServerError
import com.jervisffb.net.messages.StartGameMessage
import com.jervisffb.net.serialize.jervisNetworkSerializer
import com.jervisffb.utils.jervisLogger
import io.ktor.utils.io.CancellationException
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.readText
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

enum class GameState {
    PLANNED, // Game was created, but no teams have joined yet.
    JOINING, // Not all teams have joined yet.
    STARTING, // Teams have joined, but haven't accepted starting yet.
    ACTIVE, // Both teams have agreeded to start and the game is running.
    FINISHED, // The game has finished
//    UPLOADED,
//    BACKED_UP,
//    LOADING
}


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
    val gameId: GameId, // Unique identifier for this Game. It is required to be unique across all games on the server
    val password: Password?, // Optional password for accessing the game. This is in addition to any user auth.
    val playingTeams: List<TeamId>, // The teams in the game was predetermined up front. Only these teams can join as player clients.
    val testMode: Boolean
) {

    companion object {
        val LOG = jervisLogger()
    }

    private val messageHandlers = mapOf(
        InternalJoinMessage::class to InternalJoinHandler(this),
//        JoinGameAsPlayerMessage::class to JoinGameAsPlayerHandler(server),
//        JoinGameAsSpectatorMessage::class to JoinGameAsSpectatorMessageHandler(server),
        StartGameMessage::class to StartGameHandler(this),
        LeaveGameMessage::class to LeaveGameHandler(this),
//        GameActionMessage::class to GameActionHandler(this),
    )
    val handler = CoroutineExceptionHandler { _, exception ->
        println("GameSession threw an exception: $exception")
    }
    private val scope = CoroutineScope(Job() + CoroutineName("GameSession-${gameId.value}") + Dispatchers.Default + handler)

    // All sessions associated with this game, post messages to this queue
    // This ensures that we only update the game state
    private val incomingMessages = Channel<ReceivedMessage>(Channel.UNLIMITED)
    val out = ServerCommunication(this, parallelizeSend = !testMode)

    var state: GameState = GameState.PLANNED
    private var plannedAt: Instant = Clock.System.now()
    private val rules: Rules = BB2020Rules()
    private var game: GameEngineController? = null
    val players: MutableList<JoinedPlayerClient> = mutableListOf()
    val spectators: MutableList<JoinedSpectatorClient> = mutableListOf()

    init {
        startSession()
    }

    suspend fun addClient(connection: WebSocketSession, message: JoinGameMessage): JoinedClient {
        var newClient: JoinedClient? = null
        val command = InternalJoinMessage(
            action = {
                when (message) {
                    is JoinGameAsPlayerMessage -> {
                        val client = JoinedPlayerClient(
                            connection = connection,
                            username = message.username,
                            team = message.team!!, // For now the team is required to be sent in the JoinGame command
                            state = ClientState.ACCEPTING_GAME
                        )
                        newClient = client
                        players.add(client)
                        startClientHandler(client)
                        out.sendPlayerJoined(client.username)
                    }

                    is JoinGameAsSpectatorMessage -> {
                        val client = JoinedSpectatorClient(
                            connection = connection,
                            username = message.username,
                        )
                        newClient = client
                        spectators.add(client)
                        startClientHandler(client)
                        out.sendSpectatorJoined(client.username)
                    }
                }
                when (players.size) {
                    0, 1 -> state = GameState.JOINING
                    2 -> {
                        state = GameState.STARTING
                        out.sendStartingGameRequest(gameId, players.map { it.team!! })
                    }
                }
            }
        )

        // Handle initial Join message synchronously to prevent the WebSocketSession going out
        // of scope, which would close it.
        handleMessage(ReceivedMessage(connection, command))
        return newClient!!
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
        } ?: out.sendError(message.connection, JervisErrorCode.PROTOCOL_ERROR, "No handler found for message: $clientMessage")
    }

    private fun saveGameProgress(message: ReceivedMessage) {
        // Message was successfully handled, store current game state before processing
        // the next.
        // TODO Not implemented yet.
    }

    fun removePlayer(player: JoinedClient) {
        TODO()
    }

    fun isReadyToStart(): Boolean {
        return players.size == 2 && players.all { it.state == ClientState.READY }
    }

    private fun startSession() {
        scope.launch {
            for (message in incomingMessages) {
                handleMessage(message)
            }
        }.invokeOnCompletion {
            if (it != null && it !is CancellationException) {
                throw it
            }
        }
    }

    fun startGame() {
        if (!players.all { it.state == ClientState.READY }) {
            throw IllegalStateException("Not all players are ready to start the game.")
        }
        if (state != GameState.STARTING) {
            throw IllegalStateException("Wrong game state: $state")
        }
        if (game != null) {
            throw IllegalStateException("Game is already running.")
        }
        state = GameState.ACTIVE
        game = GameEngineController(
            rules,
            Game(
                rules,
                players[0].team!!,
                players[1].team!!,
                Field(rules.fieldWidth, rules.fieldHeight),
            ),
            server.diceRollGenerator
        ).also {
            it.startManualMode()
        }
    }

    /**
     * Gracefully stop the game session and terminate all connections.
     * The session will attempt to process all queued up events before
     * fully stopping.
     *
     * This method can be called outside the normal control (really?)
     */
    suspend fun shutdownGame(exitCode: JervisExitCode, reason: String) {
        scope.cancel()
        incomingMessages.close()
//        drainQueuedMessages(incomingMessages)

        // TODO Send close in parallel
        players.forEach {
            it.disconnect(exitCode, reason)
        }
        spectators.forEach {
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
        return players.any { it.connection == session } || spectators.any { it.connection == session }
    }

    fun getPlayerClient(session: WebSocketSession): JoinedPlayerClient? {
        return players.firstOrNull { it.connection == session }
    }

    private fun <T: ClientMessage> getHandler(type: T): ClientMessageHandler<T>? {
        val handler =  messageHandlers[type::class]?.let {
            @Suppress("UNCHECKED_CAST")
            it as ClientMessageHandler<T>
        }
        return handler
    }
}
