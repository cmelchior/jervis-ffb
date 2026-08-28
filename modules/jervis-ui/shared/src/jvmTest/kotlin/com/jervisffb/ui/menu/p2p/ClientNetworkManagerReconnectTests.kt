package com.jervisffb.ui.menu.p2p

import com.jervisffb.engine.bb2025.StandardBB2025Rules
import com.jervisffb.engine.model.Coach
import com.jervisffb.engine.model.CoachId
import com.jervisffb.engine.model.CoachType
import com.jervisffb.engine.model.Team
import com.jervisffb.net.GameId
import com.jervisffb.net.JervisExitCode
import com.jervisffb.net.LightServer
import com.jervisffb.net.messages.P2PHostState
import com.jervisffb.test.bb2025.createDefaultHomeTeamBB2025
import com.jervisffb.utils.runBlocking
import io.ktor.websocket.CloseReason
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import java.net.ServerSocket
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

/**
 * Tests for how [ClientNetworkManager] handles being pointed at a new
 * connection, which happens every time the Host restarts its server and the
 * Client joins again.
 */
class ClientNetworkManagerReconnectTests {

    private val rules = StandardBB2025Rules()
    private val timeout = 5.seconds

    private class CountingHandler : AbstractClintNetworkMessageHandler() {
        var coachJoinedCount = 0
            private set
        val firstCoachJoined = CompletableDeferred<Unit>()

        override fun onCoachJoined(coach: Coach, isHomeCoach: Boolean) {
            coachJoinedCount++
            firstCoachJoined.complete(Unit)
        }
    }

    private class ConnectionStateHandler : AbstractClintNetworkMessageHandler() {
        val states = mutableListOf<String>()
        val connected = CompletableDeferred<Unit>()
        val disconnected = CompletableDeferred<Unit>()

        override fun onConnecting() {
            states.add("connecting")
        }

        override fun onConnected() {
            states.add("connected")
            connected.complete(Unit)
        }

        override fun onDisconnected(reason: CloseReason) {
            states.add("disconnected")
            disconnected.complete(Unit)
        }
    }

    private fun freePort(): Int = ServerSocket(0).use { it.localPort }

    private fun createServer(port: Int, hostTeam: Team) = LightServer(
        gameName = "test",
        rules = rules,
        hostCoach = CoachId("HomeCoachID"),
        hostTeam = hostTeam,
        clientCoach = null,
        clientTeam = null,
        testMode = true,
        port = port,
    )

    private suspend fun joinAs(
        adapter: P2PClientNetworkAdapter,
        port: Int,
        coachName: String,
        handler: AbstractClintNetworkMessageHandler,
    ) {
        adapter.joinHost(
            gameUrl = "ws://localhost:$port/joinGame?id=test",
            coachName = coachName,
            coachType = CoachType.HUMAN,
            gameId = GameId("test"),
            teamIfHost = null,
            handler = handler,
        )
    }

    @Test
    fun connectionStaysConnectingUntilTheServerResponds(): Unit = runBlocking {
        val port = freePort()
        val hostTeam = createDefaultHomeTeamBB2025(rules)
        val adapter = P2PClientNetworkAdapter()
        val handler = ConnectionStateHandler()
        val server = createServer(port, hostTeam)
        server.start()

        try {
            joinAs(adapter, port, "client", handler)
            withTimeout(timeout) { handler.connected.await() }
            assertEquals(listOf("connecting", "connected"), handler.states)
        } finally {
            server.stop()
        }
    }

    @Test
    fun failedConnectionNeverReportsConnected(): Unit = runBlocking {
        val adapter = P2PClientNetworkAdapter()
        val handler = ConnectionStateHandler()

        joinAs(adapter, freePort(), "client", handler)
        withTimeout(timeout) { handler.disconnected.await() }

        assertEquals(listOf("connecting", "disconnected"), handler.states)
    }

