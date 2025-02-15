package com.jervisffb.net

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * Type safe wrapper representing a unique identifier for a game.
 * Unique in this context, just means unique across all running games
 * on the server.
 */
@Serializable
@JvmInline
value class GameId(val value: String)

val String.gameId
    get() = GameId(this)

/**
 * Type safe wrapper for Sha256 encoded passwords
 */
@Serializable
@JvmInline
value class Password(val value: String)

