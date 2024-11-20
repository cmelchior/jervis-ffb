package com.jervisffb.net.handlers

import com.jervisffb.net.GameSession
import com.jervisffb.net.messages.InternalJoinMessage
import io.ktor.websocket.WebSocketSession

class InternalJoinHandler(
    override val session: GameSession
) : ClientMessageHandler<InternalJoinMessage>() {
    override suspend fun handleMessage(message: InternalJoinMessage, connection: WebSocketSession) {
        message.action()
    }
}

