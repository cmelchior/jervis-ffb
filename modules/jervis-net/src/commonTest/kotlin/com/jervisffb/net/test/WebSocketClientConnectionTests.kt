package com.jervisffb.net.test

import com.jervisffb.engine.model.CoachId
import com.jervisffb.engine.rules.StandardBB2020Rules
import com.jervisffb.net.GameId
import com.jervisffb.net.JervisClientWebSocketConnection
import com.jervisffb.net.JervisExitCode
import com.jervisffb.net.LightServer
import com.jervisffb.test.createDefaultHomeTeam
import com.jervisffb.utils.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class WebSocketClientConnectionTests {

    val rules = StandardBB2020Rules()

    @Test
    fun closeMultipleTimes() = runBlocking {
        // Start server
        val server = LightServer(
            gameName = "testGame",
            rules = rules,
            hostCoach = CoachId("HomeCoachID"),
            hostTeam = createDefaultHomeTeam(rules),
            clientCoach = null,
            clientTeam = null,
            testMode = true
        )
        server.start()

        val conn = JervisClientWebSocketConnection(GameId("test"), "ws://localhost:8080/game", "host")
        conn.start()
        try {
            conn.close()
            conn.close()
            assertEquals(JervisExitCode.CLIENT_CLOSING.code, conn.getCloseReason()?.code)
        } finally {
            server.stop()
        }
    }
}
