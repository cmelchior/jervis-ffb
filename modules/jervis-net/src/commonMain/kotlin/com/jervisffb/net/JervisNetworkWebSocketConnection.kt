package com.jervisffb.net

import io.ktor.websocket.DefaultWebSocketSession

interface JervisWebSocketConnection: DefaultWebSocketSession {
    val username: String
    suspend fun send(message: String)
    suspend fun receive(): String?

}


/**
 * Wrapper for a websocket connection between a Jervis client and server.
 * It mostly exists so we can track the username owning the connection.
 */
open class JervisNetworkWebSocketConnection(val username: String, connection: DefaultWebSocketSession)
: DefaultWebSocketSession by connection


/**
 * This class fakes a websocket connection. This allows a thread inside the same process as the
 * server to fake being an external client.
 *
 * This is used by the P2P Host connection and testing.
 */
//class JervisInProcessWebsocketConnection(override val username: String): JervisWebSocketConnection {
//    override var pingIntervalMillis: Long
//        get() = TODO("Not yet implemented")
//        set(value) {}
//    override var timeoutMillis: Long
//        get() = TODO("Not yet implemented")
//        set(value) {}
//    override val closeReason: Deferred<CloseReason?>
//        get() = TODO("Not yet implemented")
//
//    @InternalAPI
//    override fun start(negotiatedExtensions: List<WebSocketExtension<*>>) {
//        TODO("Not yet implemented")
//    }
//
//    override var masking: Boolean
//        get() = TODO("Not yet implemented")
//        set(value) {}
//    override var maxFrameSize: Long = Long.MAX_VALUE
//    override val incoming: ReceiveChannel<Frame>
//        get() = TODO("Not yet implemented")
//    override val outgoing: SendChannel<Frame>
//        get() = TODO("Not yet implemented")
//    override val extensions: List<WebSocketExtension<*>>
//        get() = TODO("Not yet implemented")
//
//    override suspend fun flush() { /* Do nothing */ }
//    override fun terminate() { /* Do nothing */ }
//    override val coroutineContext: CoroutineContext
//        get() = TODO("Not yet implemented")
//}
