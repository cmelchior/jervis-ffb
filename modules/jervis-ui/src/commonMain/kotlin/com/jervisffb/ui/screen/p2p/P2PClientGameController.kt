package com.jervisffb.ui.screen.p2p

import com.jervisffb.engine.GameRunner
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.model.Coach
import com.jervisffb.engine.model.Spectator
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.utils.InvalidActionException
import com.jervisffb.net.GameId
import com.jervisffb.net.LightServer
import com.jervisffb.net.messages.GameStateSyncMessage
import com.jervisffb.net.messages.JervisErrorCode
import com.jervisffb.net.messages.P2PClientState
import com.jervisffb.net.messages.P2PHostState
import com.jervisffb.net.messages.SpectatorState
import com.jervisffb.net.messages.TeamData
import com.jervisffb.ui.screen.p2p.host.TeamInfo
import com.jervisffb.utils.jervisLogger
import io.ktor.websocket.CloseReason
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


sealed interface ConnectionState
data class Disconnected(val reason: CloseReason) : ConnectionState
object Connecting : ConnectionState
object Connected : ConnectionState

/*
 * Game controller for a client joining a game as a Peer-to-Peer Client.
 *
 * This controller is responsible for the entire lifecycle of the game. Including
 * setting it up in the menu system.
 *
 * @see [com.jervisffb.engine.GameEngineController]
 * @see [com.jervisffb.ui.UiGameController]
 */
class P2PClientGameController(private val isHost: Boolean = false) {

    private val _clientState = MutableStateFlow(P2PClientState.START)
    val clientState: StateFlow<P2PClientState> = _clientState

    private val _hostState = MutableStateFlow(P2PHostState.START)
    val hostState: StateFlow<P2PHostState> = _hostState

    private val _connectionState = MutableStateFlow<ConnectionState>(Disconnected(CloseReason(CloseReason.Codes.NORMAL, "")))
    private val connectionState: StateFlow<ConnectionState> = _connectionState

    private val connection: ClintNetworkManager = ClintNetworkManager(GameStateMessageHandler())

//    private val mockServerJob: Job
    private var server: LightServer? = null

    // Track Coach/Team as they join
    val homeCoach: MutableStateFlow<Coach?> = MutableStateFlow(null)
    val awayCoach: MutableStateFlow<Coach?> = MutableStateFlow(null)
    val homeTeam: MutableStateFlow<Team?> = MutableStateFlow(null)
    val awayTeam: MutableStateFlow<Team?> = MutableStateFlow(null)
    val spectators = mutableListOf<Spectator>()

    var runner: GameRunner? = null // Should be != null when state == RUN_GAME

    init {
        if (isHost) {
            updateHostState(P2PHostState.START_SERVER)
        } else {
            updateClientState(P2PClientState.JOIN_SERVER)
        }
    }

    // Connection failed -> wrong url
    // Connection success -> joinGame ->
    suspend fun joinHost(
        gameUrl: String,
        coachName: String,
        gameId: GameId,
        teamIfHost: Team?,
        handler: ClientNetworkMessageHandler, ) {
//        if (state != ClientState.SELECT_HOST) error("Unexpected state: $state")
        connection.addMessageHandler(handler)
        connection.connectAndJoinGame(gameUrl, gameId, coachName, isHost = (teamIfHost != null), teamIfHost)
        // TODO How to update state when handler is coming from the outside?
    }

    suspend fun teamSelected(team: TeamInfo) {
        connection.sendTeamSelected(team)
    }

    fun cancelJoin() {
        connection.cancelJoin()
    }

    fun updateClientState(newState: P2PClientState) {
        _clientState.value = newState
    }

    fun updateHostState(newState: P2PHostState) {
        _hostState.value = newState
    }

    suspend fun close() {
        updateClientState(P2PClientState.JOIN_SERVER)
        server?.stop()
//        mockServerJob.cancel()
        connection.disconnect()
    }

