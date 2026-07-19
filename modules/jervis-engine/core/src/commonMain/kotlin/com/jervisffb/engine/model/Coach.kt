package com.jervisffb.engine.model

import kotlinx.serialization.Serializable

/**
 * Enum defining the type of coach. This can impact how the UI displays certain things.
 */
enum class CoachType {
    HUMAN,
    COMPUTER
}

@Serializable
data class Coach(val id: CoachId, val name: String, val type: CoachType = CoachType.HUMAN) {
    companion object {
        val UNKNOWN = Coach(CoachId("unknown"), "Unknown")
    }
}

@Serializable
data class Spectator(val id: SpectatorId, val name: String)
