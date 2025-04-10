package com.jervisffb.net

import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionId
import com.jervisffb.engine.model.Coach
import com.jervisffb.engine.model.CoachId
import com.jervisffb.engine.model.Spectator
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.serialize.SerializedTeam
import com.jervisffb.net.messages.CoachJoinedMessage
import com.jervisffb.net.messages.CoachLeftMessage
import com.jervisffb.net.messages.ConfirmGameStartMessage
import com.jervisffb.net.messages.GameReadyMessage
import com.jervisffb.net.messages.GameStateSyncMessage
import com.jervisffb.net.messages.P2PClientState
import com.jervisffb.net.messages.P2PHostState
import com.jervisffb.net.messages.ServerError
import com.jervisffb.net.messages.ServerMessage
import com.jervisffb.net.messages.SpectatorJoinedMessage
import com.jervisffb.net.messages.SpectatorLeftMessage
import com.jervisffb.net.messages.SyncGameActionMessage
import com.jervisffb.net.messages.TeamData
import com.jervisffb.net.messages.TeamJoinedMessage
import com.jervisffb.net.messages.UpdateClientStateMessage
import com.jervisffb.net.messages.UpdateHostStateMessage
import com.jervisffb.net.serialize.jervisNetworkSerializer
import com.jervisffb.utils.jervisLogger
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import kotlinx.coroutines.CoroutineScope

/**
 * Class wrapping the responsibility of sending messages from a server game
 * session to all connected clients.
 */
class ServerCommunication(
    private val session: GameSession,
    private val scope: CoroutineScope,
    // If false, sending messages is done in order based on when a client connected. Recommended for testing
    // If true, sending messages to connected clients is done in parallel.
    private val parallelizeSend: Boolean = true
) {

    companion object {
        val LOG = jervisLogger()
    }

    suspend fun sendCoachJoined(coach: Coach, isHomeCoach: Boolean) {
        val msg = CoachJoinedMessage(coach, isHomeCoach = isHomeCoach)
        sendAllConnections(msg)
    }

    suspend fun sendCoachLeft(coach: Coach) {
        val msg = CoachLeftMessage(coach)
        sendAllConnections(msg)
    }

    suspend fun sendTeamJoined(isHomeTeam: Boolean, team: Team) {
        val msg = TeamJoinedMessage(isHomeTeam, SerializedTeam.serialize(team), team.coach)
        sendAllConnections(msg)
    }

    suspend fun sendTeamJoined(isHomeTeam: Boolean, team: SerializedTeam, coach: Coach) {
        val msg = TeamJoinedMessage(isHomeTeam, team, coach)
        sendAllConnections(msg)
    }

    suspend fun sendSpectatorJoined(spectator: Spectator) {
        val msg = SpectatorJoinedMessage(spectator)
        sendAllConnections(msg)
    }

    suspend fun sendSpectatorLeft(spectator: Spectator) {
        val msg = SpectatorLeftMessage(spectator)
        sendAllConnections(msg)
    }

    suspend fun sendGameStateSync(client: JoinedClient, session: GameSession) {
        val msg = GameStateSyncMessage(
            session.gameSettings.gameRules,
            session.coaches.map { it.coach },
            session.spectators.map { it.spectator },
            session.hostState,
            session.clientState,
            session.spectatorState,
            session.homeTeam?.let { SerializedTeam.serialize(it) },
            session.awayTeam?.let { SerializedTeam.serialize(it) },
        )
        sendToConnection(client.connection, msg)
    }

    suspend fun sendStartingGameRequest(id: GameId, rules: Rules, initialActions: List<GameAction>, teams: List<Team>) {
        val msg = ConfirmGameStartMessage(
            id,
            rules,
            initialActions,
            teams.map {
                TeamData(
                    coach = it.coach.name,
                    teamName =  it.name,
                    teamRoster = it.roster.name,
                    teamValue = it.teamValue
                )
            })
        sendAllCoaches(msg)
    }

    suspend fun sendGameReady(id: GameId) {
        val msg = GameReadyMessage(id)
        sendAllConnections(msg)
    }


    suspend fun sendHostStateUpdate(newState: P2PHostState) {
        val msg = UpdateHostStateMessage(newState)
        sendToConnection(session.host!!.connection, msg)
    }

    suspend fun sendClientStateUpdate(newState: P2PClientState) {
        val msg = UpdateClientStateMessage(newState)
        sendToConnection(session.client!!.connection, msg)
    }

    suspend fun sendError(connection: JervisNetworkWebSocketConnection?, errorMessage: ServerError) {
        LOG.w { "[Server] [${connection?.username}] Sending error (${errorMessage.errorCode}): ${errorMessage.message}" }
        if (connection != null) {
            sendToConnection(connection, errorMessage)
        } else {
            sendAllConnections(errorMessage)
        }
    }

    private suspend fun sendToConnection(connection: DefaultWebSocketSession, message: ServerMessage) {
        LOG.i { "[Server] Sending to connection: $message" }
        val jsonMessage = jervisNetworkSerializer.encodeToString(message)
        try {
            connection.send(Frame.Text(jsonMessage))
        } catch (ex: Throwable) {
            LOG.i { "[Server] Error sending message to connection: $ex" }
        }
    }

    private suspend fun sendAllConnections(message: ServerMessage) {
        LOG.i { "[Server] Sending to all connections: $message" }
        val jsonMessage = jervisNetworkSerializer.encodeToString(message)
        sendToConnections(session.coaches + session.spectators, jsonMessage)
    }

    private suspend fun sendToAllOtherConnections(sender: JoinedClient?, message: ServerMessage) {
        LOG.i { "[Server] Sending to all other connections from ${sender?.connection?.username}: $message" }
        val jsonMessage = jervisNetworkSerializer.encodeToString(message)
        val otherClients = if (sender != null) {
            session.coaches.filter { it.connection != sender.connection } + session.spectators.filter { it.connection != sender.connection }
        } else {
            session.coaches + session.spectators
        }
        sendToConnections(otherClients, jsonMessage)
    }
    private suspend fun sendAllCoaches(message: ServerMessage) {
        LOG.i { "[Server] Sending to all players: $message" }
        val jsonMessage = jervisNetworkSerializer.encodeToString(message)
        // Snapshot list, to prevent concurrent modifications
        sendToConnections(session.coaches.toList(), jsonMessage)
    }

    private suspend fun sendToConnections(connections: List<JoinedClient>, jsonMessage: String) {
        // Figure out how we can parallelize this, while also being able to send messages in parallel
        // 1. A scope with a single thread pr. connection?
        // 2. Somehow make it is possible group multiple messages?
        // 3. Some sort of outgoing queue?
        if (parallelizeSend) {
            connections.forEach {
//                scope.launch {
                try {
                    it.connection.send(Frame.Text(jsonMessage))
                } catch (ex: Throwable) {
                    LOG.i { "[Server] Error sending message to connection: $ex" }
                }

//                }
            }
        } else {
//            scope.launch {
            connections.forEach {
                try {
                    it.connection.send(Frame.Text(jsonMessage))
                } catch (ex: Throwable) {
                    LOG.i { "[Server] Error sending message to connection: $ex" }
                }

            }
//            }
        }
    }

    // A Game action was sent to the server and processed successfully, it should now be sent to all other connected
    // clients so they can update their local game model.
    suspend fun sendGameActionSync(sender: JoinedP2PCoach?, producer: CoachId, index: GameActionId, action: GameAction) {
        val message = SyncGameActionMessage(producer, index, action)
        sendToAllOtherConnections(sender, message)
    }

}
