package com.jervisffb.net

import com.jervisffb.engine.model.Team
import com.jervisffb.net.messages.ConfirmGameStartMessage
import com.jervisffb.net.messages.GameReadyMessage
import com.jervisffb.net.messages.JervisErrorCode
import com.jervisffb.net.messages.PlayerJoinedMessage
import com.jervisffb.net.messages.ServerError
import com.jervisffb.net.messages.ServerMessage
import com.jervisffb.net.messages.TeamData
import com.jervisffb.net.serialize.jervisNetworkSerializer
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import kotlinx.serialization.encodeToString

/**
 * Class wrapping the responsibility of sending messages from a game session to all connected clients
 */
class ServerCommunication(
    private val session: GameSession,
    // If false, sending messages is done in order based on when a client connected. Recommended for testing
    // If true, sending messages to connected clients is done in parallel.
    private val parallelizeSend: Boolean = true
) {
    suspend fun sendPlayerJoined(username: String) {
        val msg = PlayerJoinedMessage(username)
        sendAllConnections(msg)
    }

    fun sendSpectatorJoined(username: String) {
        TODO("Not yet implemented")
    }

    suspend fun sendStartingGameRequest(id: GameId, teams: List<Team>) {
        val msg = ConfirmGameStartMessage(id, teams.map {
            TeamData(
                coach = it.coach.name,
                teamName =  it.name,
                teamRoster = it.roster.name,
                teamValue = it.teamValue
            )
        })
        sendAllPlayers(msg)
    }

    suspend fun sendGamReady(id: GameId) {
        val msg = GameReadyMessage(id)
        sendAllConnections(msg)
    }

    suspend fun sendError(connection: WebSocketSession, errorCode: JervisErrorCode, message: String) {
        val msg = ServerError(errorCode, message)
        sendToConnection(connection, msg)
    }

    private suspend fun sendAllConnections(message: ServerMessage) {
        val jsonMessage = jervisNetworkSerializer.encodeToString(message)
        // TODO Send the messages in parallel, not sequentially
        //  Check for parallelizeSend
        session.players.forEach {
            println("Sending: $message to ${it.connection}")
            it.connection.send(Frame.Text(jsonMessage))
            println("Sent: $message to ${it.connection}")
        }
        session.spectators.forEach {
            it.connection.send(Frame.Text(jsonMessage))
        }
    }

    private suspend fun sendAllPlayers(message: ServerMessage) {
        val jsonMessage = jervisNetworkSerializer.encodeToString(message)
        // TODO Send the messages in parallel, not sequentially
        //  Check for parallelizeSend
        session.players.forEach {
            it.connection.send(Frame.Text(jsonMessage))
        }
    }

    private suspend fun sendToConnection(connection: WebSocketSession, message: ServerMessage) {
        val jsonMessage = jervisNetworkSerializer.encodeToString(message)
        connection.send(Frame.Text(jsonMessage))
    }
}
