@file:UseContextualSerialization(
    LocalDateTime::class,
)

package dk.ilios.jervis.fumbbl.model

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

@Serializable
data class GameData(
    val gameId: Int,
    val started: LocalDateTime?,
    val teamHomeId: String,
    val teamHomeName: String,
    val teamHomeCoach: String,
    val teamAwayId: String?,
    val teamAwayName: String?,
    val teamAwayCoach: String?,
)

@Serializable
data class GameList(
    val gameListEntries: List<GameData>,
)
