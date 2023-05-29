package dk.ilios.analyzer.fumbbl.model

import kotlinx.serialization.Serializable

@Serializable
data class TeamResult(
    val score: Int,
    val conceded: Boolean,
    val raisedDead: Int,
    val spectators: Int,
    val fame: Int,
    val winnings: Int,
    val fanFactorModifier: Int,
    val badlyHurtSuffered: Int,
    val seriousInjurySuffered: Int,
    val ripSuffered: Int,
    val spirallingExpenses: Int,
    val playerResults: List<PlayerResult>,
    val pettyCashFromTvDiff: Int,
    val pettyCashTransferred: Int,
    val pettyCashUsed: Int,
    val teamValue: Int,
    val treasuryUsedOnInducements: Int,
    val fanFactor: Int,
    val dedicatedFans: Int,
    val penaltyScore: Int
)