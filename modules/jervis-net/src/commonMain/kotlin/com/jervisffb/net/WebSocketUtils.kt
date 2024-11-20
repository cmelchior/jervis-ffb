package com.jervisffb.net

import com.jervisffb.net.messages.ServerMessage
import com.jervisffb.net.serialize.jervisNetworkSerializer
import io.ktor.websocket.CloseReason
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.send
import kotlinx.serialization.encodeToString

suspend fun WebSocketSession.sendMessage(message: ServerMessage) {
    val json = jervisNetworkSerializer.encodeToString(message)
    this.send(json)
}

suspend fun WebSocketSession.close(code: JervisExitCode, error: Throwable) {
    this.close(code, error.stackTraceToString())
}

suspend fun WebSocketSession.close(code: JervisExitCode, message: String) {
    // Control frames in WebSockets (including Close), must be less than 125B.
    // So make sure the message is less than that.
    // See https://datatracker.ietf.org/doc/html/rfc6455#section-5.5
    val truncatedMessage = if (message.length > 122) (message.substring(0, 119) + "...") else message
    this.close(CloseReason(code.code, truncatedMessage))
}

