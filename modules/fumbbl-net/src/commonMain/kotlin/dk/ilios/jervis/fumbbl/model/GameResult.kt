package dk.ilios.jervis.fumbbl.model

import kotlinx.serialization.Serializable

@Serializable
data class GameResult(
    val teamResultHome: TeamResult,
    val teamResultAway: TeamResult,
) {
    fun getPlayerResult(player: Player): PlayerResult {
        return teamResultHome.playerResults.firstOrNull {
            it.playerId == player.playerId
        } ?: teamResultAway.playerResults.firstOrNull { it.playerId == player.playerId }
            ?: throw IllegalStateException("Could not find: $player")
    }
}
