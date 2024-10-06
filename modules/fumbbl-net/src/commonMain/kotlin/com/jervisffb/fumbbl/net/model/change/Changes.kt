@file:UseContextualSerialization(LocalDateTime::class)

package com.jervisffb.fumbbl.net.model.change

import com.jervisffb.fumbbl.net.model.BloodSpot
import com.jervisffb.fumbbl.net.model.DialogOptions
import com.jervisffb.fumbbl.net.model.DiceDecoration
import com.jervisffb.fumbbl.net.model.FieldCoordinate
import com.jervisffb.fumbbl.net.model.FieldMarker
import com.jervisffb.fumbbl.net.model.Inducement
import com.jervisffb.fumbbl.net.model.ModelChangeId
import com.jervisffb.fumbbl.net.model.PlayerAction
import com.jervisffb.fumbbl.net.model.PlayerMarker
import com.jervisffb.fumbbl.net.model.PlayerState
import com.jervisffb.fumbbl.net.model.RangeRuler
import com.jervisffb.fumbbl.net.model.TrackNumber
import com.jervisffb.fumbbl.net.model.TrapDoor
import com.jervisffb.fumbbl.net.model.TurnMode
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

@Serializable
@SerialName("actingPlayerMarkSkillUsed")
data class ActingPlayerMarkSkillUsed(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.ACTING_PLAYER_MARK_SKILL_USED,
    @SerialName("modelChangeKey")
    override val key: Nothing?,
    @SerialName("modelChangeValue")
    override val value: String,
) : ModelChange

@Serializable
@SerialName("actingPlayerMarkSkillUnused")
data class ActingPlayerMarkSkillUnused(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.ACTING_PLAYER_MARK_SKILL_UNUSED,
    @SerialName("modelChangeKey")
    override val key: Nothing?,
    @SerialName("modelChangeValue")
    override val value: String,
) : ModelChange

@Serializable
@SerialName("actingPlayerSetCurrentMove")
data class ActingPlayerSetCurrentMove(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.ACTING_PLAYER_SET_CURRENT_MOVE,
    @SerialName("modelChangeKey")
    override val key: Nothing?,
    @SerialName("modelChangeValue")
    override val value: Int,
) : ModelChange

@Serializable
@SerialName("actingPlayerSetDodging")
data class ActingPlayerSetDodging(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.ACTING_PLAYER_SET_DODGING,
    @SerialName("modelChangeKey")
    override val key: Nothing?,
    @SerialName("modelChangeValue")
    override val value: Boolean,
) : ModelChange

@Serializable
@SerialName("actingPlayerSetGoingForIt")
data class ActingPlayerSetGoingForIt(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.ACTING_PLAYER_SET_GOING_FOR_IT,
    @SerialName("modelChangeKey")
    override val key: Nothing?,
    @SerialName("modelChangeValue")
    override val value: Boolean,
) : ModelChange

@Serializable
@SerialName("actingPlayerSetHasBlocked")
data class ActingPlayerSetHasBlocked(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.ACTING_PLAYER_SET_HAS_BLOCKED,
    @SerialName("modelChangeKey")
    override val key: Nothing?,
    @SerialName("modelChangeValue")
    override val value: Boolean,
) : ModelChange

@Serializable
@SerialName("actingPlayerSetHasFed")
data class ActingPlayerSetHasFed(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.ACTING_PLAYER_SET_HAS_FED,
    @SerialName("modelChangeKey")
    override val key: Nothing?,
    @SerialName("modelChangeValue")
    override val value: Boolean,
) : ModelChange

@Serializable
@SerialName("actingPlayerSetHasFouled")
data class ActingPlayerSetHasFouled(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.ACTING_PLAYER_SET_HAS_FOULED,
    @SerialName("modelChangeKey")
    override val key: Nothing?,
    @SerialName("modelChangeValue")
    override val value: Boolean,
) : ModelChange

@Serializable
@SerialName("actingPlayerSetHasJumped")
data class ActingPlayerSetHasJumped(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.ACTING_PLAYER_SET_HAS_JUMPED,
    @SerialName("modelChangeKey")
    override val key: Nothing?,
    @SerialName("modelChangeValue")
    override val value: Boolean,
) : ModelChange

@Serializable
@SerialName("actingPlayerSetHasMoved")
data class ActingPlayerSetHasMoved(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.ACTING_PLAYER_SET_HAS_MOVED,
    @SerialName("modelChangeKey")
    override val key: Nothing?,
    @SerialName("modelChangeValue")
    override val value: Boolean,
) : ModelChange

@Serializable
@SerialName("actingPlayerSetHasPassed")
data class ActingPlayerSetHasPassed(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.ACTING_PLAYER_SET_HAS_PASSED,
    @SerialName("modelChangeKey")
    override val key: Nothing?,
    @SerialName("modelChangeValue")
    override val value: Boolean,
) : ModelChange

