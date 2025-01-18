package com.jervisffb.fumbbl.web.api

import kotlinx.serialization.Serializable

// Typed return result form /match/current
@Serializable
data class CurrentMatchResult(
    val id: Long,
    val half: Int,
    val turn: Int,
    val division: String,
    val tournament: Tournament,
    val teams: List<Team>
) {
    val homeTeam = teams.first { it.side == "home"}
    val awayTeam = teams.first { it.side == "away"}
}

// Typed return result form /match/current
@Serializable
data class Tournament(
    val id: Int,
    val group: Int
)

// Typed return result form /match/current
@Serializable
data class Team(
    val id: Long,
    val side: String,
    val name: String,
    val coach: String,
    val race: String,
    val rating: String,
    val score: Int,
    val logo: String
)
