package com.jervisffb.net.test

import com.jervisffb.engine.bb2020.StandardBB2020Rules
import com.jervisffb.engine.model.CoachId
import com.jervisffb.net.GameId
import com.jervisffb.net.JervisClientWebSocketConnection
import com.jervisffb.net.JervisExitCode
import com.jervisffb.net.LightServer
import com.jervisffb.test.bb2020.createDefaultHomeTeamBB2020
import com.jervisffb.utils.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class WebSocketClientConnectionTests {

    val rules = StandardBB2020Rules()

    @Test
    fun closeMultipleTimes() = runBlocking {
        val server = startServerOnFreePort { port ->
            LightServer(
                gameName = "testGame",
                rules = rules,
                hostCoach = CoachId("HomeCoachID"),
                hostTeam = createDefaultHomeTeamBB2020(rules),
                clientCoach = null,
                clientTeam = null,
                testMode = true,
                port = port
            )
        }

        val conn = JervisClientWebSocketConnection(GameId("test"), "ws://localhost:${server.port}/game", "host")
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