@Serializable
@SerialName("actingPlayerSetLeaping")
data class ActingPlayerSetLeaping(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.ACTING_PLAYER_SET_JUMPING,
    @SerialName("modelChangeKey")
    override val key: Nothing?,
    @SerialName("modelChangeValue")
    override val value: Boolean,
) : ModelChange

@Serializable
@SerialName("actingPlayerSetOldPlayerState")
data class ActingPlayerSetOldPlayerState(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.ACTING_PLAYER_SET_OLD_PLAYER_STATE,
    @SerialName("modelChangeKey")
    override val key: Nothing?,
    @SerialName("modelChangeValue")
    override val value: PlayerState,
) : ModelChange

@Serializable
@SerialName("actingPlayerSetPlayerAction")
data class ActingPlayerSetPlayerAction(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.ACTING_PLAYER_SET_PLAYER_ACTION,
    @SerialName("modelChangeKey")
    override val key: Nothing?,
    @SerialName("modelChangeValue")
    override val value: PlayerAction,
) : ModelChange

@Serializable
@SerialName("actingPlayerSetPlayerId")
data class ActingPlayerSetPlayerId(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.ACTING_PLAYER_SET_PLAYER_ID,
    @SerialName("modelChangeKey")
    override val key: Nothing?,
    @SerialName("modelChangeValue")
    override val value: PlayerId?,
) : ModelChange

@Serializable
@SerialName("actingPlayerSetStandingUp")
data class ActingPlayerSetStandingUp(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.ACTING_PLAYER_SET_STANDING_UP,
    @SerialName("modelChangeKey")
    override val key: Nothing?,
    @SerialName("modelChangeValue")
    override val value: Boolean,
) : ModelChange

@Serializable
@SerialName("actingPlayerSetStrength")
data class ActingPlayerSetStrength(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.ACTING_PLAYER_SET_STRENGTH,
    @SerialName("modelChangeKey")
    override val key: Nothing?,
    @SerialName("modelChangeValue")
    override val value: Int,
) : ModelChange

@Serializable
@SerialName("actingPlayerSetSufferingAnimosity")
data class ActingPlayerSetSufferingAnimosity(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.ACTING_PLAYER_SET_SUFFERING_ANIMOSITY,
    @SerialName("modelChangeKey")
    override val key: Nothing?,
    @SerialName("modelChangeValue")
    override val value: Boolean,
) : ModelChange

@Serializable
@SerialName("actingPlayerSetSufferingBloodLust")
data class ActingPlayerSetSufferingBloodLust(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.ACTING_PLAYER_SET_SUFFERING_BLOOD_LUST,
    @SerialName("modelChangeKey")
    override val key: Nothing?,
    @SerialName("modelChangeValue")
    override val value: Boolean,
) : ModelChange

@Serializable
@SerialName("fieldModelAddBloodSpot")
data class FieldModelAddBloodSpot(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.FIELD_MODEL_ADD_BLOOD_SPOT,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: BloodSpot,
) : ModelChange

@Serializable
@SerialName("fieldModelAddCard")
data class FieldModelAddCard(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.FIELD_MODEL_ADD_CARD,
    @SerialName("modelChangeKey")
    override val key: String,
    @SerialName("modelChangeValue")
    override val value: String,
) : ModelChange

@Serializable
@SerialName("fieldModelAddCardEffect")
data class FieldModelAddCardEffect(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.FIELD_MODEL_ADD_CARD_EFFECT,
    @SerialName("modelChangeKey")
    override val key: String,
    @SerialName("modelChangeValue")
    override val value: String,
) : ModelChange

@Serializable
@SerialName("fieldModelAddDiceDecoration")
data class FieldModelAddDiceDecoration(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.FIELD_MODEL_ADD_DICE_DECORATION,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: DiceDecoration,
) : ModelChange

@Serializable
@SerialName("fieldModelAddIntensiveTraining")
data class FieldModelAddIntensiveTraining(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.FIELD_MODEL_ADD_INTENSIVE_TRAINING,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: String?,
) : ModelChange

@Serializable
@SerialName("fieldModelAddFieldMarker")
data class FieldModelAddFieldMarker(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.FIELD_MODEL_ADD_FIELD_MARKER,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: String?,
) : ModelChange

// This looks to only indicate a possible target square
@Serializable
@SerialName("fieldModelAddMoveSquare")
data class FieldModelAddMoveSquare(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.FIELD_MODEL_ADD_MOVE_SQUARE,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: MoveSquare,
) : ModelChange

@Serializable
@SerialName("fieldModelAddPlayerMarker")
data class FieldModelAddPlayerMarker(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.FIELD_MODEL_ADD_PLAYER_MARKER,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: PlayerMarker,
) : ModelChange

