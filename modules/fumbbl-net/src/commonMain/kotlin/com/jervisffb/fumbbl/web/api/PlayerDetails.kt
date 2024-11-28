package com.jervisffb.fumbbl.web.api

import kotlinx.serialization.Serializable

// Mappings for https://fumbbl.com/api/player/get/<id>

@Serializable
data class PlayerDetails(
    val id: Int,
    val teamId: Int,
    val status: String,
    val number: Int,
    val name: String,
    val position: PositionRef,
    val gender: String,
    val stats: PlayerStats,
    val portrait: String,
    val icon: String,
    val statistics: Statistics,
    val skills: List<String>,
    val injuries: List<String>,
)

@Serializable
data class PositionRef(
    val id: Int,
    val name: String,
)

@Serializable
data class PlayerStats(
    val ma: Int,
    val st: Int,
    val ag: Int,
    val pa: Int,
    val av: Int,
)

@Serializable
data class Statistics(
    val spp: Int,
    val completions: Int,
    val touchdowns: Int,
    val interceptions: Int,
    val casualties: Int,
    val mvp: Int,
    val passing: Int,
    val rushing: Int,
    val blocks: Int,
    val fouls: Int,
    val games: Int,
)
