package dk.ilios.analyzer.fumbbl.model

import kotlinx.serialization.Serializable

@Serializable
data class GameResult(
    val teamResultHome: TeamResult,
    val teamResultAway: TeamResult
)