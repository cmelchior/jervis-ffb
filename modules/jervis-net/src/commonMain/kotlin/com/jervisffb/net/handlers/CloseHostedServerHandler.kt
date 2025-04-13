package com.jervisffb.net.handlers

import com.jervisffb.net.GameSession
import com.jervisffb.net.JervisExitCode
import com.jervisffb.net.JervisNetworkWebSocketConnection
import com.jervisffb.net.JoinedP2PHost
import com.jervisffb.net.messages.CloseHostedServerMessage
import com.jervisffb.net.messages.GameState
import com.jervisffb.net.messages.ProtocolErrorServerError

/**
 * Handle closing the Hosted server gracefully while during the setup phase.
 * This is called when the Host regrets starting the server and it will abort the setup sequence and
 * disconnect all clients.
 */
class CloseHostedServerHandler(override val session: GameSession) : ClientMessageHandler<CloseHostedServerMessage>() {
    override suspend fun handleMessage(message: CloseHostedServerMessage, connection: JervisNetworkWebSocketConnection?) {
        if (connection == null) error("Missing connection for message: $message")
        val hostClient = session.getPlayerClient(connection)
        if (hostClient !is JoinedP2PHost) {
            session.out.sendError(connection, ProtocolErrorServerError("Only the host can shut down the server."))
            return
        }
        session.state = GameState.CLOSING
        session.shutdownGame(JervisExitCode.SERVER_CLOSING, "Host closed the server during setup.")
    }
}
