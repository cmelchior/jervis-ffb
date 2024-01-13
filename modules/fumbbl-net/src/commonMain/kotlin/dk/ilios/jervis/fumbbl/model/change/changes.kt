@file:UseContextualSerialization(LocalDateTime::class)
package dk.ilios.jervis.fumbbl.model.change

import dk.ilios.jervis.fumbbl.model.BloodSpot
import dk.ilios.jervis.fumbbl.model.DialogOptions
import dk.ilios.jervis.fumbbl.model.DiceDecoration
import dk.ilios.jervis.fumbbl.model.FieldCoordinate
import dk.ilios.jervis.fumbbl.model.FieldMarker
import dk.ilios.jervis.fumbbl.model.Inducement
import dk.ilios.jervis.fumbbl.model.ModelChangeId
import dk.ilios.jervis.fumbbl.model.PlayerAction
import dk.ilios.jervis.fumbbl.model.PlayerMarker
import dk.ilios.jervis.fumbbl.model.PlayerState
import dk.ilios.jervis.fumbbl.model.RangeRuler
import dk.ilios.jervis.fumbbl.model.TrackNumber
import dk.ilios.jervis.fumbbl.model.TrapDoor
import dk.ilios.jervis.fumbbl.model.TurnMode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization
import java.time.LocalDateTime
@Serializable
@SerialName("actingPlayerMarkSkillUsed")
data class ActingPlayerMarkSkillUsed(
    override val modelChangeId: ModelChangeId = ModelChangeId.ACTING_PLAYER_MARK_SKILL_USED,
    override val modelChangeKey: Nothing?,
    override val modelChangeValue: String
): ModelChange

@Serializable
@SerialName("actingPlayerMarkSkillUnused")
data class ActingPlayerMarkSkillUnused(
    override val modelChangeId: ModelChangeId = ModelChangeId.ACTING_PLAYER_MARK_SKILL_UNUSED,
    override val modelChangeKey: Nothing?,
    override val modelChangeValue: String
): ModelChange

