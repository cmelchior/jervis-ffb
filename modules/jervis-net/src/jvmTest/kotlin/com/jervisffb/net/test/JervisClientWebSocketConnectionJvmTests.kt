package com.jervisffb.net.test

import com.jervisffb.net.GameId
import com.jervisffb.net.JervisClientWebSocketConnection
import com.jervisffb.net.JervisExitCode
import com.jervisffb.utils.runBlocking
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketTimeoutException
import java.security.MessageDigest
import java.util.Base64
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds

// WebSocket opcodes, see https://datatracker.ietf.org/doc/html/rfc6455#section-5.2
private const val OPCODE_TEXT = 0x1
private const val OPCODE_CLOSE = 0x8

// No test should come anywhere near these, they only exist so a broken connection fails
// the test instead of hanging the build.
private val TEST_TIMEOUT = 5.seconds
private const val SOCKET_TIMEOUT_IN_MS = 2_000

/**
 * Tests for [JervisClientWebSocketConnection] that require full control over
 * the underlying TCP connection, i.e., they cannot be written against a real
 * Jervis server. They are JVM-only because they are built on top of raw
 * sockets.
 */
class JervisClientWebSocketConnectionJvmTests {

    @Test
    fun abruptPeerDisconnectCompletesAwaitDisconnect() = runBlocking {
        val server = FakeWebSocketServer(this)
        server.accept {
            // Deliberately close the TCP connection without sending a
            // WebSocket close frame.
        }
        val connection = server.createConnection("abrupt-disconnect")
        connection.start()
        try {
            val closeReason = withTimeout(TEST_TIMEOUT) { connection.awaitDisconnect() }
            assertEquals(JervisExitCode.UNEXPECTED_ERROR.code, closeReason.code)
            // The application-facing channel must be closed as well, otherwise
            // consumers are left suspended in `receiveOrNull()` forever.
            assertNull(withTimeout(TEST_TIMEOUT) { connection.receiveOrNull() })
            assertFalse(connection.isActive)
        } finally {
            withTimeout(TEST_TIMEOUT) { connection.close() }
            server.stop()
        }
        // Manually closing after an unexpected close must not overwrite the
        // reason the connection actually died from.
        assertEquals(JervisExitCode.UNEXPECTED_ERROR.code, connection.getCloseReason()?.code)
    }

    @Test
    fun undecodableServerMessageClosesConnection() = runBlocking {
        val firstClientOpcode = CompletableDeferred<Int>()
        val server = FakeWebSocketServer(this)
        server.accept { socket ->
            socket.sendTextFrame("this-is-not-valid-json")
            // The transport stays up, so it is up to the client to end the connection.
            firstClientOpcode.complete(socket.readNextOpcode())
        }
        val connection = server.createConnection("undecodable-message")
        connection.start()
        try {
            val closeReason = withTimeout(TEST_TIMEOUT) { connection.awaitDisconnect() }
            assertEquals(JervisExitCode.UNEXPECTED_ERROR.code, closeReason.code)
            // The server is still reachable here, so it must be told why we are leaving.
            assertEquals(OPCODE_CLOSE, withTimeout(TEST_TIMEOUT) { firstClientOpcode.await() })
            assertNull(withTimeout(TEST_TIMEOUT) { connection.receiveOrNull() })
        } finally {
            withTimeout(TEST_TIMEOUT) { connection.close() }
            server.stop()
        }
    }

    @Test
    fun clientCloseReasonSurvivesAbruptPeerDisconnect() = runBlocking {
        val firstClientOpcode = CompletableDeferred<Int>()
        val server = FakeWebSocketServer(this)
        server.accept { socket ->
            // Drop the TCP connection instead of echoing the close frame, so
            // the client hits a transport error after it has already requested
            // the close itself.
            firstClientOpcode.complete(socket.readNextOpcode())
        }
        val connection = server.createConnection("client-close")
        connection.start()
        try {
            withTimeout(TEST_TIMEOUT) {
                while (!connection.isActive) yield()
            }
            withTimeout(TEST_TIMEOUT) { connection.close() }
            assertEquals(OPCODE_CLOSE, withTimeout(TEST_TIMEOUT) { firstClientOpcode.await() })
        } finally {
            server.stop()
        }
        assertEquals(JervisExitCode.CLIENT_CLOSING.code, connection.getCloseReason()?.code)
    }
}

