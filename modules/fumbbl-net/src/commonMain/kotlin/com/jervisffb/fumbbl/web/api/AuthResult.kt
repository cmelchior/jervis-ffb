@file:OptIn(ExperimentalTime::class)

package com.jervisffb.fumbbl.web.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

// Typed return result from /oauth/token
@Serializable
data class AuthResult(
    @SerialName("access_token") val accessToken: String = "",
    @SerialName("token_type") val tokenType: String = "",
    @SerialName("expires_in") val expiresIn: Int, // Seconds to expiration
    val authReceivedAt: Instant = Clock.System.now(), // Save when auth was received.
)
