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
    this.close(code, error.message ?: error::class.simpleName ?: "WebSocket connection failed.")
}

suspend fun WebSocketSession.close(code: JervisExitCode, message: String) {
    this.close(code.toCloseReason(message))
}

/**
 * Creates a [CloseReason] with a UTF-8 truncated message to ensure compliance with RFC 6455
 * control frame payload length limits (at most 123 bytes for reason string).
 */
fun JervisExitCode.toCloseReason(message: String): CloseReason {
    return CloseReason(this.code, message.truncateUtf8(123))
}

/**
 * Truncate a string so that its UTF-8 encoded byte representation does not
 * exceed [maxBytes]. If truncated, [suffix] is appended.
 */
fun String.truncateUtf8(maxBytes: Int = 123, suffix: String = "..."): String {
    if (this.length <= maxBytes && this.utf8Cut(maxBytes) == this.length) return this

    // We need to truncate the string to ensure that there is room for the suffix.
    // If the suffix alone doesn't fit, keep as much of the message as the limit allows instead.
    val targetBytes = maxBytes - suffix.encodeToByteArray().size
    if (targetBytes <= 0) return this.substring(0, this.utf8Cut(maxBytes))
    return this.substring(0, this.utf8Cut(targetBytes)) + suffix
}

/**
 * Returns the highest index `i` for which `substring(0, i)` encodes to at most [maxBytes] UTF-8
 * bytes, i.e. [length] if the whole string fits. Surrogate pairs are always kept or dropped as
 * a unit, so the result never splits one.
 */
private fun String.utf8Cut(maxBytes: Int): Int {
    var bytes = 0
    var i = 0
    while (i < this.length) {
        val char = this[i]
        var charCount = 1
        val byteCount = when {
            char.code < 0x80 -> 1
            char.code < 0x800 -> 2
            char.isHighSurrogate() && i + 1 < this.length && this[i + 1].isLowSurrogate() -> {
                charCount = 2
                4
            }
            // Any other BMP Char, including an unpaired surrogate. The latter is encoded as a
            // replacement character, which is 1-3 bytes depending on the platform, so
            // overestimate rather than risk exceeding [maxBytes].
            else -> 3
        }
        if (bytes + byteCount > maxBytes) return i
        bytes += byteCount
        i += charCount
    }
    return this.length
}
