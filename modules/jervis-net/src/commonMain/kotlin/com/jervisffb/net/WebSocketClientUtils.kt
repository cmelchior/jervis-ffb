package com.jervisffb.net

import io.ktor.websocket.DefaultWebSocketSession

expect fun startEmbeddedServer(
    server: LightServer,
    newConnectionHandler: suspend (DefaultWebSocketSession, GameId) -> Unit,
): Any

// Stop the embedded server. Hide the type, because WASM doesn't support Ktor Engines
// If `immediately` is true, we will attempt to stop the server as quickly as possible
// without waiting for connections to terminate nicely.
expect fun stopEmbeddedServer(server: Any, immediately: Boolean = false)
