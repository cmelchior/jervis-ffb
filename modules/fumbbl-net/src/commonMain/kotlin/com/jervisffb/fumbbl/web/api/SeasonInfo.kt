package com.jervisffb.fumbbl.web.api

data class SeasonInfo(
    val currentSeason: Int,
    val gamesPlayedInCurrentSeason: Int,
    val record: com.jervisffb.fumbbl.web.api.RecordXX,
)
