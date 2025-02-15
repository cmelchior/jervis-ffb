package com.jervisffb.net.test

import com.jervisffb.net.GameId
import com.jervisffb.net.JervisClientWebSocketConnection
import com.jervisffb.net.JervisExitCode
import com.jervisffb.net.LightServer
import com.jervisffb.net.test.utils.createDefaultHomeTeam
import com.jervisffb.utils.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class WebSocketClientConnectionTests {

    @Test
    fun closeMultipleTimes() = runBlocking {
        // Start server
        val server = LightServer(createDefaultHomeTeam(), "test", testMode = true)
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
