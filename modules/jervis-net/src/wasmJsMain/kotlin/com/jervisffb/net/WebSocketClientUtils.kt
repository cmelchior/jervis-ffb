package com.jervisffb.net

import io.ktor.websocket.DefaultWebSocketSession

actual fun startEmbeddedServer(
    server: LightServer,
    newConnectionHandler: suspend (DefaultWebSocketSession, GameId) -> Unit,
): Any {
    TODO()
}

actual fun stopEmbeddedServer(server: Any, immediately: Boolean) {
    // Stop server
}
