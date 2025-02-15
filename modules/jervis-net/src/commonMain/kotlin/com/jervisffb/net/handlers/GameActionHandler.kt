package com.jervisffb.net.handlers

import com.jervisffb.net.GameSession
import com.jervisffb.net.JervisNetworkWebSocketConnection
import com.jervisffb.net.messages.GameActionMessage

class GameActionHandler(override val session: GameSession) : ClientMessageHandler<GameActionMessage>() {
    override suspend fun handleMessage(message: GameActionMessage, connection: JervisNetworkWebSocketConnection) {
        // Find relevant games cache and send action there.

        // If not, report an error
    }
}