@Serializable
@SerialName("fieldModelAddPrayer")
data class FieldModelAddPrayer(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.FIELD_MODEL_ADD_PRAYER,
    @SerialName("modelChangeKey")
    override val key: String,
    @SerialName("modelChangeValue")
    override val value: String,
) : ModelChange

@Serializable
@SerialName("fieldModelAddSkillEnhancements")
data class FieldModelAddSkillEnhancements(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.FIELD_MODEL_ADD_SKILL_ENHANCEMENTS,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: String?,
) : ModelChange

@Serializable
@SerialName("fieldModelAddPushbackSquare")
data class FieldModelAddPushbackSquare(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.FIELD_MODEL_ADD_PUSHBACK_SQUARE,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: PushBackSquare,
) : ModelChange

@Serializable
@SerialName("fieldModelAddTrackNumber")
data class FieldModelAddTrackNumber(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.FIELD_MODEL_ADD_TRACK_NUMBER,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: TrackNumber,
) : ModelChange

@Serializable
@SerialName("fieldModelAddTrapDoor")
data class FieldModelAddTrapDoor(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.FIELD_MODEL_ADD_TRAP_DOOR,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: TrapDoor,
) : ModelChange

@Serializable
@SerialName("fieldModelAddWisdom")
data class FieldModelAddWisdom(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.FIELD_MODEL_ADD_WISDOM,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: String?,
) : ModelChange

@Serializable
@SerialName("fieldModelKeepDeactivatedCard")
data class FieldModelKeepDeactivatedCard(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.FIELD_MODEL_KEEP_DEACTIVATED_CARD,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: String?,
) : ModelChange

@Serializable
@SerialName("fieldModelRemoveCard")
data class FieldModelRemoveCard(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.FIELD_MODEL_REMOVE_CARD,
    @SerialName("modelChangeKey")
    override val key: String,
    @SerialName("modelChangeValue")
    override val value: String,
) : ModelChange

@Serializable
@SerialName("fieldModelRemoveCardEffect")
data class FieldModelRemoveCardEffect(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.FIELD_MODEL_REMOVE_CARD_EFFECT,
    @SerialName("modelChangeKey")
    override val key: String,
    @SerialName("modelChangeValue")
    override val value: String,
) : ModelChange

@Serializable
@SerialName("fieldModelRemoveDiceDecoration")
data class FieldModelRemoveDiceDecoration(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.FIELD_MODEL_REMOVE_DICE_DECORATION,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: DiceDecoration,
) : ModelChange

@Serializable
@SerialName("fieldModelRemoveFieldMarker")
data class FieldModelRemoveFieldMarker(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.FIELD_MODEL_REMOVE_FIELD_MARKER,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: FieldMarker,
) : ModelChange

@Serializable
@SerialName("fieldModelRemoveMoveSquare")
data class FieldModelRemoveMoveSquare(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.FIELD_MODEL_REMOVE_MOVE_SQUARE,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: MoveSquare,
) : ModelChange

@Serializable
@SerialName("fieldModelRemovePlayer")
data class FieldModelRemovePlayer(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.FIELD_MODEL_REMOVE_PLAYER,
    @SerialName("modelChangeKey")
    override val key: PlayerId,
    @SerialName("modelChangeValue")
    override val value: FieldCoordinate?,
) : ModelChange

@Serializable
@SerialName("fieldModelRemovePlayerMarker")
data class FieldModelRemovePlayerMarker(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.FIELD_MODEL_REMOVE_PLAYER_MARKER,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: PlayerMarker,
) : ModelChange

@Serializable
@SerialName("fieldModelRemovePrayer")
data class FieldModelRemovePrayer(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.FIELD_MODEL_REMOVE_PRAYER,
    @SerialName("modelChangeKey")
    override val key: String,
    @SerialName("modelChangeValue")
    override val value: String,
) : ModelChange

@Serializable
@SerialName("fieldModelRemoveSkillEnhancements")
data class FieldModelRemoveSkillEnhancements(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.FIELD_MODEL_REMOVE_SKILL_ENHANCEMENTS,
    @SerialName("modelChangeKey")
    override val key: String,
    @SerialName("modelChangeValue")
    override val value: String?,
) : ModelChange

@Serializable
@SerialName("fieldModelRemovePushbackSquare")
data class FieldModelRemovePushbackSquare(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.FIELD_MODEL_REMOVE_PUSHBACK_SQUARE,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: PushBackSquare,
) : ModelChange

@Serializable
@SerialName("fieldModelRemoveTrackNumber")
data class FieldModelRemoveTrackNumber(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.FIELD_MODEL_REMOVE_TRACK_NUMBER,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: TrackNumber,
) : ModelChange

@Serializable
@SerialName("fieldModelRemoveTrapDoor")
data class FieldModelRemoveTrapDoor(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.FIELD_MODEL_REMOVE_TRAP_DOOR,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: TrapDoor,
) : ModelChange

