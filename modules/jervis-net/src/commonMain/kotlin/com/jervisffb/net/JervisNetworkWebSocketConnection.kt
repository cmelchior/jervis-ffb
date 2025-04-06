package com.jervisffb.net

import io.ktor.websocket.DefaultWebSocketSession

interface JervisWebSocketConnection: DefaultWebSocketSession {
    val username: String
    suspend fun send(message: String)
    suspend fun receive(): String?

}

/**
 * Wrapper for a websocket connection between a Jervis client and server.
 * It mostly exists so we can track the username owning the connection.
 */
open class JervisNetworkWebSocketConnection(val username: String, connection: DefaultWebSocketSession)
: DefaultWebSocketSession by connection
