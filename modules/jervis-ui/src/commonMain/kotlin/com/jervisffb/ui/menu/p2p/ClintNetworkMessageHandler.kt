package com.jervisffb.ui.menu.p2p

import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionId
import com.jervisffb.engine.model.Coach
import com.jervisffb.engine.model.CoachId
import com.jervisffb.engine.model.Spectator
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.Rules
import com.jervisffb.net.GameId
import com.jervisffb.net.JervisClientWebSocketConnection
import com.jervisffb.net.JervisExitCode
import com.jervisffb.net.messages.ClientMessage
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
import com.jervisffb.net.messages.StartGameMessage
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
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

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
    override fun onSpectatorJoined(specator: Spectator) { }
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

    private var currentState: ConnectionState = Disconnected(CloseReason(CloseReason.Codes.NORMAL, ""))
    private val scope = CoroutineScope(CoroutineName("ClintNetworkMessageHandler"))
    private var connection: JervisClientWebSocketConnection? = null
    private val messageHandlers: MutableList<ClientNetworkMessageHandler> = mutableListOf(initialNetworkHandler)

    suspend fun connectAndJoinGame(gameUrl: String, id: GameId, coachName: String, isHost: Boolean, team: Team?) {
        startConnection(gameUrl, id, coachName)
        send(JoinGameAsCoachMessage(
            gameId = id,
            username = coachName,
            password = "",
            coachName = coachName,
            isHost = isHost,
            team = if (team != null) P2PTeamInfo(team) else null
        ))
    }

    suspend fun sendTeamSelected(team: TeamInfo) {
        val teamInfo = if (team.teamData == null) {
            HostedTeamInfo(team.teamId)
        } else {
            P2PTeamInfo(team.teamData)
        }
        send(TeamSelectedMessage(teamInfo))
    }

    private fun startConnection(gameUrl: String, id: GameId, coachName: String) {
        connection = JervisClientWebSocketConnection(id, gameUrl, coachName).also {
            it.start()
            updateState(Connected)
        }
        scope.launch {
            val reason = connection!!.awaitDisconnect()
            LOG.d { "[Client] Disconnected: $reason" }
            updateState(Disconnected(reason))
        }
        scope.launch {
            var message: ServerMessage? = null
            while (connection?.receiveOrNull().also { message = it } != null) {
                LOG.d { "[Client] Received message: $message" }
                handleMessage(message)
            }
        }
    }

    private fun updateState(newState: ConnectionState) {
        currentState = newState
        messageHandlers.toList().forEach { messageHandler ->
            when (newState) {
                Connected -> messageHandler.onConnected()
                Connecting -> messageHandler.onConnecting()
                is Disconnected -> messageHandler.onDisconnected(newState.reason)
            }
        }
    }

    private fun handleMessage(message: ServerMessage?) {
        // Create a snapshot of the handlers, so they can be removed safely while iterating
        messageHandlers.toList().forEach { messageHandler ->
            when (message) {
                is ConfirmGameStartMessage -> messageHandler.onConfirmGameStart(message.gameId, message.rules, message.initialActions, message.teams)
                is GameNotFoundMessage -> TODO()
                is GameReadyMessage -> messageHandler.onGameReady(message.gameId)
                is CoachJoinedMessage -> messageHandler.onCoachJoined(message.coach, message.isHomeCoach)
                is ServerError -> messageHandler.onServerError(message)
                is TeamJoinedMessage -> messageHandler.onTeamSelected(message.getTeam(), message.isHomeTeam)
                is CoachLeftMessage -> messageHandler.onCoachLeft(message.coach)
                is SpectatorJoinedMessage -> TODO()
                is SpectatorLeftMessage -> TODO()
                is UserMessage -> TODO()
                is UpdateClientStateMessage -> messageHandler.updateClientState(message.state)
                is UpdateHostStateMessage -> messageHandler.onHostStateChange(message.state)
                is UpdateSpectatorStateMessage -> messageHandler.onSpectatorStateChange(message.state)
                is GameStateSyncMessage -> messageHandler.onGameSync(message)
                is SyncGameActionMessage -> messageHandler.onGameAction(message.producer, message.serverIndex, message.action)
                null -> TODO()
            }
        }
    }

    fun cancelJoin() {
        scope.launch {
            connection?.close()
            scope.cancel("Manually cancelled connection")
        }
    }

    fun addMessageHandler(messageHandler: ClientNetworkMessageHandler) {
        messageHandlers.add(messageHandler)
    }

    fun removeMessageHandler(messageHandler: ClientNetworkMessageHandler) {
        if (!messageHandlers.remove(messageHandler)) {
            error("Attempted to remove handler that was not registered: $messageHandler")
        }
    }

    private suspend fun send(message: ClientMessage) {
        connection!!.send(message)
    }

    suspend fun disconnect() {
        connection?.close(JervisExitCode.CLIENT_CLOSING)
    }

    suspend fun sendStartGame(startGame: Boolean) {
        val msg = StartGameMessage(startGame)
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

}
