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
import io.ktor.websocket.DefaultWebSocketSession
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds

actual fun startEmbeddedServer(
    server: LightServer,
    newConnectionHandler: suspend (DefaultWebSocketSession, GameId) -> Unit,
): Any {
    val LOG = jervisLogger()
    val platformServer = embeddedServer(Netty,8080) {
        install(WebSockets)
        {
            pingPeriod = 15.seconds
            timeout = 15.seconds
            maxFrameSize = Long.MAX_VALUE
            masking = false
        }
        routing {
            webSocket("/joinGame") {
                val id = call.request.queryParameters["id"] ?: throw IllegalArgumentException("Missing gameId parameter")
                val gameId = GameId(id)
                try {
                    newConnectionHandler(this, gameId)
                } catch (ex: Exception) {
                    // All known error cases should be handled inside newConnectionHandler,
                    // so if we get here, something has gone horribly wrong. Just close
                    // the connection with as much info as we have.
                    LOG.e { ex.stackTraceToString() }
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
        // TODO Unclear why the server always run the full shutdown grace + timeout. For now set it low to avoid having tests running forever.
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

