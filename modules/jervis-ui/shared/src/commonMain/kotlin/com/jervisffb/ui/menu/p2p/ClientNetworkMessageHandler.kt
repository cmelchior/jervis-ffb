package com.jervisffb.ui.menu.p2p

import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionId
import com.jervisffb.engine.model.Coach
import com.jervisffb.engine.model.CoachId
import com.jervisffb.engine.model.CoachType
import com.jervisffb.engine.model.Spectator
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.serialization.SerializedTeam
import com.jervisffb.net.GameId
import com.jervisffb.net.JervisClientWebSocketConnection
import com.jervisffb.net.JervisExitCode
import com.jervisffb.net.messages.AcceptGameMessage
import com.jervisffb.net.messages.ClientMessage
import com.jervisffb.net.messages.CloseHostedServerMessage
import com.jervisffb.net.messages.CoachJoinedMessage
import com.jervisffb.net.messages.CoachLeftMessage
import com.jervisffb.net.messages.ConfirmGameStartMessage
import com.jervisffb.net.messages.GameActionMessage
import com.jervisffb.net.messages.GameNotFoundMessage
import com.jervisffb.net.messages.GameReadyMessage
import com.jervisffb.net.messages.GameStartedMessage
import com.jervisffb.net.messages.GameStateSyncMessage
import com.jervisffb.net.messages.HostedTeamInfo
import com.jervisffb.net.messages.JoinGameAsCoachMessage
import com.jervisffb.net.messages.P2PClientState
import com.jervisffb.net.messages.P2PHostState
import com.jervisffb.net.messages.P2PTeamInfo
import com.jervisffb.net.messages.ServerError
import com.jervisffb.net.messages.ServerMessage
import com.jervisffb.net.messages.SpectatorJoinedMessage
import com.jervisffb.net.messages.SpectatorLeftMessage
import com.jervisffb.net.messages.SpectatorState
import com.jervisffb.net.messages.SyncGameActionMessage
import com.jervisffb.net.messages.TeamData
import com.jervisffb.net.messages.TeamJoinedMessage
import com.jervisffb.net.messages.TeamSelectedMessage
import com.jervisffb.net.messages.UpdateClientStateMessage
import com.jervisffb.net.messages.UpdateHostStateMessage
import com.jervisffb.net.messages.UpdateSpectatorStateMessage
import com.jervisffb.net.messages.UserMessage
import com.jervisffb.ui.menu.components.TeamInfo
import com.jervisffb.utils.jervisLogger
import io.ktor.websocket.CloseReason
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlin.concurrent.Volatile

sealed interface JoinResult
object JoinSuccess : JoinResult
data class JoinError(val error: Throwable) : JoinResult

/**
 * Interface for classes that can react to network messages.
 * Multiple handlers can be registered, and they will be called in
 * the order they are registered.
 */
interface ClientNetworkMessageHandler {
    fun onConnected()
    fun onConnecting()
    fun onDisconnected(reason: CloseReason)
    fun onTeamSelected(team: Team, homeTeam: Boolean)
    fun onCoachJoined(coach: Coach, isHomeCoach: Boolean)
    // Coach left willingly, i.e., a proper leave message was sent
    // Unexpected disconnects are tracked by [
    fun onCoachLeft(coach: Coach)
    fun onSpectatorJoined(spectator: Spectator)
    fun onSpectatorLeft(spectator: Spectator)
    fun onClientStateChange(newState: P2PClientState)
    fun onHostStateChange(newState: P2PHostState)
    fun onSpectatorStateChange(newState: SpectatorState)
    fun onGameSync(message: GameStateSyncMessage)
    fun updateClientState(state: P2PClientState)
    fun onConfirmGameStart(id: GameId, rules: Rules, initialActions: List<GameAction>, teams: List<TeamData>)
    fun onGameReady(id: GameId)
    fun onServerError(error: ServerError)
    fun onGameAction(producer: CoachId, serverIndex: GameActionId, action: GameAction)
}