@Serializable
@SerialName("fieldModelSetBallCoordinate")
data class FieldModelSetBallCoordinate(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.FIELD_MODEL_SET_BALL_COORDINATE,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: FieldCoordinate?,
) : ModelChange

@Serializable
@SerialName("fieldModelSetBallInPlay")
data class FieldModelSetBallInPlay(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.FIELD_MODEL_SET_BALL_IN_PLAY,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: Boolean,
) : ModelChange

@Serializable
@SerialName("fieldModelSetBallMoving")
data class FieldModelSetBallMoving(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.FIELD_MODEL_SET_BALL_MOVING,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: Boolean,
) : ModelChange

@Serializable
@SerialName("fieldModelSetBlitzState")
data class FieldModelSetBlitzState(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.FIELD_MODEL_SET_BLITZ_STATE,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: TargetSelectionState?,
) : ModelChange

@Serializable
@SerialName("fieldModelSetBombCoordinate")
data class FieldModelSetBombCoordinate(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.FIELD_MODEL_SET_BOMB_COORDINATE,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: FieldCoordinate?,
) : ModelChange

@Serializable
@SerialName("fieldModelSetBombMoving")
data class FieldModelSetBombMoving(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.FIELD_MODEL_SET_BOMB_MOVING,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: Boolean,
) : ModelChange

@Serializable
@SerialName("fieldModelSetPlayerCoordinate")
data class FieldModelSetPlayerCoordinate(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.FIELD_MODEL_SET_PLAYER_COORDINATE,
    @SerialName("modelChangeKey")
    override val key: String,
    @SerialName("modelChangeValue")
    override val value: FieldCoordinate?,
) : ModelChange

@Serializable
@SerialName("fieldModelSetPlayerState")
data class FieldModelSetPlayerState(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.FIELD_MODEL_SET_PLAYER_STATE,
    @SerialName("modelChangeKey")
    override val key: String,
    @SerialName("modelChangeValue")
    override val value: PlayerState,
) : ModelChange

@Serializable
@SerialName("fieldModelSetRangeRuler")
data class FieldModelSetRangeRuler(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.FIELD_MODEL_SET_RANGE_RULER,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: RangeRuler?,
) : ModelChange

@Serializable
@SerialName("fieldModelSetTargetSelectionState")
data class FieldModelSetTargetSelectionState(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.FIELD_MODEL_SET_TARGET_SELECTION_STATE,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: TargetSelectionState?,
) : ModelChange

@Serializable
@SerialName("fieldModelSetWeather")
data class FieldModelSetWeather(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.FIELD_MODEL_SET_WEATHER,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: String,
) : ModelChange

@Serializable
@SerialName("gameSetAdminMode")
data class GameSetAdminMode(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.GAME_SET_ADMIN_MODE,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: Boolean,
) : ModelChange

@Serializable
@SerialName("gameSetConcededLegally")
data class GameSetConcededLegally(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.GAME_SET_CONCEDED_LEGALLY,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: Boolean,
) : ModelChange

@Serializable
@SerialName("gameSetConcessionPossible")
data class GameSetConcessionPossible(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.GAME_SET_CONCESSION_POSSIBLE,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: Boolean,
) : ModelChange

@Serializable
@SerialName("gameSetDefenderAction")
data class GameSetDefenderAction(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.GAME_SET_DEFENDER_ACTION,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: PlayerAction?,
) : ModelChange

@Serializable
@SerialName("gameSetDefenderId")
data class GameSetDefenderId(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.GAME_SET_DEFENDER_ID,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: String?,
) : ModelChange

@Serializable
@SerialName("gameSetDialogParameter")
data class GameSetDialogParameter(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.GAME_SET_DIALOG_PARAMETER,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: DialogOptions?,
) : ModelChange

@Serializable
@SerialName("gameSetFinished")
data class GameSetFinished(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.GAME_SET_FINISHED,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: LocalDateTime?,
) : ModelChange

@Serializable
@SerialName("gameSetHalf")
data class GameSetHalf(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.GAME_SET_HALF,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: Int,
) : ModelChange

@Serializable
@SerialName("gameSetHomeFirstOffense")
data class GameSetHomeFirstOffense(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.GAME_SET_HOME_FIRST_OFFENSE,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: Boolean,
) : ModelChange

@Serializable
@SerialName("gameSetHomePlaying")
data class GameSetHomePlaying(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.GAME_SET_HOME_PLAYING,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: Boolean,
) : ModelChange

@Serializable
@SerialName("gameSetId")
data class GameSetId(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.GAME_SET_ID,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: Long,
) : ModelChange

@Serializable
@SerialName("gameSetLastDefenderId")
data class GameSetLastDefenderId(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.GAME_SET_LAST_DEFENDER_ID,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: String?,
) : ModelChange

