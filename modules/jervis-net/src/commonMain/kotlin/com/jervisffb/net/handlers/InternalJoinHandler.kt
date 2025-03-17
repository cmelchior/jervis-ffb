package com.jervisffb.net.handlers

import com.jervisffb.net.GameSession
import com.jervisffb.net.JervisNetworkWebSocketConnection
import com.jervisffb.net.messages.InternalJoinMessage
import com.jervisffb.net.messages.UnknownServerError

class InternalJoinHandler(
    override val session: GameSession
) : ClientMessageHandler<InternalJoinMessage>() {
    override suspend fun handleMessage(message: InternalJoinMessage, connection: JervisNetworkWebSocketConnection?) {
        try {
            message.action()
        } catch (e: Exception) {
            session.out.sendError(connection,  UnknownServerError(e.stackTraceToString()))
        }
    }
}

