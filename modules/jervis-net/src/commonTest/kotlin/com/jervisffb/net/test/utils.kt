package com.jervisffb.net.test

import com.jervisffb.net.JervisClientWebSocketConnection

suspend inline fun <reified T> checkServerMessage(connection: JervisClientWebSocketConnection, assertFunc: (T) -> Unit) {
    val serverMessage = connection.receiveOrNull()
    if (serverMessage !is T) {
        throw AssertionError("Expected ${T::class.simpleName}, got $serverMessage. Close reason: ${connection.getCloseReason()}")
    }
    assertFunc(serverMessage)
}

suspend inline fun <reified T> consumeServerMessage(connection: JervisClientWebSocketConnection) {
    val serverMessage = connection.receiveOrNull()
    if (serverMessage !is T) throw AssertionError("Expected ${T::class.simpleName}, got $serverMessage. Close reason: ${connection.getCloseReason()}")
}
