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
import com.jervisffb.engine.utils.safeTryEmit
import com.jervisffb.net.GameId
import com.jervisffb.net.LightServer
import com.jervisffb.net.messages.GameStateSyncMessage
import com.jervisffb.net.messages.InvalidGameActionOwnerServerError
import com.jervisffb.net.messages.InvalidGameActionTypeServerError
import com.jervisffb.net.messages.InvalidTeamServerError
import com.jervisffb.net.messages.OutOfOrderGameActionServerError
import com.jervisffb.net.messages.P2PClientState
import com.jervisffb.net.messages.P2PHostState
import com.jervisffb.net.messages.ProtocolErrorServerError
import com.jervisffb.net.messages.ReadMessageServerError
import com.jervisffb.net.messages.ServerError
import com.jervisffb.net.messages.SpectatorState
import com.jervisffb.net.messages.TeamData
import com.jervisffb.net.messages.UnknownServerError
import com.jervisffb.ui.game.model.ModelRef
import com.jervisffb.ui.menu.components.TeamInfo
import com.jervisffb.utils.jervisLogger
import io.ktor.websocket.CloseReason
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow


sealed interface ConnectionState
data class Disconnected(val reason: CloseReason) : ConnectionState
object Connecting : ConnectionState
object Connected : ConnectionState

/**
 * Game controller for a client joining a game as a Peer-to-Peer Client.
 *
 * This controller is responsible for the entire lifecycle of the game. Including
 * setting it up in the menu system.
 *
 * @see [com.jervisffb.engine.GameEngineController]
 * @see [com.jervisffb.ui.game.UiGameController]
 */