    suspend fun disconnect(handler: AbstractClintNetworkMessageHandler) {
        updateClientState(P2PClientState.JOIN_SERVER)
        connection.addMessageHandler(handler)
        connection.disconnect()
    }

    suspend fun gameAccepted(accepted: Boolean) {
        connection.sendStartGame(accepted)
        if (!accepted) {
            connection.disconnect()
        }
    }

    suspend fun sendActionToServer(action: GameAction) {
        connection.sendClientAction(action)
    }

    /**
     * Class responsible for keeping the [clientState] variable up to date. This will be
     * called first, so all further handlers can assume that the "model" state is correct.
     *
     * Unexpected messages will be ignored, but logged as warning since the host should
     * be responsible for sending
     */
    inner class GameStateMessageHandler(): ClientNetworkMessageHandler {

        private val LOG = jervisLogger()

        // Network state
        override fun onConnected() { _connectionState.value = Connected }
        override fun onConnecting() { _connectionState.value = Connecting }
        override fun onDisconnected(reason: CloseReason) { _connectionState.value = Disconnected(reason) }

        // Game State
        override fun onTeamSelected(team: Team, isHomeTeam: Boolean) {
            if (isHomeTeam) {
                homeTeam.value = team
            } else {
                awayTeam.value = team
            }
        }

        override fun onCoachJoined(coach: Coach, isHomeCoach: Boolean) {
            if (homeCoach.value == null || awayCoach.value == null) {
                if (isHomeCoach) {
                    homeCoach.value = coach
                } else {
                    awayCoach.value = coach
                }
            } else {
                LOG.w { "Received onCoachJoined event, but two coaches already joined. Ignoring message" }
            }
        }

        override fun onCoachLeft(coach: Coach) {
            // Leaving after the game has started is not allowed unless the game
            // as been conceeded
//            when (state) {
//                ClientState.RUN_GAME -> {
//                    if (gameEngine?.state?.hasConceeded != null) {
//
//                    }
//                }
//                ClientState.CLOSE_GAME ->
//            }
//            if ()

        }

        override fun onSpectatorJoined(spectator: Spectator) {
//            TODO("Not yet implemented")
        }

        override fun onSpectatorLeft(spectator: Spectator) {
//            TODO("Not yet implemented")
        }

        override fun onClientStateChange(newState: P2PClientState) {
            _clientState.value = newState
        }

        override fun onHostStateChange(newState: P2PHostState) {
            _hostState.value = newState
        }

        override fun onSpectatorStateChange(newState: SpectatorState) {
            LOG.w { "Received onSpectatorStateChange event, but this is a Client" }
        }

        override fun onGameSync(message: GameStateSyncMessage) {
            // Should only be called right after a connection is established, so it should be safe
            // to just update all things directly.
            homeCoach.value = message.coaches.firstOrNull()
            awayCoach.value = message.coaches.getOrNull(1)
            homeTeam.value = message.homeTeam
            awayTeam.value = message.awayTeam
            _clientState.value = message.clientState
        }

        override fun updateClientState(state: P2PClientState) {
            _clientState.value = state
        }

        override fun onConfirmGameStart(message1: GameId, message: List<TeamData>) {
            // Wait for State change
        }

        override fun onGameReady(id: GameId) {
            // Wait for State change
        }

        override fun onServerError(errorCode: JervisErrorCode, message: String) {
            LOG.e { "Received onServerError event [$errorCode]: $message" }
        }

        override fun onGameAction(action: GameAction) {
            // TODO How to recover from errors here. It probably means the client and server
            //  got out of Sync. So one suggestion could be to show an error dialog saying\
            //  Inconsistent state and then a button that asks the server for its state and then
            //  reinitialize everything. For now we just log it
            try {
                runner?.handleAction(action)
            } catch (e: InvalidActionException) {
                LOG.e(e) { "Error handling game action: $action" }
            }
        }
    }
}
