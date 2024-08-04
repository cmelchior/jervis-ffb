package dk.ilios.jervis.fumbbl.model

import kotlinx.serialization.Serializable

@Serializable
data class PlayerResult(
    val playerId: String,
    var completions: Int,
    var completionsWithAdditionalSpp: Int,
    var touchdowns: Int,
    var interceptions: Int,
    var casualties: Int,
    var casualtiesWithAdditionalSpp: Int,
    var playerAwards: Int,
    var blocks: Int,
    var fouls: Int,
    var rushing: Int,
    var passing: Int,
    var currentSpps: Int,
    var seriousInjury: String?,
    var seriousInjuryDecay: String?,
    var sendToBoxReason: String?,
    var sendToBoxTurn: Int,
    var sendToBoxHalf: Int,
    var sendToBoxByPlayerId: String?,
    var turnsPlayed: Int,
    var hasUsedSecretWeapon: Boolean,
    var defecting: Boolean,
    var deflections: Int = 0,
)
