package com.jervisffb.net.test

import com.jervisffb.net.JervisClientWebSocketConnection
import com.jervisffb.net.LightServer
import kotlinx.coroutines.CancellationException
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.fetchAndIncrement

// Ports handed out to test servers. Tests must not share a hardcoded port. They would fight both
// with each other and with anything else on the machine, including a Jervis client, which hosts
// its own games on 8080 by default.
@OptIn(ExperimentalAtomicApi::class)
private val nextServerPort = AtomicInt(19_080)

// Enough attempts to get past a handful of ports that happen to be taken, without hanging the
// build if the whole range is somehow unusable.
private const val MAX_PORT_ATTEMPTS = 20

/**
 * Start a [LightServer] on a port nothing else is using, and return the started server. Read the
 * port it ended up on from [LightServer.port].
 *
 * [createServer] is called with a candidate port and must return a server configured to use it. If
 * that port turns out to be taken after all, the server is discarded and the next one is tried.
 */
@OptIn(ExperimentalAtomicApi::class)
suspend fun startServerOnFreePort(createServer: (port: Int) -> LightServer): LightServer {
    var lastError: Throwable? = null
    repeat(MAX_PORT_ATTEMPTS) {
        val server = createServer(nextServerPort.fetchAndIncrement())
        try {
            server.start()
            return server
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: Exception) {
            // Something else grabbed the port. Throw this server away and try the next candidate.
            lastError = ex
            server.stop(immediately = true)
        }
    }
    throw IllegalStateException("Could not find a free port after $MAX_PORT_ATTEMPTS attempts", lastError)
}

suspend inline fun <reified T> checkServerMessage(connection: JervisClientWebSocketConnection, assertFunc: (T) -> Unit) {
    val serverMessage = connection.receiveOrNull()
    if (serverMessage !is T) {
        throw AssertionError("Expected ${T::class.simpleName}, got $serverMessage. Close reason: ${connection.getCloseReason()}")
    }
    assertFunc(serverMessage)
}

suspend inline fun <reified T> consumeServerMessage(connection: JervisClientWebSocketConnection) {
    val serverMessage = connection.receiveOrNull()
    if (serverMessage !is T) throw AssertionError("Expected ${T::class.simpleName}, got $serverMessage. Close reason: ${connection.getCloseReason()}")
}
