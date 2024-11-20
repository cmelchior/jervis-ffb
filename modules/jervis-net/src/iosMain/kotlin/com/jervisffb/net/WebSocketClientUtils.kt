package com.jervisffb.net

import io.ktor.websocket.WebSocketSession

actual fun startEmbeddedServer(
    server: LightServer,
    newConnectionHandler: suspend (WebSocketSession) -> Unit,
): Any {
    TODO()
}

actual fun stopEmbeddedServer(server: Any) {
    // Stop server
}
