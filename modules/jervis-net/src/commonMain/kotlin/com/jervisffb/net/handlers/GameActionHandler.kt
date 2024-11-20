package com.jervisffb.net.handlers

import com.jervisffb.net.GameSession
import com.jervisffb.net.messages.GameActionMessage
import io.ktor.websocket.WebSocketSession

class GameActionHandler(override val session: GameSession) : ClientMessageHandler<GameActionMessage>() {
    override suspend fun handleMessage(message: GameActionMessage, connection: WebSocketSession) {
        // Find relevant games cache and send action there.

        // If not, report an error
    }
}
