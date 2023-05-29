package dk.ilios.analyzer.fumbbl.model

import kotlinx.serialization.Serializable

@Serializable
data class GameOption(
    val gameOptionId: String,
    val gameOptionValue: String
)

@Serializable
data class GameOptions(
    val gameOptionArray: List<GameOption>
)