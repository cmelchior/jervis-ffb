package dk.ilios.analyzer.fumbbl.model

import kotlinx.serialization.Serializable

@Serializable
data class TurnData(
    val homeData: Boolean,
    val turnStarted: Boolean,
    val turnNr: Int,
    val firstTurnAfterKickoff: Boolean,
    val reRolls: Int,
    val rerollBrilliantCoachingOneDrive: Int,
    val apothecaries: Int,
    val blitzUsed: Boolean,
    val foulUsed: Boolean,
    val reRollUsed: Boolean,
    val handOverUsed: Boolean,
    val passUsed: Boolean,
    val coachBanned: Boolean,
    val ktmUsed: Boolean,
    val bombUsed: Boolean,
    val leaderState: String,
    val inducementSet: InducementSet,
    val wanderingApothecaries: Int,
    val rerollPumpUpTheCrowdOneDrive: Int,
    val plagueDoctors: Int
)