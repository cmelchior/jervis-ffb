package com.jervisffb.net.handlers

import com.jervisffb.net.ClientState
import com.jervisffb.net.GameSession
import com.jervisffb.net.messages.JervisErrorCode
import com.jervisffb.net.messages.StartGameMessage
import io.ktor.websocket.WebSocketSession

class StartGameHandler(override val session: GameSession) : ClientMessageHandler<StartGameMessage>() {
    override suspend fun handleMessage(message: StartGameMessage, connection: WebSocketSession) {
        // If all players have accepted the game, it will start "for real", sending a notification
        // to all connected clients so they can initiate their respective Game Engines.
        session.getPlayerClient(connection)?.let {
            it.state = ClientState.READY
            if (session.isReadyToStart()) {
                session.startGame()
                session.out.sendGamReady(session.gameId)
            }
        } ?: session.out.sendError(connection, JervisErrorCode.PROTOCOL_ERROR, "Spectator clients cannot start games: $message")
    }
}