    @Test
    fun aHandlerThatThrowsDoesNotDisableLaterConnections(): Unit = runBlocking {
        val port = freePort()
        val hostTeam = createDefaultHomeTeamBB2025(rules)
        val adapter = P2PClientNetworkAdapter()

        // Handlers come from the UI and reach a lot of code. One of them blowing
        // up should not cancel the scope that controls all connections.
        adapter.addMessageHandler(object : AbstractClintNetworkMessageHandler() {
            override fun onCoachJoined(coach: Coach, isHomeCoach: Boolean) {
                throw IllegalStateException("Handler is broken")
            }

            override fun onDisconnected(reason: CloseReason) {
                throw IllegalStateException("Handler is broken")
            }
        })

        val firstServer = createServer(port, hostTeam)
        firstServer.start()
        val firstSession = CountingHandler()
        joinAs(adapter, port, "client", firstSession)
        withTimeout(timeout) { firstSession.firstCoachJoined.await() }
        firstServer.stop()

        val secondServer = createServer(port, hostTeam)
        secondServer.start()
        try {
            val secondSession = CountingHandler()
            joinAs(adapter, port, "client", secondSession)
            withTimeout(timeout) { secondSession.firstCoachJoined.await() }
        } finally {
            secondServer.stop()
        }
    }

    @Test
    fun aHandlerThatThrowsTakesTheConnectionDown(): Unit = runBlocking {
        val port = freePort()
        val hostTeam = createDefaultHomeTeamBB2025(rules)
        val adapter = P2PClientNetworkAdapter()

        // A handler failure used to be logged and nothing more, which left the client connected to
        // a server that saw nothing wrong while the UI kept waiting for a state it would never
        // reach. The connection is closed instead, so the coach sees the failure and the server
        // learns why we left.
        val disconnected = CompletableDeferred<CloseReason>()
        adapter.addMessageHandler(object : AbstractClintNetworkMessageHandler() {
            override fun onCoachJoined(coach: Coach, isHomeCoach: Boolean) {
                throw IllegalStateException("Handler is broken")
            }

            override fun onDisconnected(reason: CloseReason) {
                disconnected.complete(reason)
            }
        })

        val server = createServer(port, hostTeam)
        server.start()
        try {
            joinAs(adapter, port, "client", CountingHandler())
            val reason = withTimeout(timeout) { disconnected.await() }
            assertEquals(JervisExitCode.UNEXPECTED_ERROR.code, reason.code)
        } finally {
            server.stop()
        }
    }

    @Test
    fun givingUpOnAConnectionStopsDeliveringMessages(): Unit = runBlocking {
        val port = freePort()
        val hostTeam = createDefaultHomeTeamBB2025(rules)
        val adapter = P2PClientNetworkAdapter()

        // A host join is answered with a burst of four messages, so the ones after the failure are
        // typically already on their way. None of them may reach the handlers: they would drive the
        // UI forward past the point where it broke, e.g. reaching WAIT_FOR_CLIENT on a model that
        // never got its team.
        val disconnected = CompletableDeferred<CloseReason>()
        val deliveredAfterFailure = mutableListOf<String>()
        adapter.addMessageHandler(object : AbstractClintNetworkMessageHandler() {
            override fun onCoachJoined(coach: Coach, isHomeCoach: Boolean) {
                throw IllegalStateException("Handler is broken")
            }

            override fun onTeamSelected(team: Team, homeTeam: Boolean) {
                deliveredAfterFailure += "onTeamSelected"
            }

            override fun onHostStateChange(newState: P2PHostState) {
                deliveredAfterFailure += "onHostStateChange"
            }

            override fun onDisconnected(reason: CloseReason) {
                disconnected.complete(reason)
            }
        })

        val server = createServer(port, hostTeam)
        server.start()
        try {
            adapter.joinHost(
                gameUrl = "ws://localhost:$port/joinGame?id=test",
                coachName = "Ludwig",
                coachType = CoachType.HUMAN,
                gameId = GameId("test"),
                teamIfHost = hostTeam,
                handler = CountingHandler(),
            )
            withTimeout(timeout) { disconnected.await() }
            assertTrue(
                deliveredAfterFailure.isEmpty(),
                "Handlers kept being called after giving up on the connection: $deliveredAfterFailure",
            )
        } finally {
            server.stop()
        }
    }

