package com.jervisffb.net.handlers

import com.jervisffb.net.GameSession
import com.jervisffb.net.JervisExitCode
import com.jervisffb.net.JervisNetworkWebSocketConnection
import com.jervisffb.net.messages.GameState
import com.jervisffb.net.messages.LeaveGameMessage
import com.jervisffb.net.messages.ProtocolErrorServerError

class LeaveGameHandler(override val session: GameSession) : ClientMessageHandler<LeaveGameMessage>() {
    override suspend fun handleMessage(message: LeaveGameMessage, connection: JervisNetworkWebSocketConnection?) {
        when (session.state) {
            GameState.PLANNED,
            GameState.JOINING -> {
                session.out.sendError(
                    connection,
                    ProtocolErrorServerError("A game in ${session.state} is in the wrong state to leave: $message")
                )
            }
            GameState.STARTING -> {
                // If a player doesn't accept the game. Destroy the game session completely and disconnect all
                // players. (Should we allow the declining player to select another team instead?)
                if (connection == null) error("Missing connection for message: $message")
                val coachName = session.getPlayerClient(connection)?.team?.coach ?: "<unknown>"
                session.shutdownGame(
                    JervisExitCode.GAME_NOT_ACCEPTED,
                    "$coachName did not accept the game. It will be closed.")

            }
            GameState.ACTIVE -> {
                TODO("Unclear how to handle this for now")
            }
            GameState.FINISHED -> {
                session.out.sendError(
                    connection,
                    ProtocolErrorServerError("Game '${session.gameId}' already finished: $message")
                )
            }
        }
    }
}