abstract class AbstractClintNetworkMessageHandler : ClientNetworkMessageHandler {
    override fun onConnected() { }
    override fun onConnecting() { }
    override fun onDisconnected(reason: CloseReason) { }
    override fun onCoachJoined(coach: Coach, isHomeCoach: Boolean) { }
    override fun onCoachLeft(coach: Coach) { }
    override fun onSpectatorJoined(spectator: Spectator) { }
    override fun onSpectatorLeft(spectator: Spectator) { }
    override fun onTeamSelected(team: Team, homeTeam: Boolean) { }
    override fun onClientStateChange(newState: P2PClientState) { }
    override fun onHostStateChange(newState: P2PHostState) { }
    override fun onSpectatorStateChange(newState: SpectatorState) { }
    override fun onGameSync(message: GameStateSyncMessage) { }
    override fun updateClientState(state: P2PClientState) { }
    override fun onConfirmGameStart(id: GameId, rules: Rules, initialActions: List<GameAction>, teams: List<TeamData>) { }
    override fun onGameReady(id: GameId) { }
    override fun onServerError(error: ServerError) { }
    override fun onGameAction(producer: CoachId, serverIndex: GameActionId, action: GameAction) { }
}

/**
 * Class responsible for interacting with a game host using a Websocket connection.
 * This class should be responsible for mapping high-level APIs to the correct
 * web socket messages and vice versa.
 */
class ClientNetworkManager(initialNetworkHandler: ClientNetworkMessageHandler) {

    companion object {
        val LOG = jervisLogger()
    }

    private var rules: Rules? = null
    // `SupervisorJob` is required here. Otherwise, any failing child job would
    // cancel the entire scope, and all later connections would just silently hang.
    private val scope = CoroutineScope(SupervisorJob() + CoroutineName("ClintNetworkMessageHandler"))
    private var connection: JervisClientWebSocketConnection? = null

    /**
     * The connection [abortConnection] has given up on, if any. Closing it is
     * asynchronous, so this is what stops the read loop from processing more
     * messages while the connection are being closed.
     */
    @Volatile
    private var abortedConnection: JervisClientWebSocketConnection? = null

    /**
     * Job wrapping reading and writing from [connection].
     * Created for each call to [startConnection]. Any previous jobs are
     * automatically closed.
     */
    private var connectionJob: Job? = null

    /**
     * These handlers are iterated from the connection coroutines while the UI
     *  adds and removes handlers, so it must be replaced wholesale rather than
     * mutated in place.
     */
    @Volatile
    private var messageHandlers: List<ClientNetworkMessageHandler> = listOf(initialNetworkHandler)

    /** The handler belonging to the current connection attempt, see [setConnectionHandler]. */
    private var connectionHandler: ClientNetworkMessageHandler? = null

    suspend fun connectAndJoinGame(gameUrl: String, id: GameId, coachName: String, coachType: CoachType, isHost: Boolean, team: Team?) {
        startConnection(gameUrl, id, coachName)
        val teamData = team?.let { SerializedTeam.serialize(it) }
        send(JoinGameAsCoachMessage(
            gameId = id,
            username = coachName,
            password = "",
            coachName = coachName,
            coachType = coachType,
            isHost = isHost,
            team = teamData?.let {P2PTeamInfo(it) }
        ))
    }

    suspend fun sendTeamSelected(team: TeamInfo) {
        val teamInfo = if (team.teamData == null) {
            HostedTeamInfo(team.teamId)
        } else {
            P2PTeamInfo(SerializedTeam.serialize(team.teamData.model))
        }
        send(TeamSelectedMessage(teamInfo))
    }

