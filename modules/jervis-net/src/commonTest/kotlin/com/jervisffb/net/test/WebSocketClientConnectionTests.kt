package com.jervisffb.net.test

import com.jervisffb.engine.utils.createDefaultHomeTeam
import com.jervisffb.net.GameId
import com.jervisffb.net.JervisExitCode
import com.jervisffb.net.LightServer
import com.jervisffb.net.WebSocketClientConnection
import com.jervisffb.utils.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class WebSocketClientConnectionTests {

    @Test
    fun closeMultipleTimes() = runBlocking {
        // Start server
        val server = LightServer(createDefaultHomeTeam(), "test", testMode = true)
        server.start()

        val conn = WebSocketClientConnection(GameId("test"), "ws://localhost:8080/game")
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
