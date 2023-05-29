package dk.ilios.analyzer.fumbbl.model

import kotlinx.serialization.Serializable

@Serializable
data class PlayerResult(
    val playerId: String,
    val completions: Int,
    val completionsWithAdditionalSpp: Int,
    val touchdowns: Int,
    val interceptions: Int,
    val casualties: Int,
    val casualtiesWithAdditionalSpp: Int,
    val playerAwards: Int,
    val blocks: Int,
    val fouls: Int,
    val rushing: Int,
    val passing: Int,
    val currentSpps: Int,
    val seriousInjury: String?,
    val seriousInjuryDecay: String?,
    val sendToBoxReason: String?,
    val sendToBoxTurn: Int,
    val sendToBoxHalf: Int,
    val sendToBoxByPlayerId: String?,
    val turnsPlayed: Int,
    val hasUsedSecretWeapon: Boolean,
    val defecting: Boolean
)