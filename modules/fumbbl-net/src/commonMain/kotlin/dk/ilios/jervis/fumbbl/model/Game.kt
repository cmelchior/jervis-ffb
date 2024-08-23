@file:UseContextualSerialization(
    LocalDateTime::class,
)

package dk.ilios.jervis.fumbbl.model

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

@Serializable
data class Game(
    var gameId: Long,
    var scheduled: LocalDateTime?,
    var started: LocalDateTime?,
    var finished: LocalDateTime?,
    var homePlaying: Boolean,
    var half: Int,
    var homeFirstOffense: Boolean,
    var setupOffense: Boolean,
    var waitingForOpponent: Boolean,
    var turnTime: Int,
    var gameTime: Int,
    var timeoutPossible: Boolean,
    var timeoutEnforced: Boolean,
    var concessionPossible: Boolean,
    var testing: Boolean,
    var turnMode: TurnMode,
    var lastTurnMode: TurnMode?,
    var defenderId: String?,
    var lastDefenderId: String?,
    var defenderAction: PlayerAction?,
    var passCoordinate: FieldCoordinate?,
    var throwerId: String?,
    var throwerAction: PlayerAction?,
    var teamState: TeamState,
    val teamAway: Team,
    val teamHome: Team,
    val turnDataAway: TurnData,
    val turnDataHome: TurnData,
    val fieldModel: FieldModel,
    val actingPlayer: ActingPlayer,
    val gameResult: GameResult,
    val gameOptions: GameOptions,
    var dialogParameter: DialogOptions?,
    var concededLegally: Boolean,
    var adminMode: Boolean = false,
    var rangeRuler: RangeRuler? = null,
) {
    fun getPlayerById(playerId: String): Player? {
        return teamHome.players.firstOrNull { player ->
            player.playerId == playerId
        } ?: teamAway.players.firstOrNull { player -> player.playerId == playerId }
    }

    public enum class TeamState {
        SKELETON,
        FULL,
    }
}
