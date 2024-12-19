package com.jervisffb.net.test

import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.utils.createDefaultHomeTeam
import com.jervisffb.engine.utils.lizardMenAwayTeam
import com.jervisffb.net.GameId
import com.jervisffb.net.JervisClientWebSocketConnection
import com.jervisffb.net.JervisExitCode
import com.jervisffb.net.LightServer
import com.jervisffb.net.gameId
import com.jervisffb.net.messages.ConfirmGameStartMessage
import com.jervisffb.net.messages.GameActionMessage
import com.jervisffb.net.messages.GameReadyMessage
import com.jervisffb.net.messages.JervisErrorCode
import com.jervisffb.net.messages.JoinGameAsPlayerMessage
import com.jervisffb.net.messages.PlayerJoinedMessage
import com.jervisffb.net.messages.ServerError
import com.jervisffb.net.messages.StartGameMessage
import com.jervisffb.utils.getHttpClient
import com.jervisffb.utils.runBlocking
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.websocket.Frame
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

/**
 * Class testing setting up and starting a Standalone Game with a Host and Client.
 *
 * Note, standalone hosts are not available on WASM due to restrictions in the
 * server sandbox.
 */
class StandaloneHostTests {

    @Test
    fun startStandaloneGame() = runBlocking {
        // Start server
        val server = LightServer(createDefaultHomeTeam(), "test", testMode = true)
        server.start()

        val conn1 = JervisClientWebSocketConnection(GameId("test"),"ws://localhost:8080/game")
        conn1.start()
        val conn2 = JervisClientWebSocketConnection(GameId("test"),"ws://localhost:8080/game")
        conn2.start()

        // Host Joins
        val join1 = JoinGameAsPlayerMessage(
            GameId("test"),
            "host",
            null,
            createDefaultHomeTeam(),
        )
        conn1.send(join1)
        checkServerMessage<PlayerJoinedMessage>(conn1) {
            assertEquals("host", it.coachName)
        }

        // Client Joins
        val join2 = JoinGameAsPlayerMessage(
            GameId("test"),
            "client",
            null,
            lizardMenAwayTeam(),
        )
        conn2.send(join2)
        checkServerMessage<PlayerJoinedMessage>(conn1) {
            assertEquals("client", it.coachName)
        }
        checkServerMessage<PlayerJoinedMessage>(conn2) {
            assertEquals("client", it.coachName)
        }

        // Receive request to start game
        checkServerMessage<ConfirmGameStartMessage>(conn1) {
            assertEquals("test", it.gameId.value)
            assertEquals("HomeTeam", it.teams[0].teamName)
            assertEquals("AwayTeam", it.teams[1].teamName)
        }
        checkServerMessage<ConfirmGameStartMessage>(conn2) {
            assertEquals("test", it.gameId.value)
            assertEquals("HomeTeam", it.teams[0].teamName)
            assertEquals("AwayTeam", it.teams[1].teamName)
        }

        // Confirm starting game
        conn1.send(StartGameMessage(GameId("test")))
        conn2.send(StartGameMessage(GameId("test")))

        // Game is starting
        checkServerMessage<GameReadyMessage>(conn1) {
            assertEquals("test", it.gameId.value)
        }
        checkServerMessage<GameReadyMessage>(conn2) {
            assertEquals("test", it.gameId.value)
        }

        conn1.close()
        conn2.close()
        server.stop()
    }

    @Test
    fun closeSessionWithoutSendingData() = runBlocking {
        val server = LightServer(createDefaultHomeTeam(), "test", testMode = true)
        server.start()
        try {
            val conn = JervisClientWebSocketConnection("test".gameId,"ws://localhost:8080/game")
            conn.start()
            conn.close()
            assertEquals(JervisExitCode.CLIENT_CLOSING.code, conn.getCloseReason()?.code)
        } finally {
            server.stop()
        }
    }

