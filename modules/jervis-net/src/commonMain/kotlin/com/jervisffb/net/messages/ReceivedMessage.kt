package com.jervisffb.net.messages

import com.jervisffb.net.JervisNetworkWebSocketConnection

/**
 * This class should wrap all messages from clients before further
 * processing. This allows the rest of the server to track the origin.
 */
data class ReceivedMessage(
    val connection: JervisNetworkWebSocketConnection?,
    val message: ClientMessage,
)
