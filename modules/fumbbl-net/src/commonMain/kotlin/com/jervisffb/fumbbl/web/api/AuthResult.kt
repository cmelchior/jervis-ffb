package com.jervisffb.fumbbl.web.api

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Typed return result from /oauth/token
@Serializable
data class AuthResult(
    @SerialName("access_token") val accessToken: String = "",
    @SerialName("token_type") val tokenType: String = "",
    @SerialName("expires_in") val expiresIn: Int, // Seconds to expiration
    val authReceivedAt: Instant = Clock.System.now(), // Save when auth was received.
)
