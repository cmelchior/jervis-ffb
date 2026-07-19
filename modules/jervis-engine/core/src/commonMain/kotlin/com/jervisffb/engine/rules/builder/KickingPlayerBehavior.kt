package com.jervisffb.engine.rules.builder

/**
 * This enum describes how the kicking player is selected during Kick-off.
 */
enum class KickingPlayerBehavior {
    STRICT, // Player should be selected by the Client
    FUMBBL, // Player is selected automatically by the server.
}