    private suspend fun startConnection(gameUrl: String, id: GameId, coachName: String) {
        LOG.d { "[Client-$coachName] Starting a new connection: $gameUrl" }
        // Shut the previous connection down first to avoid race conditions between
        // old and new connection jobs. Cancel before closing, so the connection
        // we are deliberately replacing does not report a disconnect.
        connectionJob?.cancelAndJoin()
        connectionJob = null
        connection?.close(JervisExitCode.CLIENT_CLOSING)
        // Nothing compares equal we are about to create below, so this is just
        // releasing the previous connection for the GC to reclaim.
        abortedConnection = null

        val newConnection = JervisClientWebSocketConnection(id, gameUrl, coachName)
        connection = newConnection
        newConnection.start()
        updateState(Connecting)
        // Both coroutines work off `newConnection` rather than the field, so they can only ever
        // act on the connection they were started for.
        connectionJob = scope.launch {
            launch {
                failSafely("waiting for $coachName to disconnect") {
                    val reason = newConnection.awaitDisconnect()
                    LOG.d { "[Client-$coachName] Disconnected: $reason" }
                    updateState(Disconnected(reason))
                }
            }
            launch {
                failSafely("reading messages for $coachName") {
                    var connectedReported = false
                    // Stop as soon as a handler has failed. `handleMessage` runs on this
                    // coroutine, so the flag is always set before the next message is read.
                    while (abortedConnection !== newConnection) {
                        val message: ServerMessage = newConnection.receiveOrNull() ?: break
                        if (!connectedReported) {
                            connectedReported = true
                            updateState(Connected)
                            if (abortedConnection === newConnection) break
                        }
                        LOG.d { "[Client-$coachName] Received message: $message" }
                        handleMessage(message)
                    }
                }
            }
        }
    }

    private fun updateState(newState: ConnectionState) {
        messageHandlers.forEach { messageHandler ->
            failSafely("notifying $messageHandler about $newState") {
                when (newState) {
                    Connected -> messageHandler.onConnected()
                    Connecting -> messageHandler.onConnecting()
                    is Disconnected -> messageHandler.onDisconnected(newState.reason)
                }
            }
        }
    }

    /**
     * Run [block], turning anything it throws into a failed connection instead
     * of letting it escape.
     *
     * Handlers are supplied by the UI and reach a lot of code, so a single
     * broken one may not take down the coroutine reading the connection, let
     * alone the scope that owns it. Everything else still runs, but the failure
     * is not swallowed: see [abortConnection] for why merely logging it is not
     * enough.
     */
    private inline fun failSafely(what: String, block: () -> Unit) {
        try {
            block()
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: Throwable) {
            LOG.e { "Failed while $what:\n${ex.stackTraceToString()}" }
            abortConnection(what, ex)
        }
    }

    /**
     * Give up on the connection that just failed, reporting
     * [JervisExitCode.UNEXPECTED_ERROR] to the server.
     *
     * Only logging the error would leave a client that is connected but no
     * longer working: the read loop or the disconnect watcher is gone while the
     * socket stays open, so nothing ever moves the UI out of whatever state it
     * was waiting in, and the server keeps a session the opposing coach is
     * waiting on. Closing instead completes
     * [JervisClientWebSocketConnection.awaitDisconnect], which surfaces the
     * failure to the handlers as an ordinary [Disconnected], and tells the
     * server why we left so it can release the seat.
     *
     * The close runs on [scope] rather than inline, so the caller can finish
     * dispatching the message it is on to its remaining handlers. Nothing after
     * that is delivered: the socket is still open until the close lands, and
     * letting the read loop drain what the server already sent would advance the
     * UI past the message it just choked on. [abortedConnection] stops it.
     *
     * Repeat calls are harmless. Only the first one completes the close reason,
     * so what the coach and the server are told is the failure that started it
     * all rather than whatever fell over afterwards.
     */
    private fun abortConnection(what: String, error: Throwable) {
        // Captured now: by the time the coroutine runs, `connection` may already
        // have been replaced by a later connection attempt that is perfectly fine.
        val failedConnection = connection ?: return
        abortedConnection = failedConnection
        scope.launch {
            failedConnection.close(
                JervisExitCode.UNEXPECTED_ERROR,
                "Client failed while $what: ${error.message ?: error::class.simpleName}"
            )
        }
    }