    // This is only possible when working around the current APIs. But since we do not control the
    // Client connecting, we need to verify this case as well.
    @Test
    fun sendingUnsupportedMessageStopsConnection() = runBlocking {
        val server = LightServer(createDefaultHomeTeam(), "test", testMode = true)
        server.start()
        val client = getHttpClient()
        val session = client.webSocketSession("ws://localhost:8080/game")
        try {
            session.send(Frame.Text("Hello World"))
            val closeReason = session.closeReason.await()
            assertNotNull(closeReason)
            assertEquals(JervisExitCode.UNEXPECTED_ERROR.code, closeReason.code)
            assertTrue(closeReason.message.startsWith("kotlinx.serialization.json.internal.JsonDecodingException"))
        } finally {
            // Unsure why we need to also close the session on this side to avoid coroutine errors
//            session.close(CloseReason(CloseReason.Codes.NORMAL, ""))
            client.close()
            server.stop()
        }
    }

    @Test
    fun sendingWrongInitialMessageTerminatesConnection() = runBlocking {
        val server = LightServer(createDefaultHomeTeam(), "test", testMode = true)
        server.start()
        val conn = JervisClientWebSocketConnection(GameId("test"),"ws://localhost:8080/game")
        conn.start()
        try {
            conn.send(StartGameMessage("test".gameId))
            val closeReason = conn.awaitDisconnect(5.seconds)
            assertEquals(JervisExitCode.WRONG_STARTING_MESSAGE.code, closeReason.code)
            assertFalse(conn.isActive)
        } finally {
            conn.close()
            server.stop()
        }
    }

    // After the initial Join message is accepted, we do allow the Client to send "wrong" messages.
    // The server will just respond with a JervisErrorCode.PROTOCOL_ERROR allowing the client
    // to send another message. This is a good behavior for development, but maybe we should consider
    // terminating the connection in "prod" mode.
    @Test
    fun sendingWrongMessageAfterInitialJoinDoesNotTerminateSession() = runBlocking {
        // Start server
        val server = LightServer(createDefaultHomeTeam(), "test", testMode = true)
        server.start()

        val conn1 = JervisClientWebSocketConnection(GameId("test"),"ws://localhost:8080/game")
        conn1.start()
        val conn2 = JervisClientWebSocketConnection(GameId("test"),"ws://localhost:8080/game")
        conn2.start()

        // Host Joins
        val join1 = JoinGameAsPlayerMessage(
            GameId("test"),
            "host",
            null,
            createDefaultHomeTeam(),
        )
        conn1.send(join1)
        checkServerMessage<PlayerJoinedMessage>(conn1) {
            assertEquals("host", it.coachName)
        }

        // Client Joins
        val join2 = JoinGameAsPlayerMessage(
            GameId("test"),
            "client",
            null,
            lizardMenAwayTeam(),
        )
        conn2.send(join2)
        checkServerMessage<PlayerJoinedMessage>(conn1) {
            assertEquals("client", it.coachName)
        }
        checkServerMessage<PlayerJoinedMessage>(conn2) {
            assertEquals("client", it.coachName)
        }

        // Receive request to start game
        consumeServerMessage<ConfirmGameStartMessage>(conn1)
        consumeServerMessage<ConfirmGameStartMessage>(conn2)

        conn1.send(GameActionMessage(Continue))
        checkServerMessage<ServerError>(conn1) {
            assertEquals(JervisErrorCode.PROTOCOL_ERROR, it.errorCode)
        }
        conn1.send(StartGameMessage("test".gameId))
        conn2.send(StartGameMessage("test".gameId))
        consumeServerMessage<GameReadyMessage>(conn1)
        consumeServerMessage<GameReadyMessage>(conn2)

        conn1.close()
        conn2.close()
        server.stop()
    }

    @Test
    fun serverTerminatesWithConnectedClients() {

    }

    private suspend inline fun <reified T> checkServerMessage(connection: JervisClientWebSocketConnection, assertFunc: (T) -> Unit) {
        val serverMessage = connection.receiveOrNull()
        if (serverMessage !is T) {
            throw AssertionError("Expected ${T::class.simpleName}, got $serverMessage. Close reason: ${connection.getCloseReason()}")
        }
        assertFunc(serverMessage)
    }

    private suspend inline fun <reified T> consumeServerMessage(connection: JervisClientWebSocketConnection) {
        val serverMessage = connection.receiveOrNull()
        if (serverMessage !is T) throw AssertionError("Expected ${T::class.simpleName}, got $serverMessage. Close reason: ${connection.getCloseReason()}")
    }
}
