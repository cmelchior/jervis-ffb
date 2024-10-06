package com.jervisffb.fumbbl.net.model

import kotlinx.serialization.Serializable

@Serializable
data class TurnData(
    val homeData: Boolean,
    var turnStarted: Boolean,
    var turnNr: Int,
    var firstTurnAfterKickoff: Boolean,
    var reRolls: Int,
    var rerollBrilliantCoachingOneDrive: Int,
    var apothecaries: Int,
    var blitzUsed: Boolean,
    var foulUsed: Boolean,
    var reRollUsed: Boolean,
    var handOverUsed: Boolean,
    var passUsed: Boolean,
    var coachBanned: Boolean,
    var ktmUsed: Boolean,
    var bombUsed: Boolean,
    var leaderState: String,
    val inducementSet: InducementSet,
    var wanderingApothecaries: Int,
    var rerollPumpUpTheCrowdOneDrive: Int,
    var singleUseReRolls: Int = 0,
    var plagueDoctors: Int,
)
