package com.jervisffb.net.test

import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.model.CoachId
import com.jervisffb.engine.model.GameDeltaId
import com.jervisffb.engine.rules.StandardBB2020Rules
import com.jervisffb.net.GameId
import com.jervisffb.net.JervisClientWebSocketConnection
import com.jervisffb.net.JervisExitCode
import com.jervisffb.net.LightServer
import com.jervisffb.net.gameId
import com.jervisffb.net.messages.CoachJoinedMessage
import com.jervisffb.net.messages.ConfirmGameStartMessage
import com.jervisffb.net.messages.GameActionMessage
import com.jervisffb.net.messages.GameReadyMessage
import com.jervisffb.net.messages.GameStateSyncMessage
import com.jervisffb.net.messages.JervisErrorCode
import com.jervisffb.net.messages.JoinGameAsCoachMessage
import com.jervisffb.net.messages.P2PClientState
import com.jervisffb.net.messages.P2PHostState
import com.jervisffb.net.messages.P2PTeamInfo
import com.jervisffb.net.messages.ServerError
import com.jervisffb.net.messages.StartGameMessage
import com.jervisffb.net.messages.TeamJoinedMessage
import com.jervisffb.net.messages.TeamSelectedMessage
import com.jervisffb.net.messages.UpdateClientStateMessage
import com.jervisffb.net.messages.UpdateHostStateMessage
import com.jervisffb.test.createDefaultHomeTeam
import com.jervisffb.test.lizardMenAwayTeam
import com.jervisffb.utils.getHttpClient
import com.jervisffb.utils.runBlocking
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.websocket.Frame
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

/**
 * Class testing setting up and starting a P2P Game with a Host and Client.
 *
 * Note, P2P hosts are not available on WASM due to restrictions in the
 * server sandbox.
 */
class P2PNetworkTests {

    val rules = StandardBB2020Rules()
    val server = LightServer(
        gameName = "test",
        rules = rules,
        hostCoach = CoachId("HomeCoachID"),
        hostTeam = createDefaultHomeTeam(),
        clientCoach = null,
        clientTeam = null,
        testMode = true
    )

