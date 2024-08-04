package com.jervis.fumbbl.restapi

data class Record(
    val casualties: Int,
    val completions: Int,
    val deflections: Int,
    val extra_spp: Int,
    val games: Int,
    val interceptions: Int,
    val mvps: Int,
    val seasons: Int,
    val spent_spp: Int,
    val spp: Int,
    val touchdowns: Int,
)
