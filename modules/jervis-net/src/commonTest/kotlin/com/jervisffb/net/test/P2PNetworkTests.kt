package com.jervisffb.net.test

import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.GameActionId
import com.jervisffb.engine.actions.Revert
import com.jervisffb.engine.actions.Undo
import com.jervisffb.engine.ext.d3
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.model.CoachId
import com.jervisffb.engine.model.CoachType
import com.jervisffb.engine.rules.StandardBB2020Rules
import com.jervisffb.engine.rules.builder.DiceRollOwner
import com.jervisffb.engine.rules.builder.UndoActionBehavior
import com.jervisffb.net.GameId
import com.jervisffb.net.JervisClientWebSocketConnection
import com.jervisffb.net.JervisExitCode
import com.jervisffb.net.LightServer
import com.jervisffb.net.gameId
import com.jervisffb.net.messages.AcceptGameMessage
import com.jervisffb.net.messages.CoachJoinedMessage
import com.jervisffb.net.messages.CoachLeftMessage
import com.jervisffb.net.messages.ConfirmGameStartMessage
import com.jervisffb.net.messages.GameActionMessage
import com.jervisffb.net.messages.GameReadyMessage
import com.jervisffb.net.messages.GameStateSyncMessage
import com.jervisffb.net.messages.InvalidGameActionOwnerServerError
import com.jervisffb.net.messages.InvalidGameActionTypeServerError
import com.jervisffb.net.messages.JervisErrorCode
import com.jervisffb.net.messages.JoinGameAsCoachMessage
import com.jervisffb.net.messages.OutOfOrderGameActionServerError
import com.jervisffb.net.messages.P2PClientState
import com.jervisffb.net.messages.P2PHostState
import com.jervisffb.net.messages.P2PTeamInfo
import com.jervisffb.net.messages.ServerError
import com.jervisffb.net.messages.SyncGameActionMessage
import com.jervisffb.net.messages.TeamJoinedMessage
import com.jervisffb.net.messages.TeamSelectedMessage
import com.jervisffb.net.messages.UpdateClientStateMessage
import com.jervisffb.net.messages.UpdateHostStateMessage
import com.jervisffb.test.createDefaultHomeTeam
import com.jervisffb.test.humanTeamAway
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

    val rules = StandardBB2020Rules().toBuilder().run {
        diceRollsOwner = DiceRollOwner.ROLL_ON_CLIENT
        undoActionBehavior = UndoActionBehavior.ALLOWED
        timers.timersEnabled = false
        build()
    }
    val server = LightServer(
        gameName = "test",
        rules = rules,
        hostCoach = CoachId("HomeCoachID"),
        hostTeam = createDefaultHomeTeam(rules),
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
            CoachType.HUMAN,
            true,
            P2PTeamInfo(createDefaultHomeTeam(rules))
        )
        conn1.send(join1)
        consumeServerMessage<GameStateSyncMessage>(conn1)
        checkServerMessage<CoachJoinedMessage>(conn1) {
            assertEquals("host", it.coach.name)
        }
        checkServerMessage<TeamJoinedMessage>(conn1) {
            assertEquals("HomeTeam", it.getTeam(rules).name)
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
            CoachType.HUMAN,
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
        conn2.send(TeamSelectedMessage(P2PTeamInfo(lizardMenAwayTeam(rules))))
        checkServerMessage<TeamJoinedMessage>(conn1) {
            assertFalse(it.isHomeTeam)
            assertEquals("AwayTeam", it.getTeam(rules).name)
        }
        checkServerMessage<TeamJoinedMessage>(conn2) {
            assertFalse(it.isHomeTeam)
            assertEquals("AwayTeam", it.getTeam(rules).name)
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
        conn1.send(AcceptGameMessage(true))
        conn2.send(AcceptGameMessage(true))

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


    // Test for a Host starting a game, a Client joins, selects a team, regrets the choice
    // and then submit another team that gets accepted
    @Test
    fun clientRejectsGame() = runBlocking {
        // Start server
        server.start()

        val conn1 = JervisClientWebSocketConnection(GameId("test"), "ws://localhost:8080/joinGame?id=test", "host")
        conn1.start()
        var conn2 = JervisClientWebSocketConnection(GameId("test"), "ws://localhost:8080/joinGame?id=test", "client")
        conn2.start()

        hostAndClientSelectTeams(conn1, conn2)

        // Client rejects the game
        conn2.send(AcceptGameMessage(false))
        conn2.awaitDisconnect().also {
            assertEquals(JervisExitCode.GAME_NOT_ACCEPTED.code, it.code)
        }
        checkServerMessage<UpdateHostStateMessage>(conn1) {
            assertEquals(P2PHostState.WAIT_FOR_CLIENT, it.state)
        }
        checkServerMessage<UpdateClientStateMessage>(conn2) {
            assertEquals(P2PClientState.JOIN_SERVER, it.state)
        }
        conn2.close()
        consumeServerMessage<CoachLeftMessage>(conn1)

        // Client reconnects
        conn2 = JervisClientWebSocketConnection(GameId("test"), "ws://localhost:8080/joinGame?id=test", "client")
        conn2.start()
        val join2 = JoinGameAsCoachMessage(
            GameId("test"),
            "client",
            null,
            "client",
            CoachType.HUMAN,
            false
        )
        conn2.send(join2)
        consumeServerMessage<CoachJoinedMessage>(conn1)
        consumeServerMessage<GameStateSyncMessage>(conn2)
        consumeServerMessage<CoachJoinedMessage>(conn2)
        consumeServerMessage<UpdateClientStateMessage>(conn2)

        // Client selects new team
        conn2.send(TeamSelectedMessage(P2PTeamInfo(humanTeamAway(rules))))
        consumeServerMessage<TeamJoinedMessage>(conn1)
        consumeServerMessage<TeamJoinedMessage>(conn2)

        // Receive request to start game
        consumeServerMessage<ConfirmGameStartMessage>(conn1)
        consumeServerMessage<ConfirmGameStartMessage>(conn2)
        consumeServerMessage<UpdateHostStateMessage>(conn1)
        consumeServerMessage<UpdateClientStateMessage>(conn2)

        // Confirm starting game
        conn1.send(AcceptGameMessage(true))
        conn2.send(AcceptGameMessage(true))

        // Game is starting
        consumeServerMessage<GameReadyMessage>(conn1)
        consumeServerMessage<GameReadyMessage>(conn2)
        consumeServerMessage<UpdateHostStateMessage>(conn1)
        consumeServerMessage<UpdateClientStateMessage>(conn2)

        conn1.close()
        conn2.close()
        server.stop()
    }

    // Test a Host setting up a game, a Client joins and selects a team
    // When we get to accepting the game, the Host rejects.
    @Test
    fun hostRejectsGame() = runBlocking {
        // Start server
        server.start()

        val conn1 = JervisClientWebSocketConnection(GameId("test"), "ws://localhost:8080/joinGame?id=test", "host")
        conn1.start()
        var conn2 = JervisClientWebSocketConnection(GameId("test"), "ws://localhost:8080/joinGame?id=test", "client")
        conn2.start()

        hostAndClientSelectTeams(conn1, conn2)

        // Host rejects the game
        conn1.send(AcceptGameMessage(false))
        checkServerMessage<UpdateHostStateMessage>(conn1) {
            assertEquals(P2PHostState.SETUP_GAME, it.state)
        }
        conn1.awaitDisconnect().also {
            assertEquals(JervisExitCode.GAME_NOT_ACCEPTED.code, it.code)
        }
        conn2.awaitDisconnect().also {
            assertEquals(JervisExitCode.GAME_NOT_ACCEPTED.code, it.code)
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
            conn.send(AcceptGameMessage(true))
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
            CoachType.HUMAN,
            true,
            P2PTeamInfo(createDefaultHomeTeam(rules))
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
            CoachType.HUMAN,
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
        conn1.send(GameActionMessage(GameActionId(100), Continue))
        checkServerMessage<ServerError>(conn1) {
            assertEquals(JervisErrorCode.OUT_OF_ORDER_GAME_ACTION, it.errorCode)
        }

        // Host selects team
        conn1.send(TeamSelectedMessage(P2PTeamInfo(createDefaultHomeTeam(rules))))
        consumeServerMessage<TeamJoinedMessage>(conn1)
        consumeServerMessage<TeamJoinedMessage>(conn2)

        conn1.close()
        conn2.close()
        server.stop()
    }


    @Test
    fun serverSendsErrorIfWrongClientSendsAction() {
        startGame { homeConn, awayConn ->
            // Home team is expected send a factor factor roll, but away team sends it instead.
            // This should result in the server sending an error to Away team, but not change the
            // server game state.
            awayConn.send(GameActionMessage(GameActionId(1), 1.d3))
            checkServerMessage<InvalidGameActionOwnerServerError>(awayConn) { errorMessage ->
                assertEquals(GameActionId(1),  errorMessage.actionId)
            }
            homeConn.send(GameActionMessage(GameActionId(1), 1.d3))
            checkServerMessage<SyncGameActionMessage>(awayConn) { message ->
                assertEquals(GameActionId(1), message.serverIndex)
            }
        }
    }

    @Test
    fun serverSendsErrorIfActionIsOutOfOrder() {
        startGame { homeConn, awayConn ->
            // The Home team is expected to send a fan factor roll, but we fake the action id to be
            // too far in the future. The server should reject messages sending unexpected action ids.
            homeConn.send(GameActionMessage(GameActionId(2), 1.d3))
            checkServerMessage<OutOfOrderGameActionServerError>(homeConn) { errorMessage ->
                assertEquals(GameActionId(2),  errorMessage.actionId)
            }
            homeConn.send(GameActionMessage(GameActionId(1), 1.d3))
            checkServerMessage<SyncGameActionMessage>(awayConn) { message ->
                assertEquals(GameActionId(1), message.serverIndex)
            }
        }
    }

    @Test
    fun serverSendsErrorIfActionIsWrongType() {
        startGame { homeConn, awayConn ->
            // The Home team is expected to send a fan factor roll, but sends another event (but with the
            // correct action id)
            homeConn.send(GameActionMessage(GameActionId(1), 1.d6))
            checkServerMessage<InvalidGameActionTypeServerError>(homeConn) { errorMessage ->
                assertEquals(GameActionId(1),  errorMessage.actionId)
            }
            homeConn.send(GameActionMessage(GameActionId(1), 1.d3))
            checkServerMessage<SyncGameActionMessage>(awayConn) { message ->
                assertEquals(GameActionId(1), message.serverIndex)
            }
        }
    }

    @Test
    fun serverNeverAcceptsRevertActions() {
        startGame { homeConn, awayConn ->
            // The Home team is expected to send a fan factor roll. But isn't allowed to "Revert" it
            // on the server. Only "Undo" it.
            homeConn.send(GameActionMessage(GameActionId(1), 1.d3))
            consumeServerMessage<SyncGameActionMessage>(awayConn)
            // Invalid action
            homeConn.send(GameActionMessage(GameActionId(2), Revert))
            checkServerMessage<InvalidGameActionTypeServerError>(homeConn) { errorMessage ->
                assertEquals(GameActionId(2),  errorMessage.actionId)
            }
            // Since we are waiting on the Away coach, only this coach is allowed to undo (even though it undoes
            // the home coaches roll)
            awayConn.send(GameActionMessage(GameActionId(2), Undo))
            consumeServerMessage<SyncGameActionMessage>(homeConn)
            homeConn.send(GameActionMessage(GameActionId(3), 1.d3))
            checkServerMessage<SyncGameActionMessage>(awayConn) { message ->
                assertEquals(GameActionId(3), message.serverIndex)
            }
        }
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

    // Helper method that starts a came and puts it at the "Waiting for first action" stage.
    // Connections and server will be closed regardless if any exception is thrown.
    private fun startGame(block: suspend (JervisClientWebSocketConnection, JervisClientWebSocketConnection) -> Unit) = runBlocking {
        server.start()
        val conn1 = JervisClientWebSocketConnection(GameId("test"), "ws://localhost:8080/joinGame?id=test", "host")
        conn1.start()
        val conn2 = JervisClientWebSocketConnection(GameId("test"), "ws://localhost:8080/joinGame?id=test", "client")
        conn2.start()
        val join1 = JoinGameAsCoachMessage(
            GameId("test"),
            "host",
            null,
            "host",
            CoachType.HUMAN,
            true,
            P2PTeamInfo(createDefaultHomeTeam(rules))
        )
        conn1.send(join1)
        consumeServerMessage<GameStateSyncMessage>(conn1)
        consumeServerMessage<CoachJoinedMessage>(conn1)
        consumeServerMessage<TeamJoinedMessage>(conn1)
        consumeServerMessage<UpdateHostStateMessage>(conn1)

        // Client Joins
        val join2 = JoinGameAsCoachMessage(
            GameId("test"),
            "client",
            null,
            "client",
            CoachType.HUMAN,
            false
        )
        conn2.send(join2)
        consumeServerMessage<CoachJoinedMessage>(conn1)
        consumeServerMessage<GameStateSyncMessage>(conn2)
        consumeServerMessage<CoachJoinedMessage>(conn2)
        consumeServerMessage<UpdateClientStateMessage>(conn2)

        // Client selects team
        conn2.send(TeamSelectedMessage(P2PTeamInfo(lizardMenAwayTeam(rules))))
        consumeServerMessage<TeamJoinedMessage>(conn1)
        consumeServerMessage<TeamJoinedMessage>(conn2)

        // Receive request to start game
        consumeServerMessage<ConfirmGameStartMessage>(conn1)
        consumeServerMessage<ConfirmGameStartMessage>(conn2)
        consumeServerMessage<UpdateHostStateMessage>(conn1)
        consumeServerMessage<UpdateClientStateMessage>(conn2)

        // Confirm starting game
        conn1.send(AcceptGameMessage(true))
        conn2.send(AcceptGameMessage(true))

        // Game is starting
        consumeServerMessage<GameReadyMessage>(conn1)
        consumeServerMessage<GameReadyMessage>(conn2)
        consumeServerMessage<UpdateHostStateMessage>(conn1)
        consumeServerMessage<UpdateClientStateMessage>(conn2)
        try {
            block(conn1, conn2)
        } finally {
            conn1.close()
            conn2.close()
            server.stop()
        }
    }

    // Helper method that connects a Host and a Client and let
    // them both choose teams. Making the server state machine ready to accept the game.
    private suspend fun hostAndClientSelectTeams(
        conn1: JervisClientWebSocketConnection,
        conn2: JervisClientWebSocketConnection
    ) {
        // Host Joins
        val join1 = JoinGameAsCoachMessage(
            GameId("test"),
            "host",
            null,
            "host",
            CoachType.HUMAN,
            true,
            P2PTeamInfo(createDefaultHomeTeam(rules))
        )
        conn1.send(join1)
        consumeServerMessage<GameStateSyncMessage>(conn1)
        consumeServerMessage<CoachJoinedMessage>(conn1)
        consumeServerMessage<TeamJoinedMessage>(conn1)
        consumeServerMessage<UpdateHostStateMessage>(conn1)

        // Client Joins
        val join2 = JoinGameAsCoachMessage(
            GameId("test"),
            "client",
            null,
            "client",
            CoachType.HUMAN,
            false
        )
        conn2.send(join2)
        consumeServerMessage<CoachJoinedMessage>(conn1)
        consumeServerMessage<GameStateSyncMessage>(conn2)
        consumeServerMessage<CoachJoinedMessage>(conn2)
        consumeServerMessage<UpdateClientStateMessage>(conn2)

        // Client selects team
        conn2.send(TeamSelectedMessage(P2PTeamInfo(lizardMenAwayTeam(rules))))
        consumeServerMessage<TeamJoinedMessage>(conn1)
        consumeServerMessage<TeamJoinedMessage>(conn2)

        // Receive request to start game
        consumeServerMessage<ConfirmGameStartMessage>(conn1)
        consumeServerMessage<ConfirmGameStartMessage>(conn2)
        consumeServerMessage<UpdateHostStateMessage>(conn1)
        consumeServerMessage<UpdateClientStateMessage>(conn2)
    }

}
