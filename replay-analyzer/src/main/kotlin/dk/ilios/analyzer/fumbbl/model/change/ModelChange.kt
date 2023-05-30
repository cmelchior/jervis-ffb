@file:UseContextualSerialization(
    LocalDateTime::class
)
package dk.ilios.analyzer.fumbbl.model.change

import dk.ilios.analyzer.fumbbl.model.ModelChangeId
import dk.ilios.analyzer.fumbbl.model.Player
import dk.ilios.analyzer.fumbbl.model.PlayerAction
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UseContextualSerialization
import kotlinx.serialization.json.JsonClassDiscriminator
import java.time.LocalDateTime

@Serializable
@JvmInline
value class PlayerId(val id: String)

@Serializable
@JsonClassDiscriminator("modelChangeId")
sealed interface ModelChange {
    val modelChangeId: ModelChangeId
    val modelChangeKey: Any? // Normally String
    val modelChangeValue: Any?
}

@Serializable
@SerialName("gameSetStarted")
class GameSetStartedChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: String?,
    override val modelChangeValue: LocalDateTime // Time of game started
): ModelChange

@Serializable
@SerialName("teamResultSetFanFactor")
class TeamResultSetFanFactorChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: String?, // Which team was modified
    override val modelChangeValue: Int // Fan Factor value
): ModelChange

@Serializable
@SerialName("teamResultSetTeamValue")
class TeamResultSetTeamValueChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: String?, // Which team was modified
    override val modelChangeValue: Int // Team value
): ModelChange

@Serializable
@SerialName("gameSetDialogParameter")
class GameSetDialogParameterChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: String?, // Which team/player was modified
    @Transient
    override val modelChangeValue: Any? = null // Ignore for now
): ModelChange

@Serializable
@SerialName("gameSetHalf")
class GameSetHalfChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: Nothing?, // Not used
    override val modelChangeValue: Int // Which half
): ModelChange

@Serializable
@SerialName("turnDataSetReRolls")
class TurnDataSetReRerollsChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: String, // Which team
    override val modelChangeValue: Int // Set number of rerolls available
): ModelChange

@Serializable
@SerialName("gameSetLastTurnMode")
class GameSetLastTurnModeChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: Nothing?, // Not used
    override val modelChangeValue: String // Mode last used
): ModelChange

@Serializable
@SerialName("gameSetTurnMode")
class GameSetTurnModeChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: Nothing?, // Not used
    override val modelChangeValue: String // What mode are we switching to
): ModelChange

@Serializable
@SerialName("gameSetConcessionPossible")
class GameSetConcessionPossibleChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: Nothing?, // Not used
    override val modelChangeValue: Boolean // Is concession possible or not?
): ModelChange

@Serializable
@SerialName("fieldModelSetPlayerState")
class FieldModelSetPlayerStateChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: PlayerId, // PlayerId
    override val modelChangeValue: Int // Unsure what this value represents? Doesn't look like it is used?
): ModelChange

@Serializable
@SerialName("fieldModelSetPlayerCoordinate")
class FieldModelSetPlayerCoordinateChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: PlayerId, // PlayerId
    override val modelChangeValue: List<Int> // Where to place player?
): ModelChange

@Serializable
@SerialName("gameSetHomePlaying")
class GameSetHomePlayingChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: Nothing?, // Not used
    override val modelChangeValue: Boolean // Unsure exactly what this mean? Probably whether or not Home is starting?
): ModelChange


@Serializable
@SerialName("gameSetSetupOffense")
class GameSetSetupOffenseChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: Nothing?, // Not used
    override val modelChangeValue: Boolean // Unsure exactly what this mean?
): ModelChange

@Serializable
@SerialName("fieldModelAddPlayerMarker")
class FieldModelAddPlayerMarkerChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: Nothing?, // Not used
    @Transient
    override val modelChangeValue: Any? = null // {"playerId":"15437642","homeText":"BWL","awayText":null}
): ModelChange

@Serializable
@SerialName("fieldModelSetBallCoordinate")
class FieldModelSetBallCoordinateChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: Nothing?, // Not used
    override val modelChangeValue: List<Int>? // Ball coordinates
): ModelChange