@Serializable
@SerialName("actingPlayerSetCurrentMove")
data class ActingPlayerSetCurrentMove(
    override val modelChangeId: ModelChangeId = ModelChangeId.ACTING_PLAYER_SET_CURRENT_MOVE,
    override val modelChangeKey: Nothing?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("actingPlayerSetDodging")
data class ActingPlayerSetDodging(
    override val modelChangeId: ModelChangeId = ModelChangeId.ACTING_PLAYER_SET_DODGING,
    override val modelChangeKey: Nothing?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("actingPlayerSetGoingForIt")
data class ActingPlayerSetGoingForIt(
    override val modelChangeId: ModelChangeId = ModelChangeId.ACTING_PLAYER_SET_GOING_FOR_IT,
    override val modelChangeKey: Nothing?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("actingPlayerSetHasBlocked")
data class ActingPlayerSetHasBlocked(
    override val modelChangeId: ModelChangeId = ModelChangeId.ACTING_PLAYER_SET_HAS_BLOCKED,
    override val modelChangeKey: Nothing?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("actingPlayerSetHasFed")
data class ActingPlayerSetHasFed(
    override val modelChangeId: ModelChangeId = ModelChangeId.ACTING_PLAYER_SET_HAS_FED,
    override val modelChangeKey: Nothing?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("actingPlayerSetHasFouled")
data class ActingPlayerSetHasFouled(
    override val modelChangeId: ModelChangeId = ModelChangeId.ACTING_PLAYER_SET_HAS_FOULED,
    override val modelChangeKey: Nothing?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("actingPlayerSetHasJumped")
data class ActingPlayerSetHasJumped(
    override val modelChangeId: ModelChangeId = ModelChangeId.ACTING_PLAYER_SET_HAS_JUMPED,
    override val modelChangeKey: Nothing?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("actingPlayerSetHasMoved")
data class ActingPlayerSetHasMoved(
    override val modelChangeId: ModelChangeId = ModelChangeId.ACTING_PLAYER_SET_HAS_MOVED,
    override val modelChangeKey: Nothing?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("actingPlayerSetHasPassed")
data class ActingPlayerSetHasPassed(
    override val modelChangeId: ModelChangeId = ModelChangeId.ACTING_PLAYER_SET_HAS_PASSED,
    override val modelChangeKey: Nothing?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("actingPlayerSetLeaping")
data class ActingPlayerSetLeaping(
    override val modelChangeId: ModelChangeId = ModelChangeId.ACTING_PLAYER_SET_JUMPING,
    override val modelChangeKey: Nothing?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("actingPlayerSetOldPlayerState")
data class ActingPlayerSetOldPlayerState(
    override val modelChangeId: ModelChangeId = ModelChangeId.ACTING_PLAYER_SET_OLD_PLAYER_STATE,
    override val modelChangeKey: Nothing?,
    override val modelChangeValue: PlayerState
): ModelChange

@Serializable
@SerialName("actingPlayerSetPlayerAction")
data class ActingPlayerSetPlayerAction(
    override val modelChangeId: ModelChangeId = ModelChangeId.ACTING_PLAYER_SET_PLAYER_ACTION,
    override val modelChangeKey: Nothing?,
    override val modelChangeValue: PlayerAction?
): ModelChange

@Serializable
@SerialName("actingPlayerSetPlayerId")
data class ActingPlayerSetPlayerId(
    override val modelChangeId: ModelChangeId = ModelChangeId.ACTING_PLAYER_SET_PLAYER_ID,
    override val modelChangeKey: Nothing?,
    override val modelChangeValue: PlayerId?
): ModelChange

@Serializable
@SerialName("actingPlayerSetStandingUp")
data class ActingPlayerSetStandingUp(
    override val modelChangeId: ModelChangeId = ModelChangeId.ACTING_PLAYER_SET_STANDING_UP,
    override val modelChangeKey: Nothing?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("actingPlayerSetStrength")
data class ActingPlayerSetStrength(
    override val modelChangeId: ModelChangeId = ModelChangeId.ACTING_PLAYER_SET_STRENGTH,
    override val modelChangeKey: Nothing?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("actingPlayerSetSufferingAnimosity")
data class ActingPlayerSetSufferingAnimosity(
    override val modelChangeId: ModelChangeId = ModelChangeId.ACTING_PLAYER_SET_SUFFERING_ANIMOSITY,
    override val modelChangeKey: Nothing?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("actingPlayerSetSufferingBloodLust")
data class ActingPlayerSetSufferingBloodLust(
    override val modelChangeId: ModelChangeId = ModelChangeId.ACTING_PLAYER_SET_SUFFERING_BLOOD_LUST,
    override val modelChangeKey: Nothing?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("fieldModelAddBloodSpot")
data class FieldModelAddBloodSpot(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_ADD_BLOOD_SPOT,
    override val modelChangeKey: String?,
    override val modelChangeValue: BloodSpot
): ModelChange

@Serializable
@SerialName("fieldModelAddCard")
data class FieldModelAddCard(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_ADD_CARD,
    override val modelChangeKey: String,
    override val modelChangeValue: String
): ModelChange

@Serializable
@SerialName("fieldModelAddCardEffect")
data class FieldModelAddCardEffect(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_ADD_CARD_EFFECT,
    override val modelChangeKey: String,
    override val modelChangeValue: String
): ModelChange

@Serializable
@SerialName("fieldModelAddDiceDecoration")
data class FieldModelAddDiceDecoration(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_ADD_DICE_DECORATION,
    override val modelChangeKey: String?,
    override val modelChangeValue: DiceDecoration
): ModelChange

@Serializable
@SerialName("fieldModelAddIntensiveTraining")
data class FieldModelAddIntensiveTraining(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_ADD_INTENSIVE_TRAINING,
    override val modelChangeKey: String?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("fieldModelAddFieldMarker")
data class FieldModelAddFieldMarker(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_ADD_FIELD_MARKER,
    override val modelChangeKey: String?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("fieldModelAddMoveSquare")
data class FieldModelAddMoveSquare(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_ADD_MOVE_SQUARE,
    override val modelChangeKey: String?,
    override val modelChangeValue: MoveSquare
): ModelChange

@Serializable
@SerialName("fieldModelAddPlayerMarker")
data class FieldModelAddPlayerMarker(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_ADD_PLAYER_MARKER,
    override val modelChangeKey: String?,
    override val modelChangeValue: PlayerMarker
): ModelChange

@Serializable
@SerialName("fieldModelAddPrayer")
data class FieldModelAddPrayer(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_ADD_PRAYER,
    override val modelChangeKey: String,
    override val modelChangeValue: String
): ModelChange

@Serializable
@SerialName("fieldModelAddSkillEnhancements")
data class FieldModelAddSkillEnhancements(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_ADD_SKILL_ENHANCEMENTS,
    override val modelChangeKey: String?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("fieldModelAddPushbackSquare")
data class FieldModelAddPushbackSquare(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_ADD_PUSHBACK_SQUARE,
    override val modelChangeKey: String?,
    override val modelChangeValue: PushBackSquare
): ModelChange

@Serializable
@SerialName("fieldModelAddTrackNumber")
data class FieldModelAddTrackNumber(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_ADD_TRACK_NUMBER,
    override val modelChangeKey: String?,
    override val modelChangeValue: TrackNumber
): ModelChange

@Serializable
@SerialName("fieldModelAddTrapDoor")
data class FieldModelAddTrapDoor(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_ADD_TRAP_DOOR,
    override val modelChangeKey: String?,
    override val modelChangeValue: TrapDoor
): ModelChange

@Serializable
@SerialName("fieldModelAddWisdom")
data class FieldModelAddWisdom(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_ADD_WISDOM,
    override val modelChangeKey: String?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("fieldModelKeepDeactivatedCard")
data class FieldModelKeepDeactivatedCard(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_KEEP_DEACTIVATED_CARD,
    override val modelChangeKey: String?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("fieldModelRemoveCard")
data class FieldModelRemoveCard(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_REMOVE_CARD,
    override val modelChangeKey: String,
    override val modelChangeValue: String
): ModelChange

@Serializable
@SerialName("fieldModelRemoveCardEffect")
data class FieldModelRemoveCardEffect(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_REMOVE_CARD_EFFECT,
    override val modelChangeKey: String,
    override val modelChangeValue: String
): ModelChange

@Serializable
@SerialName("fieldModelRemoveDiceDecoration")
data class FieldModelRemoveDiceDecoration(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_REMOVE_DICE_DECORATION,
    override val modelChangeKey: String?,
    override val modelChangeValue: DiceDecoration
): ModelChange

@Serializable
@SerialName("fieldModelRemoveFieldMarker")
data class FieldModelRemoveFieldMarker(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_REMOVE_FIELD_MARKER,
    override val modelChangeKey: String?,
    override val modelChangeValue: FieldMarker
): ModelChange

@Serializable
@SerialName("fieldModelRemoveMoveSquare")
data class FieldModelRemoveMoveSquare(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_REMOVE_MOVE_SQUARE,
    override val modelChangeKey: String?,
    override val modelChangeValue: MoveSquare
): ModelChange

@Serializable
@SerialName("fieldModelRemovePlayer")
data class FieldModelRemovePlayer(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_REMOVE_PLAYER,
    override val modelChangeKey: String,
    override val modelChangeValue: FieldCoordinate?
): ModelChange

@Serializable
@SerialName("fieldModelRemovePlayerMarker")
data class FieldModelRemovePlayerMarker(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_REMOVE_PLAYER_MARKER,
    override val modelChangeKey: String?,
    override val modelChangeValue: PlayerMarker
): ModelChange

@Serializable
@SerialName("fieldModelRemovePrayer")
data class FieldModelRemovePrayer(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_REMOVE_PRAYER,
    override val modelChangeKey: String,
    override val modelChangeValue: String
): ModelChange

@Serializable
@SerialName("fieldModelRemoveSkillEnhancements")
data class FieldModelRemoveSkillEnhancements(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_REMOVE_SKILL_ENHANCEMENTS,
    override val modelChangeKey: String,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("fieldModelRemovePushbackSquare")
data class FieldModelRemovePushbackSquare(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_REMOVE_PUSHBACK_SQUARE,
    override val modelChangeKey: String?,
    override val modelChangeValue: PushBackSquare
): ModelChange

@Serializable
@SerialName("fieldModelRemoveTrackNumber")
data class FieldModelRemoveTrackNumber(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_REMOVE_TRACK_NUMBER,
    override val modelChangeKey: String?,
    override val modelChangeValue: TrackNumber
): ModelChange

@Serializable
@SerialName("fieldModelRemoveTrapDoor")
data class FieldModelRemoveTrapDoor(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_REMOVE_TRAP_DOOR,
    override val modelChangeKey: String?,
    override val modelChangeValue: TrapDoor
): ModelChange

@Serializable
@SerialName("fieldModelSetBallCoordinate")
data class FieldModelSetBallCoordinate(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_SET_BALL_COORDINATE,
    override val modelChangeKey: String?,
    override val modelChangeValue: FieldCoordinate?
): ModelChange

@Serializable
@SerialName("fieldModelSetBallInPlay")
data class FieldModelSetBallInPlay(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_SET_BALL_IN_PLAY,
    override val modelChangeKey: String?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("fieldModelSetBallMoving")
data class FieldModelSetBallMoving(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_SET_BALL_MOVING,
    override val modelChangeKey: String?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("fieldModelSetBlitzState")
data class FieldModelSetBlitzState(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_SET_BLITZ_STATE,
    override val modelChangeKey: String?,
    override val modelChangeValue: TargetSelectionState?
): ModelChange

@Serializable
@SerialName("fieldModelSetBombCoordinate")
data class FieldModelSetBombCoordinate(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_SET_BOMB_COORDINATE,
    override val modelChangeKey: String?,
    override val modelChangeValue: FieldCoordinate?
): ModelChange

@Serializable
@SerialName("fieldModelSetBombMoving")
data class FieldModelSetBombMoving(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_SET_BOMB_MOVING,
    override val modelChangeKey: String?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("fieldModelSetPlayerCoordinate")
data class FieldModelSetPlayerCoordinate(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_SET_PLAYER_COORDINATE,
    override val modelChangeKey: String,
    override val modelChangeValue: FieldCoordinate?
): ModelChange

@Serializable
@SerialName("fieldModelSetPlayerState")
data class FieldModelSetPlayerState(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_SET_PLAYER_STATE,
    override val modelChangeKey: String,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("fieldModelSetRangeRuler")
data class FieldModelSetRangeRuler(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_SET_RANGE_RULER,
    override val modelChangeKey: String?,
    override val modelChangeValue: RangeRuler?
): ModelChange

@Serializable
@SerialName("fieldModelSetTargetSelectionState")
data class FieldModelSetTargetSelectionState(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_SET_TARGET_SELECTION_STATE,
    override val modelChangeKey: String?,
    override val modelChangeValue: TargetSelectionState?
): ModelChange

@Serializable
@SerialName("fieldModelSetWeather")
data class FieldModelSetWeather(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_SET_WEATHER,
    override val modelChangeKey: String?,
    override val modelChangeValue: String
): ModelChange

@Serializable
@SerialName("gameSetAdminMode")
data class GameSetAdminMode(
    override val modelChangeId: ModelChangeId = ModelChangeId.GAME_SET_ADMIN_MODE,
    override val modelChangeKey: String?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("gameSetConcededLegally")
data class GameSetConcededLegally(
    override val modelChangeId: ModelChangeId = ModelChangeId.GAME_SET_CONCEDED_LEGALLY,
    override val modelChangeKey: String?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("gameSetConcessionPossible")
data class GameSetConcessionPossible(
    override val modelChangeId: ModelChangeId = ModelChangeId.GAME_SET_CONCESSION_POSSIBLE,
    override val modelChangeKey: String?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("gameSetDefenderAction")
data class GameSetDefenderAction(
    override val modelChangeId: ModelChangeId = ModelChangeId.GAME_SET_DEFENDER_ACTION,
    override val modelChangeKey: String?,
    override val modelChangeValue: PlayerAction?
): ModelChange

@Serializable
@SerialName("gameSetDefenderId")
data class GameSetDefenderId(
    override val modelChangeId: ModelChangeId = ModelChangeId.GAME_SET_DEFENDER_ID,
    override val modelChangeKey: String?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("gameSetDialogParameter")
data class GameSetDialogParameter(
    override val modelChangeId: ModelChangeId = ModelChangeId.GAME_SET_DIALOG_PARAMETER,
    override val modelChangeKey: String?,
    override val modelChangeValue: DialogOptions?
): ModelChange

@Serializable
@SerialName("gameSetFinished")
data class GameSetFinished(
    override val modelChangeId: ModelChangeId = ModelChangeId.GAME_SET_FINISHED,
    override val modelChangeKey: String?,
    override val modelChangeValue: LocalDateTime?
): ModelChange

@Serializable
@SerialName("gameSetHalf")
data class GameSetHalf(
    override val modelChangeId: ModelChangeId = ModelChangeId.GAME_SET_HALF,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("gameSetHomeFirstOffense")
data class GameSetHomeFirstOffense(
    override val modelChangeId: ModelChangeId = ModelChangeId.GAME_SET_HOME_FIRST_OFFENSE,
    override val modelChangeKey: String?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("gameSetHomePlaying")
data class GameSetHomePlaying(
    override val modelChangeId: ModelChangeId = ModelChangeId.GAME_SET_HOME_PLAYING,
    override val modelChangeKey: String?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("gameSetId")
data class GameSetId(
    override val modelChangeId: ModelChangeId = ModelChangeId.GAME_SET_ID,
    override val modelChangeKey: String?,
    override val modelChangeValue: Long
): ModelChange

@Serializable
@SerialName("gameSetLastDefenderId")
data class GameSetLastDefenderId(
    override val modelChangeId: ModelChangeId = ModelChangeId.GAME_SET_LAST_DEFENDER_ID,
    override val modelChangeKey: String?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("gameSetLastTurnMode")
data class GameSetLastTurnMode(
    override val modelChangeId: ModelChangeId = ModelChangeId.GAME_SET_LAST_TURN_MODE,
    override val modelChangeKey: String?,
    override val modelChangeValue: TurnMode?
): ModelChange

@Serializable
@SerialName("gameSetPassCoordinate")
data class GameSetPassCoordinate(
    override val modelChangeId: ModelChangeId = ModelChangeId.GAME_SET_PASS_COORDINATE,
    override val modelChangeKey: String?,
    override val modelChangeValue: FieldCoordinate?
): ModelChange

@Serializable
@SerialName("gameSetScheduled")
data class GameSetScheduled(
    override val modelChangeId: ModelChangeId = ModelChangeId.GAME_SET_SCHEDULED,
    override val modelChangeKey: String?,
    override val modelChangeValue: LocalDateTime?
): ModelChange

@Serializable
@SerialName("gameSetSetupOffense")
data class GameSetSetupOffense(
    override val modelChangeId: ModelChangeId = ModelChangeId.GAME_SET_SETUP_OFFENSE,
    override val modelChangeKey: String?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("gameSetStarted")
data class GameSetStarted(
    override val modelChangeId: ModelChangeId = ModelChangeId.GAME_SET_STARTED,
    override val modelChangeKey: String?,
    override val modelChangeValue: LocalDateTime?
): ModelChange

@Serializable
@SerialName("gameSetTesting")
data class GameSetTesting(
    override val modelChangeId: ModelChangeId = ModelChangeId.GAME_SET_TESTING,
    override val modelChangeKey: String?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("gameSetThrowerId")
data class GameSetThrowerId(
    override val modelChangeId: ModelChangeId = ModelChangeId.GAME_SET_THROWER_ID,
    override val modelChangeKey: String?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("gameSetThrowerAction")
data class GameSetThrowerAction(
    override val modelChangeId: ModelChangeId = ModelChangeId.GAME_SET_THROWER_ACTION,
    override val modelChangeKey: String?,
    override val modelChangeValue: PlayerAction?
): ModelChange

@Serializable
@SerialName("gameSetTimeoutEnforced")
data class GameSetTimeoutEnforced(
    override val modelChangeId: ModelChangeId = ModelChangeId.GAME_SET_TIMEOUT_ENFORCED,
    override val modelChangeKey: String?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("gameSetTimeoutPossible")
data class GameSetTimeoutPossible(
    override val modelChangeId: ModelChangeId = ModelChangeId.GAME_SET_TIMEOUT_POSSIBLE,
    override val modelChangeKey: String?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("gameSetTurnMode")
data class GameSetTurnMode(
    override val modelChangeId: ModelChangeId = ModelChangeId.GAME_SET_TURN_MODE,
    override val modelChangeKey: String?,
    override val modelChangeValue: TurnMode
): ModelChange

@Serializable
@SerialName("gameSetWaitingForOpponent")
data class GameSetWaitingForOpponent(
    override val modelChangeId: ModelChangeId = ModelChangeId.GAME_SET_WAITING_FOR_OPPONENT,
    override val modelChangeKey: String?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("gameOptionsAddOption")
data class GameOptionsAddOption(
    override val modelChangeId: ModelChangeId = ModelChangeId.GAME_OPTIONS_ADD_OPTION,
    override val modelChangeKey: String?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("inducementSetActivateCard")
data class InducementSetActivateCard(
    override val modelChangeId: ModelChangeId = ModelChangeId.INDUCEMENT_SET_ACTIVATE_CARD,
    override val modelChangeKey: String?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("inducementSetAddAvailableCard")
data class InducementSetAddAvailableCard(
    override val modelChangeId: ModelChangeId = ModelChangeId.INDUCEMENT_SET_ADD_AVAILABLE_CARD,
    override val modelChangeKey: String?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("inducementSetAddInducement")
data class InducementSetAddInducement(
    override val modelChangeId: ModelChangeId = ModelChangeId.INDUCEMENT_SET_ADD_INDUCEMENT,
    override val modelChangeKey: String?,
    override val modelChangeValue: Inducement
): ModelChange

@Serializable
@SerialName("inducementSetCardChoices")
data class InducementSetCardChoices(
    override val modelChangeId: ModelChangeId = ModelChangeId.INDUCEMENT_SET_CARD_CHOICES,
    override val modelChangeKey: String?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("inducementSetDeactivateCard")
data class InducementSetDeactivateCard(
    override val modelChangeId: ModelChangeId = ModelChangeId.INDUCEMENT_SET_DEACTIVATE_CARD,
    override val modelChangeKey: String?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("inducementSetAddPrayer")
data class InducementSetAddPrayer(
    override val modelChangeId: ModelChangeId = ModelChangeId.INDUCEMENT_SET_ADD_PRAYER,
    override val modelChangeKey: String?,
    override val modelChangeValue: String
): ModelChange

@Serializable
@SerialName("inducementSetRemoveAvailableCard")
data class InducementSetRemoveAvailableCard(
    override val modelChangeId: ModelChangeId = ModelChangeId.INDUCEMENT_SET_REMOVE_AVAILABLE_CARD,
    override val modelChangeKey: String?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("inducementSetRemoveInducement")
data class InducementSetRemoveInducement(
    override val modelChangeId: ModelChangeId = ModelChangeId.INDUCEMENT_SET_REMOVE_INDUCEMENT,
    override val modelChangeKey: String?,
    override val modelChangeValue: Inducement
): ModelChange

@Serializable
@SerialName("inducementSetRemovePrayer")
data class InducementSetRemovePrayer(
    override val modelChangeId: ModelChangeId = ModelChangeId.INDUCEMENT_SET_REMOVE_PRAYER,
    override val modelChangeKey: String?,
    override val modelChangeValue: String
): ModelChange

@Serializable
@SerialName("playerMarkSkillUsed")
data class PlayerMarkSkillUsed(
    override val modelChangeId: ModelChangeId = ModelChangeId.PLAYER_MARK_SKILL_USED,
    override val modelChangeKey: String,
    override val modelChangeValue: String
): ModelChange

@Serializable
@SerialName("playerMarkSkillUnused")
data class PlayerMarkSkillUnused(
    override val modelChangeId: ModelChangeId = ModelChangeId.PLAYER_MARK_SKILL_UNUSED,
    override val modelChangeKey: String,
    override val modelChangeValue: String
): ModelChange

@Serializable
@SerialName("playerResultSetBlocks")
data class PlayerResultSetBlocks(
    override val modelChangeId: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_BLOCKS,
    override val modelChangeKey: String,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("playerResultSetCasualties")
data class PlayerResultSetCasualties(
    override val modelChangeId: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_CASUALTIES,
    override val modelChangeKey: String,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("playerResultSetCasualtiesWithAdditionalSpp")
data class PlayerResultSetCasualtiesWithAdditionalSpp(
    override val modelChangeId: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_CASUALTIES_WITH_ADDITIONAL_SPP,
    override val modelChangeKey: String,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("playerResultSetCompletions")
data class PlayerResultSetCompletions(
    override val modelChangeId: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_COMPLETIONS,
    override val modelChangeKey: String,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("playerResultSetCompletionsWithAdditionalSpp")
data class PlayerResultSetCompletionsWithAdditionalSpp(
    override val modelChangeId: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_COMPLETIONS_WITH_ADDITIONAL_SPP,
    override val modelChangeKey: String,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("playerResultSetCurrentSpps")
data class PlayerResultSetCurrentSpps(
    override val modelChangeId: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_CURRENT_SPPS,
    override val modelChangeKey: String,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("playerResultSetDefecting")
data class PlayerResultSetDefecting(
    override val modelChangeId: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_DEFECTING,
    override val modelChangeKey: String,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("playerResultSetFouls")
data class PlayerResultSetFouls(
    override val modelChangeId: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_FOULS,
    override val modelChangeKey: String,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("playerResultSetHasUsedSecretWeapon")
data class PlayerResultSetHasUsedSecretWeapon(
    override val modelChangeId: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_HAS_USED_SECRET_WEAPON,
    override val modelChangeKey: String,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("playerResultSetInterceptions")
data class PlayerResultSetInterceptions(
    override val modelChangeId: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_INTERCEPTIONS,
    override val modelChangeKey: String,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("playerResultSetDeflections")
data class PlayerResultSetDeflections(
    override val modelChangeId: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_DEFLECTIONS,
    override val modelChangeKey: String,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("playerResultSetPassing")
data class PlayerResultSetPassing(
    override val modelChangeId: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_PASSING,
    override val modelChangeKey: String,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("playerResultSetPlayerAwards")
data class PlayerResultSetPlayerAwards(
    override val modelChangeId: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_PLAYER_AWARDS,
    override val modelChangeKey: String,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("playerResultSetRushing")
data class PlayerResultSetRushing(
    override val modelChangeId: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_RUSHING,
    override val modelChangeKey: String,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("playerResultSetSendToBoxByPlayerId")
data class PlayerResultSetSendToBoxByPlayerId(
    override val modelChangeId: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_SEND_TO_BOX_BY_PLAYER_ID,
    override val modelChangeKey: String,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("playerResultSetSendToBoxHalf")
data class PlayerResultSetSendToBoxHalf(
    override val modelChangeId: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_SEND_TO_BOX_HALF,
    override val modelChangeKey: String,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("playerResultSetSendToBoxReason")
data class PlayerResultSetSendToBoxReason(
    override val modelChangeId: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_SEND_TO_BOX_REASON,
    override val modelChangeKey: String,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("playerResultSetSendToBoxTurn")
data class PlayerResultSetSendToBoxTurn(
    override val modelChangeId: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_SEND_TO_BOX_TURN,
    override val modelChangeKey: String,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("playerResultSetSeriousInjury")
data class PlayerResultSetSeriousInjury(
    override val modelChangeId: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_SERIOUS_INJURY,
    override val modelChangeKey: String,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("playerResultSetSeriousInjuryDecay")
data class PlayerResultSetSeriousInjuryDecay(
    override val modelChangeId: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_SERIOUS_INJURY_DECAY,
    override val modelChangeKey: String,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("playerResultSetTouchdowns")
data class PlayerResultSetTouchdowns(
    override val modelChangeId: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_TOUCHDOWNS,
    override val modelChangeKey: String,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("playerResultSetTurnsPlayed")
data class PlayerResultSetTurnsPlayed(
    override val modelChangeId: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_TURNS_PLAYED,
    override val modelChangeKey: String,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("teamResultSetConceded")
data class TeamResultSetConceded(
    override val modelChangeId: ModelChangeId = ModelChangeId.TEAM_RESULT_SET_CONCEDED,
    override val modelChangeKey: String?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("teamResultDedicatedFansModifier")
data class TeamResultDedicatedFansModifier(
    override val modelChangeId: ModelChangeId = ModelChangeId.TEAM_RESULT_SET_DEDICATED_FANS_MODIFIER,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("teamResultSetFame")
data class TeamResultSetFame(
    override val modelChangeId: ModelChangeId = ModelChangeId.TEAM_RESULT_SET_FAME,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("teamResultSetFanFactor")
data class TeamResultSetFanFactor(
    override val modelChangeId: ModelChangeId = ModelChangeId.TEAM_RESULT_SET_FAN_FACTOR,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("teamResultSetBadlyHurtSuffered")
data class TeamResultSetBadlyHurtSuffered(
    override val modelChangeId: ModelChangeId = ModelChangeId.TEAM_RESULT_SET_BADLY_HURT_SUFFERED,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("teamResultSetFanFactorModifier")
data class TeamResultSetFanFactorModifier(
    override val modelChangeId: ModelChangeId = ModelChangeId.TEAM_RESULT_SET_FAN_FACTOR_MODIFIER,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("teamResultSetPenaltyScore")
data class TeamResultSetPenaltyScore(
    override val modelChangeId: ModelChangeId = ModelChangeId.TEAM_RESULT_SET_PENALTY_SCORE,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("teamResultSetPettyCashTransferred")
data class TeamResultSetPettyCashTransferred(
    override val modelChangeId: ModelChangeId = ModelChangeId.TEAM_RESULT_SET_PETTY_CASH_TRANSFERRED,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("teamResultSetPettyCashUsed")
data class TeamResultSetPettyCashUsed(
    override val modelChangeId: ModelChangeId = ModelChangeId.TEAM_RESULT_SET_PETTY_CASH_USED,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("teamResultSetRaisedDead")
data class TeamResultSetRaisedDead(
    override val modelChangeId: ModelChangeId = ModelChangeId.TEAM_RESULT_SET_RAISED_DEAD,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("teamResultSetRipSuffered")
data class TeamResultSetRipSuffered(
    override val modelChangeId: ModelChangeId = ModelChangeId.TEAM_RESULT_SET_RIP_SUFFERED,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("teamResultSetScore")
data class TeamResultSetScore(
    override val modelChangeId: ModelChangeId = ModelChangeId.TEAM_RESULT_SET_SCORE,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("teamResultSetSeriousInjurySuffered")
data class TeamResultSetSeriousInjurySuffered(
    override val modelChangeId: ModelChangeId = ModelChangeId.TEAM_RESULT_SET_SERIOUS_INJURY_SUFFERED,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("teamResultSetSpectators")
data class TeamResultSetSpectators(
    override val modelChangeId: ModelChangeId = ModelChangeId.TEAM_RESULT_SET_SPECTATORS,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("teamResultSetSpirallingExpenses")
data class TeamResultSetSpirallingExpenses(
    override val modelChangeId: ModelChangeId = ModelChangeId.TEAM_RESULT_SET_SPIRALLING_EXPENSES,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("teamResultSetTeamValue")
data class TeamResultSetTeamValue(
    override val modelChangeId: ModelChangeId = ModelChangeId.TEAM_RESULT_SET_TEAM_VALUE,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("teamResultSetWinnings")
data class TeamResultSetWinnings(
    override val modelChangeId: ModelChangeId = ModelChangeId.TEAM_RESULT_SET_WINNINGS,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("turnDataSetApothecaries")
data class TurnDataSetApothecaries(
    override val modelChangeId: ModelChangeId = ModelChangeId.TURN_DATA_SET_APOTHECARIES,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("turnDataSetBlitzUsed")
data class TurnDataSetBlitzUsed(
    override val modelChangeId: ModelChangeId = ModelChangeId.TURN_DATA_SET_BLITZ_USED,
    override val modelChangeKey: String?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("turnDataSetBombUsed")
data class TurnDataSetBombUsed(
    override val modelChangeId: ModelChangeId = ModelChangeId.TURN_DATA_SET_BOMB_USED,
    override val modelChangeKey: String?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("turnDataSetFirstTurnAfterKickoff")
data class TurnDataSetFirstTurnAfterKickoff(
    override val modelChangeId: ModelChangeId = ModelChangeId.TURN_DATA_SET_FIRST_TURN_AFTER_KICKOFF,
    override val modelChangeKey: String?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("turnDataSetFoulUsed")
data class TurnDataSetFoulUsed(
    override val modelChangeId: ModelChangeId = ModelChangeId.TURN_DATA_SET_FOUL_USED,
    override val modelChangeKey: String?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("turnDataSetHandOverUsed")
data class TurnDataSetHandOverUsed(
    override val modelChangeId: ModelChangeId = ModelChangeId.TURN_DATA_SET_HAND_OVER_USED,
    override val modelChangeKey: String?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("turnDataSetLeaderState")
data class TurnDataSetLeaderState(
    override val modelChangeId: ModelChangeId = ModelChangeId.TURN_DATA_SET_LEADER_STATE,
    override val modelChangeKey: String?,
    override val modelChangeValue: String
): ModelChange

@Serializable
@SerialName("turnDataSetPassUsed")
data class TurnDataSetPassUsed(
    override val modelChangeId: ModelChangeId = ModelChangeId.TURN_DATA_SET_PASS_USED,
    override val modelChangeKey: String?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("turnDataSetPlagueDoctors")
data class TurnDataSetPlagueDoctors(
    override val modelChangeId: ModelChangeId = ModelChangeId.TURN_DATA_SET_PLAGUE_DOCTORS,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("turnDataSetKtmUsed")
data class TurnDataSetKtmUsed(
    override val modelChangeId: ModelChangeId = ModelChangeId.TURN_DATA_SET_KTM_USED,
    override val modelChangeKey: String?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("turnDataSetReRolls")
data class TurnDataSetReRolls(
    override val modelChangeId: ModelChangeId = ModelChangeId.TURN_DATA_SET_RE_ROLLS,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("turnDataSetReRollsBrilliantCoachingOneDrive")
data class TurnDataSetReRollsBrilliantCoachingOneDrive(
    override val modelChangeId: ModelChangeId = ModelChangeId.TURN_DATA_SET_RE_ROLLS_BRILLIANT_COACHING_ONE_DRIVE,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("turnDataSetReRollsPumpUpTheCrowdOneDrive")
data class TurnDataSetReRollsPumpUpTheCrowdOneDrive(
    override val modelChangeId: ModelChangeId = ModelChangeId.TURN_DATA_SET_RE_ROLLS_PUMP_UP_THE_CROWD_ONE_DRIVE,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("turnDataSetReRollsSingleUse")
data class TurnDataSetReRollsSingleUse(
    override val modelChangeId: ModelChangeId = ModelChangeId.TURN_DATA_SET_RE_ROLLS_SINGLE_USE,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("turnDataSetReRollUsed")
data class TurnDataSetReRollUsed(
    override val modelChangeId: ModelChangeId = ModelChangeId.TURN_DATA_SET_RE_ROLL_USED,
    override val modelChangeKey: String?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("turnDataSetTurnNr")
data class TurnDataSetTurnNr(
    override val modelChangeId: ModelChangeId = ModelChangeId.TURN_DATA_SET_TURN_NR,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("turnDataSetTurnStarted")
data class TurnDataSetTurnStarted(
    override val modelChangeId: ModelChangeId = ModelChangeId.TURN_DATA_SET_TURN_STARTED,
    override val modelChangeKey: String?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("turnDataSetWanderingApothecaries")
data class TurnDataSetWanderingApothecaries(
    override val modelChangeId: ModelChangeId = ModelChangeId.TURN_DATA_SET_WANDERING_APOTHECARIES,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("turnDataSetCoachBanned")
data class TurnDataSetCoachBanned(
    override val modelChangeId: ModelChangeId = ModelChangeId.TURN_DATA_SET_COACH_BANNED,
    override val modelChangeKey: String?,
    override val modelChangeValue: Boolean
): ModelChange
