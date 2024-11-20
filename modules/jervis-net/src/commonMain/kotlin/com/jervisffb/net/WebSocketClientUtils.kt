package com.jervisffb.net

import io.ktor.websocket.WebSocketSession

expect fun startEmbeddedServer(
    server: LightServer,
    newConnectionHandler: suspend (WebSocketSession) -> Unit,
): Any

// Stop the embedded server. Hide the type, because WASM doesn't support Ktor Engines
expect fun stopEmbeddedServer(server: Any)
