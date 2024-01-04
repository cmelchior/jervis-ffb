@file:UseContextualSerialization(
    LocalDateTime::class
)
package dk.ilios.jervis.fumbbl.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization
import java.time.LocalDateTime

@Serializable
data class Game(
    val gameId: Int,
    val scheduled: LocalDateTime?,
    val started: LocalDateTime?,
    val finished: LocalDateTime?,
    val homePlaying: Boolean?,
    val half: Int,
    val homeFirstOffense: Boolean,
    val setupOffense: Boolean,
    val waitingForOpponent: Boolean,
    val turnTime: Int,
    val gameTime: Int,
    val timeoutPossible: Boolean,
    val timeoutEnforced: Boolean,
    val testing: Boolean,
    val turnMode: TurnMode,
    val lastTurnMode: TurnMode?,
    val defenderId: String?,
    val lastDefenderId: String?,
    val defenderAction: PlayerAction?,
    val passCoordinate: FieldCoordinate?,
    val throwerId: String?,
    val throwerAction: PlayerAction?,
    val teamState: TeamState,
    val teamAway: Team,
    val teamHome: Team,
    val turnDataAway: TurnData,
    val turnDataHome: TurnData,
    val fieldModel: FieldModel,
    val actingPlayer: ActingPlayer,
    val gameResult: GameResult,
    val gameOptions: GameOptions,
    val dialogParameter: DialogParameter,
    val concededLegally: Boolean
) {

    public enum class TeamState {
        SKELETON, FULL
    }
}