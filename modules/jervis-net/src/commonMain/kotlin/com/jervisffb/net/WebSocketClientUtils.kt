package com.jervisffb.net

import io.ktor.websocket.DefaultWebSocketSession

expect fun startEmbeddedServer(
    server: LightServer,
    newConnectionHandler: suspend (DefaultWebSocketSession, GameId) -> Unit,
): Any

// Stop the embedded server. Hide the type, because WASM doesn't support Ktor Engines
expect fun stopEmbeddedServer(server: Any)
