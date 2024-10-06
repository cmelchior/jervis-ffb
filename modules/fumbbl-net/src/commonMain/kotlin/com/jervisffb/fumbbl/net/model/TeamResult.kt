package com.jervisffb.fumbbl.net.model

import kotlinx.serialization.Serializable

@Serializable
data class TeamResult(
    var score: Int,
    var conceded: Boolean,
    var raisedDead: Int,
    var spectators: Int,
    var fame: Int,
    var winnings: Int,
    var fanFactorModifier: Int,
    var badlyHurtSuffered: Int,
    var seriousInjurySuffered: Int,
    var ripSuffered: Int,
    var spirallingExpenses: Int,
    val playerResults: List<PlayerResult>,
    var pettyCashFromTvDiff: Int,
    var pettyCashTransferred: Int,
    var pettyCashUsed: Int,
    var teamValue: Int,
    var treasuryUsedOnInducements: Int,
    var fanFactor: Int,
    var dedicatedFans: Int,
    var penaltyScore: Int,
)
