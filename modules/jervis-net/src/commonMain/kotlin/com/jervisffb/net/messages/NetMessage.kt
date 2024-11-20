package com.jervisffb.net.messages

import kotlinx.serialization.Serializable

/**
 * General interface describing all messages being sent across websocket
 * connection between server and client.
 */
@Serializable
sealed interface NetMessage
