package com.jervisffb.engine.model

import kotlinx.serialization.Serializable

@Serializable
data class Coach(val id: CoachId, val name: String)

@Serializable
data class Spectator(val id: SpectatorId, val name: String)