    @Test
    fun hostCanRejoinANewServerOnTheSamePort(): Unit = runBlocking {
        val port = freePort()
        val hostTeam = createDefaultHomeTeamBB2025(rules)
        val adapter = P2PClientNetworkAdapter()

        suspend fun joinAsHost() = adapter.joinHost(
            gameUrl = "ws://127.0.0.1:$port/joinGame?id=test",
            coachName = "Ludwig",
            coachType = CoachType.HUMAN,
            gameId = GameId("test"),
            teamIfHost = hostTeam,
            handler = CountingHandler(),
        )

        val firstServer = createServer(port, hostTeam)
        firstServer.start()
        joinAsHost()
        withTimeout(timeout) { adapter.homeTeam.first { it != null } }
        firstServer.stop()

        // Exactly what the Host does after rejecting a game: new server, same port, join again.
        val secondServer = createServer(port, hostTeam)
        secondServer.start()
        try {
            joinAsHost()
            // This is what `startServer()` waits for before reporting success.
            withTimeout(timeout) { adapter.homeTeam.first { it != null } }
        } finally {
            secondServer.stop()
        }
    }

    @Test
    fun rejoiningDropsTheHandlerFromThePreviousAttempt(): Unit = runBlocking {
        val port = freePort()
        val hostTeam = createDefaultHomeTeamBB2025(rules)
        val adapter = P2PClientNetworkAdapter()

        val first = CountingHandler()
        val firstServer = createServer(port, hostTeam)
        firstServer.start()
        joinAs(adapter, port, "client", first)
        withTimeout(timeout) { first.firstCoachJoined.await() }
        val countAfterFirstSession = first.coachJoinedCount
        firstServer.stop()

        // The Host rejected the game and started a new server on the same URL.
        val second = CountingHandler()
        val secondServer = createServer(port, hostTeam)
        secondServer.start()
        try {
            joinAs(adapter, port, "client", second)
            withTimeout(timeout) { second.firstCoachJoined.await() }
            // Handlers used to be appended on every join and never removed, so the handler from
            // the abandoned attempt kept running and every side effect fired once per attempt
            // made so far.
            assertEquals(
                countAfterFirstSession,
                first.coachJoinedCount,
                "Handler from the previous join attempt is still receiving messages",
            )
        } finally {
            secondServer.stop()
        }
    }

    @Test
    fun handlersAddedForTheWholeSessionSurviveRejoining(): Unit = runBlocking {
        val port = freePort()
        val hostTeam = createDefaultHomeTeamBB2025(rules)
        val adapter = P2PClientNetworkAdapter()

        // Handlers such as the one forwarding game actions are not tied to a single connection.
        val longLived = CountingHandler()
        adapter.addMessageHandler(longLived)

        val firstServer = createServer(port, hostTeam)
        firstServer.start()
        joinAs(adapter, port, "client", CountingHandler())
        withTimeout(timeout) { longLived.firstCoachJoined.await() }
        val countAfterFirstSession = longLived.coachJoinedCount
        firstServer.stop()

        val secondServer = createServer(port, hostTeam)
        secondServer.start()
        try {
            val reconnected = CountingHandler()
            joinAs(adapter, port, "client", reconnected)
            withTimeout(timeout) { reconnected.firstCoachJoined.await() }
            assertTrue(
                longLived.coachJoinedCount > countAfterFirstSession,
                "Handler registered for the whole session stopped receiving after a rejoin",
            )
        } finally {
            secondServer.stop()
        }
    }
}
