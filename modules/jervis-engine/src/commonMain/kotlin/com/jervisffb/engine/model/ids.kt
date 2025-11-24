package com.jervisffb.engine.model

import com.jervisffb.engine.rules.common.skills.SkillType
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
value class DicePoolId(val value: Int)

/**
 * Unique identifier for a player.
 * This must be unique across all players playing a game.
 */
@JvmInline
@Serializable
value class PlayerId(val value: String)

@Serializable
@JvmInline
value class PositionId(val value: String)

/**
 * Unique identifier for a [RerollSource]. This must be unique across
 * the entire game.
 */
@Serializable
@JvmInline
value class RerollSourceId(val id: String)

/**
 * Unique identifier for a [SkillType] inside a ruleset.
 * Skills with values must have a unique id for each value.
 * The same skill across different players should have the same
 * id
 */
@Serializable
data class SkillId(val type: SkillType, val value: SkillValue = SkillValue.None) {
    fun serialize(): String {
        val valueDescription = when (value) {
            is SkillValue.Int -> "(${value.value})"
            is SkillValue.Keyword -> "(${value.value.description})"
            SkillValue.None -> ""
        }
        return "${type.name}$$valueDescription"
    }
}

/**
 * Some skills have a value associated with them. For example "Loner (4+)" or
 * "Hatred (Troll)". this interface encapsulates that concept.
 * Skills with no value should use [SkillValue.None] or an optional ?
 *
 * See [com.jervisffb.engine.rules.common.skills.Skill] for usage.
 */
@Serializable
sealed interface SkillValue {
    @Serializable
    @JvmInline
    value class Int(val value: kotlin.Int) : SkillValue

    @Serializable
    @JvmInline
    value class Keyword(val value: PlayerKeyword) : SkillValue

    @Serializable
    object None : SkillValue
}

@Serializable
@JvmInline
value class RosterId(val id: String)

@Serializable
@JvmInline
value class TeamId(val value: String = "") {
    fun getTeamReference(state: Game): Team {
        return when {
            state.homeTeam.id == this -> state.homeTeam
            state.awayTeam.id == this -> state.awayTeam
            else -> error("Unknown team: $value")
        }
    }
}

@Serializable
@JvmInline
value class SetupId(val value: String)