@Serializable
@SerialName("fieldModelSetBallMoving")
class FieldModelSetBallMovingChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: Nothing?, // Not used
    override val modelChangeValue: Boolean // Unsure what this indicates exactly? Just animations?
): ModelChange

@Serializable
@SerialName("fieldModelSetBallInPlay")
class FieldModelSetBallInPlayChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: Nothing?, // Not used
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("turnDataSetTurnNr")
class TurnDataSetTurnNrChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: String, // Team
    override val modelChangeValue: Int // Turn no.
): ModelChange

@Serializable
@SerialName("turnDataSetFirstTurnAfterKickoff")
class TurnDataSetFirstTurnAfterKickoffChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: String, // Team
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("fieldModelRemoveSkillEnhancements")
class FieldModelRemoveSkillEnhancementsChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: PlayerId, // PlayerId
    override val modelChangeValue: String // Remove "enhancement". Unclear what that is
): ModelChange

@Serializable
@SerialName("actingPlayerSetStrength")
class ActingPlayerSetStrengthChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: Nothing?, // Not used. How do we know who the acting player is?
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("actingPlayerSetPlayerId")
class ActingPlayerSetPlayerIdChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: Nothing?, // Not used
    override val modelChangeValue: String? // PlayerId or `null` (when what?)
): ModelChange

@Serializable
@SerialName("actingPlayerSetOldPlayerState")
class ActingPlayerSetOldPlayerStateChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: Nothing?, // Not used
    override val modelChangeValue: Int // Unclear what this is?
): ModelChange

@Serializable
@SerialName("actingPlayerSetPlayerAction")
class ActingPlayerSetPlayerActionChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: Nothing?, // Not used
    override val modelChangeValue: PlayerAction
): ModelChange

@Serializable
@SerialName("fieldModelAddMoveSquare")
class FieldModelAddMoveSquareChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: Nothing?, // Not used
    @Transient
    override val modelChangeValue: Any? = null // {"coordinate":[14,10],"minimumRollDodge":0,"minimumRollGfi":0}}
): ModelChange

@Serializable
@SerialName("fieldModelRemoveMoveSquare")
class FieldModelRemoveMoveSquareChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: Nothing?, // Not used
    @Transient
    override val modelChangeValue: Any? = null // {"coordinate":[16,10],"minimumRollDodge":0,"minimumRollGfi":0}}
): ModelChange

@Serializable
@SerialName("actingPlayerSetHasMoved")
class ActingPlayerSetHasMovedChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: Nothing?, // Not used
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("turnDataSetTurnStarted")
class TurnDataSetTurnStartedChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: String, // Team
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("actingPlayerSetCurrentMove")
class ActingPlayerSetCurrentMoveChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: Nothing?, // Not used
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("fieldModelAddTrackNumber")
class FieldModelAddTrackNumberChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: Nothing?, // Not used
    @Transient
    override val modelChangeValue: Any? = null // {"number":0,"coordinate":[15,11]}
): ModelChange

@Serializable
@SerialName("actingPlayerSetGoingForIt")
class ActingPlayerSetGoingForIt(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: Nothing?, // Not used
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("fieldModelRemoveTrackNumber")
class FieldModelRemoveTrackNumberChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: Nothing?, // Not used
    @Transient
    override val modelChangeValue: Any? = null // {"number":5,"coordinate":[20,14]}
): ModelChange

@Serializable
@SerialName("fieldModelSetTargetSelectionState")
class FieldModelSetTargetSelectionStateChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: Nothing?, // Not used
    @Transient
    override val modelChangeValue: Any? = null
//    {
//        "playerId": "15367986",
//        "targetSelectionStatus": "SELECTED",
//        "targetSelectionStatusIsCommitted": false,
//        "playerStateOld": 258,
//        "usedSkills": [
//        ]
//    }
): ModelChange

