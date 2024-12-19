package com.jervisffb.net

import com.jervisffb.utils.jervisLogger
import io.ktor.server.application.install
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.WebSocketSession
import java.time.Duration
import java.util.concurrent.TimeUnit

actual fun startEmbeddedServer(
    server: LightServer,
    newConnectionHandler: suspend (WebSocketSession) -> Unit,
): Any {
    val platformServer = embeddedServer(Netty,8080) {
        install(WebSockets)
        {
            pingPeriod = Duration.ofSeconds(15)
            timeout = Duration.ofSeconds(15)
            maxFrameSize = Long.MAX_VALUE
            masking = false
        }
        routing {
            webSocket("/game") {
                try {
                    newConnectionHandler(this)
                } catch (ex: Exception) {
                    // All known error cases should be handled inside newConnectionHandler,
                    // so if we get here, something has gone horribly wrong. Just close
                    // the connection with as much info as we have.
                    this.close(JervisExitCode.UNEXPECTED_ERROR, ex.stackTraceToString())
                }
            }
        }
    }
    platformServer.start(wait = false)
    jervisLogger().i { "Embedded server started" }
    return platformServer
}

actual fun stopEmbeddedServer(server: Any) {
    if (server is EmbeddedServer<*, *>) {
            server.stop(
                shutdownGracePeriod = 500,
                shutdownTimeout = 500,
                timeUnit = TimeUnit.MILLISECONDS,
            )
        jervisLogger().i { "Embedded server stopped" }
    } else {
        throw IllegalArgumentException("Invalid server type: $server")
    }
}

