package com.jervisffb.net.handlers

import com.jervisffb.engine.utils.containsActionWithRandomBehavior
import com.jervisffb.engine.utils.createRandomAction
import com.jervisffb.net.GameSession
import com.jervisffb.net.JervisNetworkWebSocketConnection
import com.jervisffb.net.messages.GameStartedMessage
import com.jervisffb.net.messages.JervisErrorCode

class GameStartedHandler(override val session: GameSession) : ClientMessageHandler<GameStartedMessage>() {
    override suspend fun handleMessage(message: GameStartedMessage, connection: JervisNetworkWebSocketConnection) {
        // If all players have accepted the game, it will start "for real", sending a notification
        // to all connected clients so they can initiate their respective Game Engines.
        session.getPlayerClient(connection)?.let {
            if (it.hasStartedGame) {
                session.out.sendError(connection, message, JervisErrorCode.PROTOCOL_ERROR, "Player has already started the game.")
                return@let
            }
            it.hasStartedGame = true
            if (session.coaches.all { it.hasStartedGame}) {
                val game = session.game!!

                var availableActions = game.getAvailableActions()
                while (availableActions.containsActionWithRandomBehavior()) {
                    val action = createRandomAction(game.state, availableActions)
                    game.handleAction(action)
                    // If no producer, we just set it to the Home Team
                    val producer = session.coaches.firstOrNull { it.coach == availableActions.team?.coach } ?: session.coaches.first()
                    session.out.sendGameActionSync(sender = null, producer.coach.id,session.game?.history?.last()?.id!!, action = action)
                    availableActions = game.getAvailableActions()
                }
            }
        } ?: session.out.sendError(connection, message,JervisErrorCode.PROTOCOL_ERROR, "Spectator clients cannot start games: $message")
    }
}