@Serializable
@SerialName("turnDataSetBlitzUsed")
class TurnDataSetBlitzUsedChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: String?, // Not used
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("fieldModelAddDiceDecoration")
class FieldModelAddDiceDecorationChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: Nothing?, // Not used
    @Transient
    override val modelChangeValue: Any? = null // {"coordinate":[10,10],"nrOfDice":2}
): ModelChange

@Serializable
@SerialName("gameSetDefenderId")
class GameSetDefenderIdChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: String?, // PlayerId
    override val modelChangeValue: Nothing?
): ModelChange

@Serializable
@SerialName("actingPlayerSetHasBlocked")
class ActingPlayerSetHasBlockedChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: Nothing?, // Not used
    override val modelChangeValue: Boolean
): ModelChange

// -- 38 events so far

@Serializable
@SerialName("playerResultSetBlocks")
class PlayerResultSetBlocksChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: String, // PlayerId
    override val modelChangeValue: Int // Unclear what this?
): ModelChange

@Serializable
@SerialName("fieldModelRemoveDiceDecoration")
class FieldModelRemoveDiceDecorationChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: Nothing?, // Not used
    @Transient
    override val modelChangeValue: Any? = null // {"coordinate":[10,10],"nrOfDice":2}
): ModelChange

@Serializable
@SerialName("fieldModelAddPushbackSquare")
class FieldModelAddPushbackSquareChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: Nothing?, // Not used
    @Transient
    override val modelChangeValue: Any? = null // {"coordinate":[10,11],"direction":"South","selected":false,"locked":false,"homeChoice":false}
): ModelChange

@Serializable
@SerialName("gameSetWaitingForOpponent")
class GameSetWaitingForOpponentChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: Nothing?, // Not used
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("fieldModelRemovePushbackSquare")
class FieldModelRemovePushbackSquareChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: Nothing?, // Not used
    @Transient
    override val modelChangeValue: Any? = null // {"coordinate":[10,11],"direction":"South","selected":false,"locked":false,"homeChoice":false}
): ModelChange

@Serializable
@SerialName("actingPlayerMarkSkillUsed")
class ActingPlayerMarkSkillUsedChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: Nothing?, // Not used
    override val modelChangeValue: String
): ModelChange

@Serializable
@SerialName("playerResultSetTurnsPlayed")
class PlayerResultSetTurnsPlayedChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: PlayerId, // PlayerId
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("actingPlayerSetStandingUp")
class ActingPlayerSetStandingUpChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: Nothing?, // Not used
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("playerResultSetRushing")
class PlayerResultSetRushingChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: PlayerId, // PlayerId
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("turnDataSetFoulUsed")
class TurnDataSetFoulUsedChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: String, // Team
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("actingPlayerSetDodging")
class ActingPlayerSetDodgingChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: Nothing?, // Not used
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("actingPlayerSetHasFouled")
class ActingPlayerSetHasFouledChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: Nothing?, // Not used
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("playerResultSetFouls")
class PlayerResultSetFoulsChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: PlayerId?, // PlayerId
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("fieldModelRemovePlayer")
class FieldModelRemovePlayerChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: PlayerId?, // PlayerId
    override val modelChangeValue: List<Int>? = listOf()
): ModelChange

@Serializable
@SerialName("playerResultSetSeriousInjury")
class PlayerResultSetSeriousInjuryChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: PlayerId?, // PlayerId
    override val modelChangeValue: String
): ModelChange

@Serializable
@SerialName("playerResultSetSendToBoxReason")
class PlayerResultSetSendToBoxReasonChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: PlayerId?, // PlayerId
    override val modelChangeValue: String
): ModelChange

@Serializable
@SerialName("playerResultSetSendToBoxTurn")
class PlayerResultSetSendToBoxTurnChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: PlayerId?, // PlayerId
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("playerResultSetSendToBoxHalf")
class PlayerResultSetSendToBoxHalfChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: PlayerId?, // PlayerId
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("playerResultSetSendToBoxByPlayerId")
class PlayerResultSetSendToBoxByPlayerId(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: PlayerId?, // PlayerId
    override val modelChangeValue: String // Unsure, another player Id?
): ModelChange