    private fun handleMessage(message: ServerMessage) {
        // `messageHandlers` is immutable, so handlers can be added or removed while we iterate.
        messageHandlers.forEach { messageHandler ->
            failSafely("delivering $message to $messageHandler") {
                when (message) {
                    is ConfirmGameStartMessage -> messageHandler.onConfirmGameStart(message.gameId, message.rules, message.initialActions, message.teams)
                    is GameReadyMessage -> messageHandler.onGameReady(message.gameId)
                    is CoachJoinedMessage -> messageHandler.onCoachJoined(message.coach, message.isHomeCoach)
                    is ServerError -> messageHandler.onServerError(message)
                    is TeamJoinedMessage -> {
                        val gameRules = rules ?: throw IllegalStateException("Rules have not been sent by the server yet.")
                        messageHandler.onTeamSelected(message.getTeam(gameRules), message.isHomeTeam)
                    }
                    is CoachLeftMessage -> messageHandler.onCoachLeft(message.coach)
                    is UpdateClientStateMessage -> messageHandler.updateClientState(message.state)
                    is UpdateHostStateMessage -> messageHandler.onHostStateChange(message.state)
                    is UpdateSpectatorStateMessage -> messageHandler.onSpectatorStateChange(message.state)
                    is GameStateSyncMessage -> {
                        rules = message.rules
                        messageHandler.onGameSync(message)
                    }
                    is SyncGameActionMessage -> messageHandler.onGameAction(message.producer, message.serverIndex, message.action)
                    // Message types the client does not act on yet. They are just logged
                    // to prevent them taking down the server.
                    is GameNotFoundMessage,
                    is SpectatorJoinedMessage,
                    is SpectatorLeftMessage,
                    is UserMessage -> LOG.w { "[Client] Ignoring unsupported message: $message" }
                }
            }
        }
    }

    fun cancelJoin() {
        scope.launch {
            connection?.close()
            // Only the current connection is cancelled. Cancelling `scope` would leave this
            // manager permanently unable to run a connection, including any later attempt.
            connectionJob?.cancelAndJoin()
            connectionJob = null
        }
    }

    /**
     * Register the handler for the current connection attempt, replacing the one from any earlier
     * attempt. Callers build a fresh handler every time they join or disconnect, so registering
     * them with [addMessageHandler] piled them up and ran the same side effect once per attempt
     * made so far.
     */
    fun setConnectionHandler(messageHandler: ClientNetworkMessageHandler) {
        val previous = connectionHandler
        connectionHandler = messageHandler
        messageHandlers = messageHandlers.filterNot { it === previous } + messageHandler
    }

    /**
     * Register a handler that lives for as long as this manager does, e.g. the one forwarding game
     * actions. Use [setConnectionHandler] for handlers tied to a single connection attempt.
     */
    fun addMessageHandler(messageHandler: ClientNetworkMessageHandler) {
        messageHandlers = messageHandlers + messageHandler
    }

    fun removeMessageHandler(messageHandler: ClientNetworkMessageHandler) {
        val remaining = messageHandlers.filterNot { it === messageHandler }
        if (remaining.size == messageHandlers.size) {
            error("Attempted to remove handler that was not registered: $messageHandler")
        }
        messageHandlers = remaining
        if (connectionHandler === messageHandler) {
            connectionHandler = null
        }
    }

    private suspend fun send(message: ClientMessage) {
        connection!!.send(message)
    }

    suspend fun disconnect() {
        connection?.close(JervisExitCode.CLIENT_CLOSING)
    }

    suspend fun sendStartGame(startGame: Boolean) {
        val msg = AcceptGameMessage(startGame)
        send(msg)
    }

    suspend fun sendClientAction(index: GameActionId, action: GameAction) {
        val msg = GameActionMessage(index, action)
        send(msg)
    }

    suspend fun sendGameStarted(id: GameId) {
        val msg = GameStartedMessage(id)
        send(msg)
    }

    suspend fun sendCloseHostedServer() {
        val msg = CloseHostedServerMessage
        send(msg)
    }

}