/**
 * Minimal raw-socket WebSocket server. It implements just enough of RFC 6455
 * for these tests: the opening handshake, sending small unmasked text frames
 * and reading the opcode of whatever the client sends back. A raw socket is
 * used instead of a Ktor server because these tests need to drop the TCP
 * connection without a WebSocket close handshake.
 */
private class FakeWebSocketServer(private val scope: CoroutineScope) {

    private val serverSocket = ServerSocket(0)
    private var acceptJob: Job? = null

    fun createConnection(gameId: String): JervisClientWebSocketConnection {
        return JervisClientWebSocketConnection(
            gameId = GameId(gameId),
            url = "ws://127.0.0.1:${serverSocket.localPort}/game",
            coachName = "test",
        )
    }

    /**
     * Accept a single connection, complete the WebSocket upgrade and hand the
     * socket to [handler]. The TCP connection is dropped when [handler]
     * returns.
     */
    fun accept(handler: (Socket) -> Unit) {
        acceptJob = scope.launch(Dispatchers.IO) {
            try {
                serverSocket.accept().use { socket ->
                    socket.soTimeout = SOCKET_TIMEOUT_IN_MS
                    completeWebSocketUpgrade(socket)
                    handler(socket)
                }
            } catch (ex: CancellationException) {
                throw ex
            } catch (ex: Throwable) {
                // [stop] unblocks `accept()` with an exception. Letting it escape would cancel
                // the test scope and hide whatever the test was actually asserting.
                println("FakeWebSocketServer stopped: $ex")
            }
        }
    }

    suspend fun stop() {
        serverSocket.close()
        acceptJob?.cancelAndJoin()
    }

    private fun completeWebSocketUpgrade(socket: Socket) {
        val reader = socket.getInputStream().bufferedReader(Charsets.ISO_8859_1)
        val requestHeaders = buildMap {
            while (true) {
                val line = reader.readLine() ?: error("Client closed before the WebSocket upgrade")
                if (line.isEmpty()) break
                val separator = line.indexOf(':')
                if (separator > 0) {
                    put(line.substring(0, separator).lowercase(), line.substring(separator + 1).trim())
                }
            }
        }
        val key = requestHeaders["sec-websocket-key"] ?: error("Missing Sec-WebSocket-Key")
        val accept = Base64.getEncoder().encodeToString(
            MessageDigest.getInstance("SHA-1").digest(
                (key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").toByteArray(Charsets.ISO_8859_1)
            )
        )
        val response = buildString {
            append("HTTP/1.1 101 Switching Protocols\r\n")
            append("Upgrade: websocket\r\n")
            append("Connection: Upgrade\r\n")
            append("Sec-WebSocket-Accept: $accept\r\n")
            append("\r\n")
        }
        val output = socket.getOutputStream()
        output.write(response.toByteArray(Charsets.ISO_8859_1))
        output.flush()
    }
}

/** Send an unmasked, unfragmented text frame. Only small payloads are supported. */
private fun Socket.sendTextFrame(message: String) {
    val payload = message.toByteArray(Charsets.UTF_8)
    require(payload.size < 126) { "Payload is too large for a single-byte length: ${payload.size}" }
    val output = getOutputStream()
    output.write(byteArrayOf((0x80 or OPCODE_TEXT).toByte(), payload.size.toByte()))
    output.write(payload)
    output.flush()
}

/** The opcode of the next frame sent by the client, or `-1` if the client sent nothing. */
private fun Socket.readNextOpcode(): Int {
    return try {
        when (val firstByte = getInputStream().read()) {
            -1 -> -1
            else -> firstByte and 0x0F
        }
    } catch (ex: SocketTimeoutException) {
        -1
    }
}
