package com.jervisffb.engine.model

import kotlinx.serialization.Serializable

@Serializable
data class Coach(val id: CoachId, val name: String) {
    companion object {
        val UNKNOWN = Coach(CoachId("unknown"), "Unknown")
    }
}

@Serializable
data class Spectator(val id: SpectatorId, val name: String)
