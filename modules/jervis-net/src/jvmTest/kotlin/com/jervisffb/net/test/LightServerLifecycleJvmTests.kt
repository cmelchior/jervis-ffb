package com.jervisffb.net.test

import com.jervisffb.engine.bb2025.StandardBB2025Rules
import com.jervisffb.engine.model.CoachId
import com.jervisffb.net.LightServer
import com.jervisffb.test.bb2025.createDefaultHomeTeamBB2025
import com.jervisffb.utils.runBlocking
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.net.BindException
import java.net.ServerSocket
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

/**
 * Tests for the [LightServer] start/stop life-cycle. They are JVM-only because
 * that is the only platform with a real embedded server implementation.
 */
class LightServerLifecycleJvmTests {

    private val rules = StandardBB2025Rules()

    // Note: the tests below declare `: Unit` explicitly. `runBlocking` is generic, so without it a
    // test whose last expression isn't `Unit` returns that value instead, and JUnit silently skips
    // the method.

    private fun createServer(port: Int) = LightServer(
        gameName = "testGame",
        rules = rules,
        hostCoach = CoachId("HomeCoachID"),
        hostTeam = createDefaultHomeTeamBB2025(rules),
        clientCoach = null,
        clientTeam = null,
        testMode = true,
        port = port,
    )

    // Find a port that is free right now. There is an inherent race here, but the alternative
    // (a hardcoded port) is worse since it also clashes with the other tests in this module.
    private fun findFreePort(): Int = ServerSocket(0).use { it.localPort }

    @Test
    fun startServerOnFreePortGivesEachServerItsOwnPort(): Unit = runBlocking {
        val first = startServerOnFreePort { port -> createServer(port) }
        val second = startServerOnFreePort { port -> createServer(port) }
        try {
            assertNotEquals(first.port, second.port)
        } finally {
            first.stop()
            second.stop()
        }
    }

    @Test
    fun startServerOnFreePortSkipsPortsThatAreTaken(): Unit = runBlocking {
        ServerSocket(0).use { occupied ->
            var attempts = 0
            val server = startServerOnFreePort { port ->
                attempts++
                // Send the first attempt at a port that is definitely taken, so the helper has to
                // notice the failed bind and move on instead of giving up.
                createServer(if (attempts == 1) occupied.localPort else port)
            }
            try {
                assertEquals(2, attempts)
                assertNotEquals(occupied.localPort, server.port)
            } finally {
                server.stop()
            }
        }
    }

    @Test
    fun startAfterStopReusesThePort(): Unit = runBlocking {
        val port = findFreePort()
        val first = createServer(port)
        first.start()
        first.stop()

        // `stop()` must not return before the port is released, otherwise the host UI cannot go
        // back and forth between "Select Team" and "Wait For Opponent" without crashing.
        val second = createServer(port)
        second.start()
        second.stop()
    }

    @Test
    fun concurrentStopsAllWaitForThePortToBeReleased(): Unit = runBlocking {
        val port = findFreePort()
        val server = createServer(port)
        server.start()

        // The host UI can stop the same server from several coroutines at once, e.g. the game
        // ending while the screen is being disposed. None of them may return early, claiming the
        // server is stopped while another one is still shutting Netty down.
        coroutineScope {
            repeat(4) { launch(Dispatchers.Default) { server.stop() } }
        }

        val restarted = createServer(port)
        restarted.start()
        restarted.stop()
    }

    @Test
    fun startFailsWhenPortIsAlreadyInUse(): Unit = runBlocking {
        val port = findFreePort()
        val running = createServer(port)
        running.start()
        try {
            assertFailsWith<BindException> { createServer(port).start() }
        } finally {
            running.stop()
        }
    }

    @Test
    fun serverCanBeStartedAgainAfterAFailedStart(): Unit = runBlocking {
        val port = findFreePort()
        val blocker = createServer(port)
        blocker.start()

        val server = createServer(port)
        assertFailsWith<BindException> { server.start() }
        // Stopping a server that never started must be a no-op rather than an error, and the
        // failed start must not leave the server in a state where it can't be started again.
        server.stop()
        blocker.stop()

        server.start()
        server.stop()
    }
}
