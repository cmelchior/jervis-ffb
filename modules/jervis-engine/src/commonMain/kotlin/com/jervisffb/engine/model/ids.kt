package com.jervisffb.engine.model

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

// This file contains all ID's used elsewhere in the model
// All Ids are type-safe integers or strings, preventing users
// from accidentally mixing or modifying them.

@Serializable
@JvmInline
value class CoachId(val value: String)

@Serializable
@JvmInline
value class SpectatorId(val value: String)

// ID that identities a single die
// Used to track individual dice through rerolls
@JvmInline
@Serializable
value class DieId(val id: String) {
    companion object {
        fun generate(state: Game): DieId = state.idGenerator.nextDiceId()
    }
}

@JvmInline
@Serializable
value class PlayerId(val value: String)

@Serializable
@JvmInline
value class PositionId(val id: String)

@Serializable
@JvmInline
value class RerollSourceId(val id: String)

@Serializable
@JvmInline
value class RosterId(val id: String)

@Serializable
@JvmInline
value class TeamId(val value: String = "")