class P2PClientNetworkAdapter(
    private val isHost: Boolean = false
) {
    val clientState: StateFlow<P2PClientState>
        field = MutableStateFlow(P2PClientState.JOIN_SERVER)

    val hostState: StateFlow<P2PHostState>
        field= MutableStateFlow(P2PHostState.SETUP_GAME)

    val connectionState: StateFlow<ConnectionState>
        field = MutableStateFlow<ConnectionState>(Disconnected(CloseReason(CloseReason.Codes.NORMAL, "")))

    /**
     * Errors the server rejected a request with, that the coach needs to be told about. These are
     * one-off events rather than state, so they are only delivered to whoever is collecting when
     * they happen. Errors tied to game actions are not reported here, they are handled where the
     * action was made.
     */
    val serverErrors: SharedFlow<ServerError>
        field = MutableSharedFlow<ServerError>(extraBufferCapacity = 8)

    val networkManager: ClientNetworkManager = ClientNetworkManager(GameStateMessageHandler())

    private var server: LightServer? = null
    private var gameId: GameId? = null

    // Track Coach/Team as they join
    var rules: Rules? = null
    var initialActions: List<GameAction> = emptyList()
    val homeCoach: MutableStateFlow<Coach?> = MutableStateFlow(null)
    val awayCoach: MutableStateFlow<Coach?> = MutableStateFlow(null)
    val homeTeam: MutableStateFlow<ModelRef<Team>?> = MutableStateFlow(null)
    val awayTeam: MutableStateFlow<ModelRef<Team>?> = MutableStateFlow(null)
    val spectators = mutableListOf<Spectator>()

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
        coachType: CoachType,
        gameId: GameId,
        teamIfHost: Team?,
        handler: ClientNetworkMessageHandler,) {
        this.gameId = gameId
//        if (state != ClientState.SELECT_HOST) error("Unexpected state: $state")
        networkManager.setConnectionHandler(handler)
        networkManager.connectAndJoinGame(gameUrl, gameId, coachName, coachType, isHost = (teamIfHost != null), teamIfHost)
        // TODO How to update state when handler is coming from the outside?
    }

    suspend fun teamSelected(team: TeamInfo) {
        networkManager.sendTeamSelected(team)
    }

    fun cancelJoin() {
        networkManager.cancelJoin()
    }

    fun updateClientState(newState: P2PClientState) {
        clientState.value = newState
    }

    fun updateHostState(newState: P2PHostState) {
        hostState.value = newState
    }

    suspend fun close() {
        updateClientState(P2PClientState.JOIN_SERVER)
        server?.stop()
//        mockServerJob.cancel()
        networkManager.disconnect()
    }

    suspend fun disconnect(handler: AbstractClintNetworkMessageHandler) {
        updateClientState(P2PClientState.JOIN_SERVER)
        networkManager.setConnectionHandler(handler)
        networkManager.disconnect()
    }

    suspend fun gameAccepted(accepted: Boolean) {
        networkManager.sendStartGame(accepted)
        // TODO The server will disconnect us, and doing this here
        //  results in a race condition where the server never receives
        //  the accepted result. This only happens if we reject twice.
        //  Smells like a bug somewhere. Probably a bug with ClientNetworkManager
        //  not being reset correctly.
        // if (!accepted) {
        //     connection.disconnect()
        // }
    }

    suspend fun sendActionToServer(index: GameActionId, action: GameAction) {
        networkManager.sendClientAction(index, action)
    }

    suspend fun sendGameStarted() {
        networkManager.sendGameStarted(this.gameId!!)
    }

    fun addMessageHandler(handler: ClientNetworkMessageHandler) {
        networkManager.addMessageHandler(handler)
    }

    suspend fun sendServerClosed() {
        networkManager.sendCloseHostedServer()
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
        override fun onConnected() {
            LOG.d { "onConnected" }
            // A new connection means a new game session, possibly against a server that was
            // restarted while we were away. Everything below is re-sent by the game sync, and
            // keeping the old values around means a rejoin looks identical to the previous
            // session: `MutableStateFlow` conflates equal values, so collectors would never see
            // the state arrive again, and `onCoachJoined` would drop the coaches as duplicates.
            homeCoach.value = null
            awayCoach.value = null
            homeTeam.value = null
            awayTeam.value = null
            spectators.clear()
            connectionState.value = Connected
        }
        override fun onConnecting() {
            LOG.d { "onConnecting" }
            connectionState.value = Connecting
        }
        override fun onDisconnected(reason: CloseReason) {
            LOG.d { "onDisconnected: $reason" }
            connectionState.value = Disconnected(reason)
        }

        // Game State
        override fun onTeamSelected(team: Team, homeTeam: Boolean) {
            if (homeTeam) {
                team.coach = homeCoach.value!!
                this@P2PClientNetworkAdapter.homeTeam.value = ModelRef(team.id, team)
            } else {
                team.coach = awayCoach.value!!
                awayTeam.value = ModelRef(team.id, team)
            }
        }

        override fun onCoachJoined(coach: Coach, isHomeCoach: Boolean) {
            // Fill the slot the server told us about. The previous version only accepted a coach
            // while either slot was still empty, which silently dropped the message whenever both
            // were already filled, e.g. by a stale session.
            val slot = if (isHomeCoach) homeCoach else awayCoach
            val current = slot.value
            if (current != null && current.id != coach.id) {
                LOG.w { "Replacing already joined coach '${current.name}' with '${coach.name}'" }
            }
            slot.value = coach
        }

        override fun onCoachLeft(coach: Coach) {
            // TODO Leaving after the game has started is not allowed unless the game
            //  as been conceeded
            when (coach.id) {
                awayCoach.value?.id -> awayCoach.value = null
                homeCoach.value?.id -> homeCoach.value = null
            }
        }

        override fun onSpectatorJoined(spectator: Spectator) {
//            TODO("Not yet implemented")
        }

        override fun onSpectatorLeft(spectator: Spectator) {
//            TODO("Not yet implemented")
        }

        override fun onClientStateChange(newState: P2PClientState) {
            clientState.value = newState
        }

        override fun onHostStateChange(newState: P2PHostState) {
            hostState.value = newState
        }

        override fun onSpectatorStateChange(newState: SpectatorState) {
            LOG.w { "Received onSpectatorStateChange event, but this is a Client" }
        }

        override fun onGameSync(message: GameStateSyncMessage) {
            // Should only be called right after a connection is established, so it should be safe
            // to just update all things directly.
            rules = message.rules
            homeCoach.value = message.coaches.firstOrNull()
            awayCoach.value = message.coaches.getOrNull(1)
            homeTeam.value = message.homeTeam?.let {
                val team = SerializedTeam.deserialize(message.rules, it, homeCoach.value!!)
                ModelRef(team.id, team)
            }
            awayTeam.value = message.awayTeam?.let {
                val team = SerializedTeam.deserialize(message.rules, it, awayCoach.value!!)
                ModelRef(team.id, team)
            }
            clientState.value = message.clientState
        }

        override fun updateClientState(state: P2PClientState) {
            clientState.value = state
        }

        override fun onConfirmGameStart(id: GameId, rules: Rules, initialActions: List<GameAction>, teams: List<TeamData>) {
            // Wait for State change
            this@P2PClientNetworkAdapter.rules = rules
            this@P2PClientNetworkAdapter.initialActions = initialActions
        }

        override fun onGameReady(id: GameId) {
            // Wait for State change
        }

        override fun onServerError(error: ServerError) {
            when (error) {
                is InvalidTeamServerError,
                is ProtocolErrorServerError,
                is ReadMessageServerError,
                is UnknownServerError -> {
                    LOG.e { "Received onServerError event [${error.errorCode}]: ${error.message}" }
                    // The server refused something we asked for. Nothing further happens on the
                    // connection, so unless this is surfaced the UI just looks stuck.
                    serverErrors.safeTryEmit(error)
                }
                is OutOfOrderGameActionServerError,
                is InvalidGameActionOwnerServerError,
                is InvalidGameActionTypeServerError -> {
                    // Consider removing these logs since these should always be handled by the UI layer.
                    LOG.d { "Received onServerError event [${error.errorCode}]: ${error.message}." }
                }
            }
        }

        override fun onGameAction(producer: CoachId, serverIndex: GameActionId, action: GameAction) {
            // Do nothing here. This is handled in P2PActionActionProvider
        }
    }
}