@Serializable
@SerialName("gameSetLastTurnMode")
data class GameSetLastTurnMode(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.GAME_SET_LAST_TURN_MODE,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: TurnMode?,
) : ModelChange

@Serializable
@SerialName("gameSetPassCoordinate")
data class GameSetPassCoordinate(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.GAME_SET_PASS_COORDINATE,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: FieldCoordinate?,
) : ModelChange

@Serializable
@SerialName("gameSetScheduled")
data class GameSetScheduled(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.GAME_SET_SCHEDULED,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: LocalDateTime?,
) : ModelChange

@Serializable
@SerialName("gameSetSetupOffense")
data class  GameSetSetupOffense(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.GAME_SET_SETUP_OFFENSE,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: Boolean,
) : ModelChange

@Serializable
@SerialName("gameSetStarted")
data class GameSetStarted(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.GAME_SET_STARTED,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: LocalDateTime?,
) : ModelChange

@Serializable
@SerialName("gameSetTesting")
data class GameSetTesting(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.GAME_SET_TESTING,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: Boolean,
) : ModelChange

@Serializable
@SerialName("gameSetThrowerId")
data class GameSetThrowerId(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.GAME_SET_THROWER_ID,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: String?,
) : ModelChange

@Serializable
@SerialName("gameSetThrowerAction")
data class GameSetThrowerAction(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.GAME_SET_THROWER_ACTION,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: PlayerAction?,
) : ModelChange

@Serializable
@SerialName("gameSetTimeoutEnforced")
data class GameSetTimeoutEnforced(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.GAME_SET_TIMEOUT_ENFORCED,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: Boolean,
) : ModelChange

@Serializable
@SerialName("gameSetTimeoutPossible")
data class GameSetTimeoutPossible(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.GAME_SET_TIMEOUT_POSSIBLE,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: Boolean,
) : ModelChange

@Serializable
@SerialName("gameSetTurnMode")
data class GameSetTurnMode(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.GAME_SET_TURN_MODE,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: TurnMode,
) : ModelChange

@Serializable
@SerialName("gameSetWaitingForOpponent")
data class GameSetWaitingForOpponent(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.GAME_SET_WAITING_FOR_OPPONENT,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: Boolean,
) : ModelChange

@Serializable
@SerialName("gameOptionsAddOption")
data class GameOptionsAddOption(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.GAME_OPTIONS_ADD_OPTION,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: String?,
) : ModelChange

@Serializable
@SerialName("inducementSetActivateCard")
data class InducementSetActivateCard(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.INDUCEMENT_SET_ACTIVATE_CARD,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: String?,
) : ModelChange

@Serializable
@SerialName("inducementSetAddAvailableCard")
data class InducementSetAddAvailableCard(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.INDUCEMENT_SET_ADD_AVAILABLE_CARD,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: String?,
) : ModelChange

@Serializable
@SerialName("inducementSetAddInducement")
data class InducementSetAddInducement(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.INDUCEMENT_SET_ADD_INDUCEMENT,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: Inducement,
) : ModelChange

@Serializable
@SerialName("inducementSetCardChoices")
data class InducementSetCardChoices(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.INDUCEMENT_SET_CARD_CHOICES,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: String?,
) : ModelChange

@Serializable
@SerialName("inducementSetDeactivateCard")
data class InducementSetDeactivateCard(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.INDUCEMENT_SET_DEACTIVATE_CARD,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: String?,
) : ModelChange

@Serializable
@SerialName("inducementSetAddPrayer")
data class InducementSetAddPrayer(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.INDUCEMENT_SET_ADD_PRAYER,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: String,
) : ModelChange

@Serializable
@SerialName("inducementSetRemoveAvailableCard")
data class InducementSetRemoveAvailableCard(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.INDUCEMENT_SET_REMOVE_AVAILABLE_CARD,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: String?,
) : ModelChange

@Serializable
@SerialName("inducementSetRemoveInducement")
data class InducementSetRemoveInducement(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.INDUCEMENT_SET_REMOVE_INDUCEMENT,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: Inducement,
) : ModelChange

@Serializable
@SerialName("inducementSetRemovePrayer")
data class InducementSetRemovePrayer(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.INDUCEMENT_SET_REMOVE_PRAYER,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: String?,
) : ModelChange

@Serializable
@SerialName("playerMarkSkillUsed")
data class PlayerMarkSkillUsed(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.PLAYER_MARK_SKILL_USED,
    @SerialName("modelChangeKey")
    override val key: String,
    @SerialName("modelChangeValue")
    override val value: String,
) : ModelChange

@Serializable
@SerialName("playerMarkSkillUnused")
data class PlayerMarkSkillUnused(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.PLAYER_MARK_SKILL_UNUSED,
    @SerialName("modelChangeKey")
    override val key: String,
    @SerialName("modelChangeValue")
    override val value: String,
) : ModelChange

