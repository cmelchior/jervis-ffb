package com.jervisffb.net.handlers

import com.jervisffb.net.GameSession
import com.jervisffb.net.JervisNetworkWebSocketConnection
import com.jervisffb.net.messages.GameStartedMessage
import com.jervisffb.net.messages.ProtocolErrorServerError

class GameStartedHandler(override val session: GameSession) : ClientMessageHandler<GameStartedMessage>() {
    override suspend fun handleMessage(message: GameStartedMessage, connection: JervisNetworkWebSocketConnection?) {
        // If all players have accepted the game, it will start "for real", sending a notification
        // to all connected clients so they can initiate their respective Game Engines.
        if (connection == null) error("Missing connection for message: $message")
        session.getPlayerClient(connection)?.let {
            if (it.hasStartedGame) {
                session.out.sendError(connection, ProtocolErrorServerError("Player has already started the game."))
                return@let
            }
            it.hasStartedGame = true
            if (session.coaches.all { it.hasStartedGame}) {
                val game = session.game!!
                rollForwardToUserAction(session, game, connection)
            }
        } ?: session.out.sendError(connection, ProtocolErrorServerError("Spectator clients cannot start games: $message"))
    }
}