    @Test
    fun startP2PGame() = runBlocking {
        // Start server
        server.start()

        val conn1 = JervisClientWebSocketConnection(GameId("test"), "ws://localhost:8080/joinGame?id=test", "host")
        conn1.start()
        val conn2 = JervisClientWebSocketConnection(GameId("test"), "ws://localhost:8080/joinGame?id=test", "client")
        conn2.start()

        // Host Joins
        val join1 = JoinGameAsCoachMessage(
            GameId("test"),
            "host",
            null,
            "host",
            true,
            P2PTeamInfo(createDefaultHomeTeam())
        )
        conn1.send(join1)
        consumeServerMessage<GameStateSyncMessage>(conn1)
        checkServerMessage<CoachJoinedMessage>(conn1) {
            assertEquals("host", it.coach.name)
        }
        checkServerMessage<TeamJoinedMessage>(conn1) {
            assertEquals("HomeTeam", it.getTeam().name)
        }
        checkServerMessage<UpdateHostStateMessage>(conn1) {
            assertEquals(P2PHostState.WAIT_FOR_CLIENT, it.state)
        }

        // Client Joins
        val join2 = JoinGameAsCoachMessage(
            GameId("test"),
            "client",
            null,
            "client",
            false
        )
        conn2.send(join2)
        checkServerMessage<CoachJoinedMessage>(conn1) {
            assertEquals("client", it.coach.name)
        }
        consumeServerMessage<GameStateSyncMessage>(conn2)
        checkServerMessage<CoachJoinedMessage>(conn2) {
            assertEquals("client", it.coach.name)
        }
        checkServerMessage<UpdateClientStateMessage>(conn2) {
            assertEquals(P2PClientState.SELECT_TEAM, it.state)
        }

        // Client selects team
        conn2.send(TeamSelectedMessage(P2PTeamInfo(lizardMenAwayTeam())))
        checkServerMessage<TeamJoinedMessage>(conn1) {
            assertFalse(it.isHomeTeam)
            assertEquals("AwayTeam", it.getTeam().name)
        }
        checkServerMessage<TeamJoinedMessage>(conn2) {
            assertFalse(it.isHomeTeam)
            assertEquals("AwayTeam", it.getTeam().name)
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
        checkServerMessage<UpdateHostStateMessage>(conn1) {
            assertEquals(P2PHostState.ACCEPT_GAME, it.state)
        }
        checkServerMessage<UpdateClientStateMessage>(conn2) {
            assertEquals(P2PClientState.ACCEPT_GAME, it.state)
        }

        // Confirm starting game
        conn1.send(StartGameMessage(true))
        conn2.send(StartGameMessage(true))

        // Game is starting
        checkServerMessage<GameReadyMessage>(conn1) {
            assertEquals("test", it.gameId.value)
        }
        checkServerMessage<GameReadyMessage>(conn2) {
            assertEquals("test", it.gameId.value)
        }
        checkServerMessage<UpdateHostStateMessage>(conn1) {
            assertEquals(P2PHostState.RUN_GAME, it.state)
        }
        checkServerMessage<UpdateClientStateMessage>(conn2) {
            assertEquals(P2PClientState.RUN_GAME, it.state)
        }

        conn1.close()
        conn2.close()
        server.stop()
    }

    @Test
    fun closeSessionWithoutSendingData() = runBlocking {
        server.start()
        try {
            val conn = JervisClientWebSocketConnection("test".gameId, "ws://localhost:8080/joinGame?id=test", "host")
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
        server.start()
        val client = getHttpClient()
        val session = client.webSocketSession("ws://localhost:8080/joinGame?id=test")
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
        server.start()
        val conn = JervisClientWebSocketConnection(GameId("test"), "ws://localhost:8080/joinGame?id=test", "host")
        conn.start()
        try {
            // Sending a message that is not a JoinAs* message.
            conn.send(StartGameMessage(true))
            withTimeout(5.seconds) {
                val closeReason = conn.awaitDisconnect()
                assertEquals(JervisExitCode.WRONG_STARTING_MESSAGE.code, closeReason.code)
                assertFalse(conn.isActive)
            }
        } finally {
            conn.close()
            server.stop()
        }
    }

    @Test
    fun sendingWrongGameIdTerminatesConnection() = runBlocking {
        server.start()
        val conn = JervisClientWebSocketConnection(GameId("wrongGameId"), "ws://localhost:8080/joinGame?id=wrongGameId", "host")
        conn.start()
        try {
            withTimeout(5.seconds) {
                val closeReason = conn.awaitDisconnect()
                assertEquals(JervisExitCode.NO_GAME_FOUND.code, closeReason.code)
                assertFalse(conn.isActive)
            }
        } finally {
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
        server.start()

        val conn1 = JervisClientWebSocketConnection(GameId("test"), "ws://localhost:8080/joinGame?id=test", "host")
        conn1.start()
        val conn2 = JervisClientWebSocketConnection(GameId("test"), "ws://localhost:8080/joinGame?id=test", "host")
        conn2.start()

        // Host Joins
        val join1 = JoinGameAsCoachMessage(
            GameId("test"),
            "host",
            null,
            "host",
            true,
            P2PTeamInfo(createDefaultHomeTeam())
        )
        conn1.send(join1)
        consumeServerMessage<GameStateSyncMessage>(conn1)
        checkServerMessage<CoachJoinedMessage>(conn1) {
            assertEquals("host", it.coach.name)
        }
        consumeServerMessage<TeamJoinedMessage>(conn1)
        consumeServerMessage<UpdateHostStateMessage>(conn1)

        // Client Joins
        val join2 = JoinGameAsCoachMessage(
            GameId("test"),
            "client",
            null,
            "client",
            false
        )
        conn2.send(join2)
        consumeServerMessage<GameStateSyncMessage>(conn2)
        checkServerMessage<CoachJoinedMessage>(conn1) {
            assertEquals("client", it.coach.name)
        }
        checkServerMessage<CoachJoinedMessage>(conn2) {
            assertEquals("client", it.coach.name)
        }
        consumeServerMessage<UpdateClientStateMessage>(conn2)

        // Host sends message not supported at this point (it should be team selection)
        conn1.send(GameActionMessage(GameDeltaId(100), Continue))
        checkServerMessage<ServerError>(conn1) {
            assertEquals(JervisErrorCode.INVALID_GAME_ACTION, it.errorCode)
        }

        // Host selects team
        conn1.send(TeamSelectedMessage(P2PTeamInfo(createDefaultHomeTeam())))
        consumeServerMessage<TeamJoinedMessage>(conn1)
        consumeServerMessage<TeamJoinedMessage>(conn2)

        conn1.close()
        conn2.close()
        server.stop()
    }

    @Test
    fun serverTerminatesWithConnectedClients() {
        // TODO
    }

    @Test
    fun serverRejectsTooManyCoachClients() {
        // TODO
    }

    @Test
    fun serverSendsGameSessionStateOnConnect() {
        // TODO
    }

    @Test
    fun serverSendsLatestGameSessionStateOnReconnect() {
        // TODO
    }

    @Test
    fun serverSendsRevertGameActionClientIfWrong() {
        // TODO
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