@Serializable
@SerialName("playerResultSetBlocks")
data class PlayerResultSetBlocks(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_BLOCKS,
    @SerialName("modelChangeKey")
    override val key: String,
    @SerialName("modelChangeValue")
    override val value: Int,
) : ModelChange

@Serializable
@SerialName("playerResultSetCasualties")
data class PlayerResultSetCasualties(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_CASUALTIES,
    @SerialName("modelChangeKey")
    override val key: String,
    @SerialName("modelChangeValue")
    override val value: Int,
) : ModelChange

@Serializable
@SerialName("playerResultSetCasualtiesWithAdditionalSpp")
data class PlayerResultSetCasualtiesWithAdditionalSpp(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_CASUALTIES_WITH_ADDITIONAL_SPP,
    @SerialName("modelChangeKey")
    override val key: String,
    @SerialName("modelChangeValue")
    override val value: Int,
) : ModelChange

@Serializable
@SerialName("playerResultSetCompletions")
data class PlayerResultSetCompletions(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_COMPLETIONS,
    @SerialName("modelChangeKey")
    override val key: String,
    @SerialName("modelChangeValue")
    override val value: Int,
) : ModelChange

@Serializable
@SerialName("playerResultSetCompletionsWithAdditionalSpp")
data class PlayerResultSetCompletionsWithAdditionalSpp(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_COMPLETIONS_WITH_ADDITIONAL_SPP,
    @SerialName("modelChangeKey")
    override val key: String,
    @SerialName("modelChangeValue")
    override val value: Int,
) : ModelChange

@Serializable
@SerialName("playerResultSetCurrentSpps")
data class PlayerResultSetCurrentSpps(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_CURRENT_SPPS,
    @SerialName("modelChangeKey")
    override val key: String,
    @SerialName("modelChangeValue")
    override val value: Int,
) : ModelChange

@Serializable
@SerialName("playerResultSetDefecting")
data class PlayerResultSetDefecting(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_DEFECTING,
    @SerialName("modelChangeKey")
    override val key: String,
    @SerialName("modelChangeValue")
    override val value: Boolean,
) : ModelChange

@Serializable
@SerialName("playerResultSetFouls")
data class PlayerResultSetFouls(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_FOULS,
    @SerialName("modelChangeKey")
    override val key: String,
    @SerialName("modelChangeValue")
    override val value: Int,
) : ModelChange

@Serializable
@SerialName("playerResultSetHasUsedSecretWeapon")
data class PlayerResultSetHasUsedSecretWeapon(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_HAS_USED_SECRET_WEAPON,
    @SerialName("modelChangeKey")
    override val key: String,
    @SerialName("modelChangeValue")
    override val value: Boolean,
) : ModelChange

@Serializable
@SerialName("playerResultSetInterceptions")
data class PlayerResultSetInterceptions(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_INTERCEPTIONS,
    @SerialName("modelChangeKey")
    override val key: String,
    @SerialName("modelChangeValue")
    override val value: Int,
) : ModelChange

@Serializable
@SerialName("playerResultSetDeflections")
data class PlayerResultSetDeflections(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_DEFLECTIONS,
    @SerialName("modelChangeKey")
    override val key: String,
    @SerialName("modelChangeValue")
    override val value: Int,
) : ModelChange

@Serializable
@SerialName("playerResultSetPassing")
data class PlayerResultSetPassing(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_PASSING,
    @SerialName("modelChangeKey")
    override val key: String,
    @SerialName("modelChangeValue")
    override val value: Int,
) : ModelChange

@Serializable
@SerialName("playerResultSetPlayerAwards")
data class PlayerResultSetPlayerAwards(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_PLAYER_AWARDS,
    @SerialName("modelChangeKey")
    override val key: String,
    @SerialName("modelChangeValue")
    override val value: Int,
) : ModelChange

@Serializable
@SerialName("playerResultSetRushing")
data class PlayerResultSetRushing(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_RUSHING,
    @SerialName("modelChangeKey")
    override val key: String,
    @SerialName("modelChangeValue")
    override val value: Int,
) : ModelChange

@Serializable
@SerialName("playerResultSetSendToBoxByPlayerId")
data class PlayerResultSetSendToBoxByPlayerId(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_SEND_TO_BOX_BY_PLAYER_ID,
    @SerialName("modelChangeKey")
    override val key: String,
    @SerialName("modelChangeValue")
    override val value: String?,
) : ModelChange

@Serializable
@SerialName("playerResultSetSendToBoxHalf")
data class PlayerResultSetSendToBoxHalf(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_SEND_TO_BOX_HALF,
    @SerialName("modelChangeKey")
    override val key: String,
    @SerialName("modelChangeValue")
    override val value: Int,
) : ModelChange