@Serializable
@SerialName("teamResultSetSeriousInjurySuffered")
class TeamResultSetSeriousInjurySufferedChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: String?, // Team
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("playerResultSetCasualties")
class PlayerResultSetCasualtiesChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: PlayerId, // PlayerId
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("fieldModelAddBloodSpot")
class FieldModelAddBloodSpotChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: Nothing?, // Not used
    @Transient
    override val modelChangeValue: Any? = null // {"injury":7,"coordinate":[10,8]}
): ModelChange

@Serializable
@SerialName("teamResultSetBadlyHurtSuffered")
class TeamResultSetBadlyHurtSufferedChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: String, // Team
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("turnDataSetHandOverUsed")
class TurnDataSetHandOverUsedChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: String, // Team
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("gameSetPassCoordinate")
class GameSetPassCoordinateChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: String?, // Not used
    override val modelChangeValue: List<Int>?
): ModelChange

@Serializable
@SerialName("gameSetThrowerId")
class GameSetThrowerIdChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: PlayerId?, // PlayerId
    override val modelChangeValue: Nothing?
): ModelChange

@Serializable
@SerialName("gameSetThrowerAction")
class GameSetThrowerActionChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: Nothing?,
    override val modelChangeValue: PlayerAction?
): ModelChange

@Serializable
@SerialName("actingPlayerSetHasPassed")
class ActingPlayerSetHasPassedChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: Nothing?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("turnDataSetCoachBanned")
class TurnDataSetCoachBannedChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: String,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("turnDataSetPassUsed")
class TurnDataSetPassUsedChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: String,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("fieldModelSetRangeRuler")
class FieldModelSetRangeRuler(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: Nothing?,
    @Transient
    override val modelChangeValue: Any? = null // {"throwerId":"15367992","targetCoordinate":[13,3],"minimumRoll":3,"throwTeamMate":false}
): ModelChange

@Serializable
@SerialName("playerResultSetCompletions")
class PlayerResultSetCompletionsChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: String,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("playerResultSetPassing")
class PlayerResultSetPassing(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: PlayerId, // PlayerId
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("playerResultSetTouchdowns")
class PlayerResultSetTouchdownsChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: PlayerId, // PlayerId
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("teamResultSetScore")
class TeamResultSetScoreChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: String, // PlayerId
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("playerResultSetPlayerAwards")
class PlayerResultSetPlayerAwardsChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: PlayerId,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("teamResultSetWinnings")
class TeamResultSetWinningsChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: String,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("gameSetFinished")
class GameSetFinishedChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: Nothing?,
    override val modelChangeValue: LocalDateTime
): ModelChange

@Serializable
@SerialName("gameSetTimeoutPossible")
class GameSetTimeoutPossibleChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: Nothing?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("inducementSetAddPrayer")
class InducementSetAddPrayerChange(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: String,
    override val modelChangeValue: String
): ModelChange

@Serializable
@SerialName("fieldModelAddPrayer")
class FieldModelAddPrayer(
    override val modelChangeId: ModelChangeId,
    override val modelChangeKey: PlayerId,
    override val modelChangeValue: String // Enum
): ModelChange

@Serializable
@SerialName("inducementSetRemovePrayer")
class InducementSetRemovePrayeChange(
    override val modelChangeId: ModelChangeId = ModelChangeId.INDUCEMENT_SET_REMOVE_INDUCEMENT,
    override val modelChangeKey: String,
    override val modelChangeValue: String
): ModelChange

@Serializable
@SerialName("fieldModelRemovePrayer")
class FieldModelRemovePrayerChange(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_REMOVE_PRAYER,
    override val modelChangeKey: PlayerId,
    override val modelChangeValue: String // Enum
): ModelChange

@Serializable
@SerialName("turnDataSetReRollsBrilliantCoachingOneDrive")
class TurnDataSetReRollsBrilliantCoachingOneDriveChange(
    override val modelChangeId: ModelChangeId = ModelChangeId.TURN_DATA_SET_RE_ROLLS_BRILLIANT_COACHING_ONE_DRIVE,
    override val modelChangeKey: String,
    override val modelChangeValue: Int
): ModelChange
