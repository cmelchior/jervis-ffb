package com.jervisffb.net.handlers

import com.jervisffb.net.GameSession
import com.jervisffb.net.JervisNetworkWebSocketConnection
import com.jervisffb.net.messages.GameState
import com.jervisffb.net.messages.P2PClientState
import com.jervisffb.net.messages.P2PHostState
import com.jervisffb.net.messages.ProtocolErrorServerError
import com.jervisffb.net.messages.StartGameMessage

class StartGameHandler(override val session: GameSession) : ClientMessageHandler<StartGameMessage>() {
    override suspend fun handleMessage(message: StartGameMessage, connection: JervisNetworkWebSocketConnection?) {
        // If all players have accepted the game, it will start "for real", sending a notification
        // to all connected clients so they can initiate their respective Game Engines.
        if (connection == null) error("Missing connection for message: $message")
        session.getPlayerClient(connection)?.let {
            if (it.hasAcceptedGame) {
                session.out.sendError(
                    connection,
                    ProtocolErrorServerError("Player has already accepted the game."),
                )
                return@let
            }
            if (session.state != GameState.STARTING) {
                session.out.sendError(
                    connection,
                    ProtocolErrorServerError("Game are in a state that doesn't allow starting: ${session.state}."),
                )
                return@let
            }
            it.hasAcceptedGame = true
            if (session.isReadyToStart()) {
                session.startGame()
                session.out.sendGamReady(session.gameId)
                session.hostState = P2PHostState.RUN_GAME
                session.clientState = P2PClientState.RUN_GAME
                session.out.sendHostStateUpdate(session.hostState)
                session.out.sendClientStateUpdate(session.clientState)
            }
        } ?: session.out.sendError(
            connection,
            ProtocolErrorServerError("Spectator clients cannot start games: $message"),
        )
    }
}