@Serializable
@SerialName("playerResultSetSendToBoxReason")
data class PlayerResultSetSendToBoxReason(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_SEND_TO_BOX_REASON,
    @SerialName("modelChangeKey")
    override val key: String,
    @SerialName("modelChangeValue")
    override val value: String?,
) : ModelChange

@Serializable
@SerialName("playerResultSetSendToBoxTurn")
data class PlayerResultSetSendToBoxTurn(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_SEND_TO_BOX_TURN,
    @SerialName("modelChangeKey")
    override val key: String,
    @SerialName("modelChangeValue")
    override val value: Int,
) : ModelChange

@Serializable
@SerialName("playerResultSetSeriousInjury")
data class PlayerResultSetSeriousInjury(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_SERIOUS_INJURY,
    @SerialName("modelChangeKey")
    override val key: String,
    @SerialName("modelChangeValue")
    override val value: String?,
) : ModelChange

@Serializable
@SerialName("playerResultSetSeriousInjuryDecay")
data class PlayerResultSetSeriousInjuryDecay(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_SERIOUS_INJURY_DECAY,
    @SerialName("modelChangeKey")
    override val key: String,
    @SerialName("modelChangeValue")
    override val value: String?,
) : ModelChange

@Serializable
@SerialName("playerResultSetTouchdowns")
data class PlayerResultSetTouchdowns(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_TOUCHDOWNS,
    @SerialName("modelChangeKey")
    override val key: String,
    @SerialName("modelChangeValue")
    override val value: Int,
) : ModelChange

@Serializable
@SerialName("playerResultSetTurnsPlayed")
data class PlayerResultSetTurnsPlayed(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_TURNS_PLAYED,
    @SerialName("modelChangeKey")
    override val key: String,
    @SerialName("modelChangeValue")
    override val value: Int,
) : ModelChange

@Serializable
@SerialName("teamResultSetConceded")
data class TeamResultSetConceded(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.TEAM_RESULT_SET_CONCEDED,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: Boolean,
) : ModelChange

@Serializable
@SerialName("teamResultDedicatedFansModifier")
data class TeamResultDedicatedFansModifier(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.TEAM_RESULT_SET_DEDICATED_FANS_MODIFIER,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: Int,
) : ModelChange

@Serializable
@SerialName("teamResultSetFame")
data class TeamResultSetFame(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.TEAM_RESULT_SET_FAME,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: Int,
) : ModelChange

@Serializable
@SerialName("teamResultSetFanFactor")
data class TeamResultSetFanFactor(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.TEAM_RESULT_SET_FAN_FACTOR,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: Int,
) : ModelChange

@Serializable
@SerialName("teamResultSetBadlyHurtSuffered")
data class TeamResultSetBadlyHurtSuffered(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.TEAM_RESULT_SET_BADLY_HURT_SUFFERED,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: Int,
) : ModelChange

@Serializable
@SerialName("teamResultSetFanFactorModifier")
data class TeamResultSetFanFactorModifier(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.TEAM_RESULT_SET_FAN_FACTOR_MODIFIER,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: Int,
) : ModelChange

@Serializable
@SerialName("teamResultSetPenaltyScore")
data class TeamResultSetPenaltyScore(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.TEAM_RESULT_SET_PENALTY_SCORE,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: Int,
) : ModelChange

@Serializable
@SerialName("teamResultSetPettyCashTransferred")
data class TeamResultSetPettyCashTransferred(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.TEAM_RESULT_SET_PETTY_CASH_TRANSFERRED,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: Int,
) : ModelChange

@Serializable
@SerialName("teamResultSetPettyCashUsed")
data class TeamResultSetPettyCashUsed(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.TEAM_RESULT_SET_PETTY_CASH_USED,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: Int,
) : ModelChange

@Serializable
@SerialName("teamResultSetRaisedDead")
data class TeamResultSetRaisedDead(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.TEAM_RESULT_SET_RAISED_DEAD,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: Int,
) : ModelChange

@Serializable
@SerialName("teamResultSetRipSuffered")
data class TeamResultSetRipSuffered(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.TEAM_RESULT_SET_RIP_SUFFERED,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: Int,
) : ModelChange

@Serializable
@SerialName("teamResultSetScore")
data class TeamResultSetScore(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.TEAM_RESULT_SET_SCORE,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: Int,
) : ModelChange

@Serializable
@SerialName("teamResultSetSeriousInjurySuffered")
data class TeamResultSetSeriousInjurySuffered(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.TEAM_RESULT_SET_SERIOUS_INJURY_SUFFERED,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: Int,
) : ModelChange

@Serializable
@SerialName("teamResultSetSpectators")
data class TeamResultSetSpectators(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.TEAM_RESULT_SET_SPECTATORS,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: Int,
) : ModelChange

@Serializable
@SerialName("teamResultSetSpirallingExpenses")
data class TeamResultSetSpirallingExpenses(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.TEAM_RESULT_SET_SPIRALLING_EXPENSES,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: Int,
) : ModelChange

