package com.jervisffb.net.handlers

import com.jervisffb.net.GameSession
import com.jervisffb.net.messages.ClientMessage
import io.ktor.websocket.WebSocketSession

abstract class ClientMessageHandler<T: ClientMessage> {
    protected abstract val session: GameSession
    abstract suspend fun handleMessage(message: T, connection: WebSocketSession)
}