@Serializable
@SerialName("teamResultSetTeamValue")
data class TeamResultSetTeamValue(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.TEAM_RESULT_SET_TEAM_VALUE,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: Int,
) : ModelChange

@Serializable
@SerialName("teamResultSetWinnings")
data class TeamResultSetWinnings(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.TEAM_RESULT_SET_WINNINGS,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: Int,
) : ModelChange

@Serializable
@SerialName("turnDataSetApothecaries")
data class TurnDataSetApothecaries(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.TURN_DATA_SET_APOTHECARIES,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: Int,
) : ModelChange

@Serializable
@SerialName("turnDataSetBlitzUsed")
data class TurnDataSetBlitzUsed(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.TURN_DATA_SET_BLITZ_USED,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: Boolean,
) : ModelChange

@Serializable
@SerialName("turnDataSetBombUsed")
data class TurnDataSetBombUsed(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.TURN_DATA_SET_BOMB_USED,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: Boolean,
) : ModelChange

@Serializable
@SerialName("turnDataSetFirstTurnAfterKickoff")
data class TurnDataSetFirstTurnAfterKickoff(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.TURN_DATA_SET_FIRST_TURN_AFTER_KICKOFF,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: Boolean,
) : ModelChange

@Serializable
@SerialName("turnDataSetFoulUsed")
data class TurnDataSetFoulUsed(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.TURN_DATA_SET_FOUL_USED,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: Boolean,
) : ModelChange

@Serializable
@SerialName("turnDataSetHandOverUsed")
data class TurnDataSetHandOverUsed(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.TURN_DATA_SET_HAND_OVER_USED,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: Boolean,
) : ModelChange

@Serializable
@SerialName("turnDataSetLeaderState")
data class TurnDataSetLeaderState(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.TURN_DATA_SET_LEADER_STATE,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: String,
) : ModelChange

@Serializable
@SerialName("turnDataSetPassUsed")
data class TurnDataSetPassUsed(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.TURN_DATA_SET_PASS_USED,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: Boolean,
) : ModelChange

@Serializable
@SerialName("turnDataSetPlagueDoctors")
data class TurnDataSetPlagueDoctors(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.TURN_DATA_SET_PLAGUE_DOCTORS,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: Int,
) : ModelChange

@Serializable
@SerialName("turnDataSetKtmUsed")
data class TurnDataSetKtmUsed(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.TURN_DATA_SET_KTM_USED,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: Boolean,
) : ModelChange

@Serializable
@SerialName("turnDataSetReRolls")
data class TurnDataSetReRolls(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.TURN_DATA_SET_RE_ROLLS,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: Int,
) : ModelChange

@Serializable
@SerialName("turnDataSetReRollsBrilliantCoachingOneDrive")
data class TurnDataSetReRollsBrilliantCoachingOneDrive(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.TURN_DATA_SET_RE_ROLLS_BRILLIANT_COACHING_ONE_DRIVE,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: Int,
) : ModelChange

@Serializable
@SerialName("turnDataSetReRollsPumpUpTheCrowdOneDrive")
data class TurnDataSetReRollsPumpUpTheCrowdOneDrive(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.TURN_DATA_SET_RE_ROLLS_PUMP_UP_THE_CROWD_ONE_DRIVE,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: Int,
) : ModelChange

@Serializable
@SerialName("turnDataSetReRollsSingleUse")
data class TurnDataSetReRollsSingleUse(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.TURN_DATA_SET_RE_ROLLS_SINGLE_USE,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: Int,
) : ModelChange

@Serializable
@SerialName("turnDataSetReRollUsed")
data class TurnDataSetReRollUsed(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.TURN_DATA_SET_RE_ROLL_USED,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: Boolean,
) : ModelChange

@Serializable
@SerialName("turnDataSetTurnNr")
data class TurnDataSetTurnNr(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.TURN_DATA_SET_TURN_NR,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: Int,
) : ModelChange

@Serializable
@SerialName("turnDataSetTurnStarted")
data class TurnDataSetTurnStarted(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.TURN_DATA_SET_TURN_STARTED,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: Boolean,
) : ModelChange

@Serializable
@SerialName("turnDataSetWanderingApothecaries")
data class TurnDataSetWanderingApothecaries(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.TURN_DATA_SET_WANDERING_APOTHECARIES,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: Int,
) : ModelChange

@Serializable
@SerialName("turnDataSetCoachBanned")
data class TurnDataSetCoachBanned(
    @SerialName("modelChangeId")
    override val id: ModelChangeId = ModelChangeId.TURN_DATA_SET_COACH_BANNED,
    @SerialName("modelChangeKey")
    override val key: String?,
    @SerialName("modelChangeValue")
    override val value: Boolean,
) : ModelChange
