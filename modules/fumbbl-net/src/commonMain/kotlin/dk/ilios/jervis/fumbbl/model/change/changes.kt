@file:UseContextualSerialization(LocalDateTime::class)
package dk.ilios.jervis.fumbbl.model.change

import dk.ilios.jervis.fumbbl.model.BloodSpot
import dk.ilios.jervis.fumbbl.model.DialogOptions
import dk.ilios.jervis.fumbbl.model.DiceDecoration
import dk.ilios.jervis.fumbbl.model.Inducement
import dk.ilios.jervis.fumbbl.model.ModelChangeId
import dk.ilios.jervis.fumbbl.model.PlayerMarker
import dk.ilios.jervis.fumbbl.model.RangeRuler
import dk.ilios.jervis.fumbbl.model.TrackNumber
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization
import java.time.LocalDateTime

@Serializable
@SerialName("actingPlayerMarkSkillUsed")
class ActingPlayerMarkSkillUsed(
    override val modelChangeId: ModelChangeId = ModelChangeId.ACTING_PLAYER_MARK_SKILL_USED,
    override val modelChangeKey: Nothing?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("actingPlayerMarkSkillUnused")
class ActingPlayerMarkSkillUnused(
    override val modelChangeId: ModelChangeId = ModelChangeId.ACTING_PLAYER_MARK_SKILL_UNUSED,
    override val modelChangeKey: Nothing?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("actingPlayerSetCurrentMove")
class ActingPlayerSetCurrentMove(
    override val modelChangeId: ModelChangeId = ModelChangeId.ACTING_PLAYER_SET_CURRENT_MOVE,
    override val modelChangeKey: Nothing?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("actingPlayerSetDodging")
class ActingPlayerSetDodging(
    override val modelChangeId: ModelChangeId = ModelChangeId.ACTING_PLAYER_SET_DODGING,
    override val modelChangeKey: Nothing?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("actingPlayerSetGoingForIt")
class ActingPlayerSetGoingForIt(
    override val modelChangeId: ModelChangeId = ModelChangeId.ACTING_PLAYER_SET_GOING_FOR_IT,
    override val modelChangeKey: Nothing?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("actingPlayerSetHasBlocked")
class ActingPlayerSetHasBlocked(
    override val modelChangeId: ModelChangeId = ModelChangeId.ACTING_PLAYER_SET_HAS_BLOCKED,
    override val modelChangeKey: Nothing?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("actingPlayerSetHasFed")
class ActingPlayerSetHasFed(
    override val modelChangeId: ModelChangeId = ModelChangeId.ACTING_PLAYER_SET_HAS_FED,
    override val modelChangeKey: Nothing?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("actingPlayerSetHasFouled")
class ActingPlayerSetHasFouled(
    override val modelChangeId: ModelChangeId = ModelChangeId.ACTING_PLAYER_SET_HAS_FOULED,
    override val modelChangeKey: Nothing?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("actingPlayerSetHasJumped")
class ActingPlayerSetHasJumped(
    override val modelChangeId: ModelChangeId = ModelChangeId.ACTING_PLAYER_SET_HAS_JUMPED,
    override val modelChangeKey: Nothing?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("actingPlayerSetHasMoved")
class ActingPlayerSetHasMoved(
    override val modelChangeId: ModelChangeId = ModelChangeId.ACTING_PLAYER_SET_HAS_MOVED,
    override val modelChangeKey: Nothing?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("actingPlayerSetHasPassed")
class ActingPlayerSetHasPassed(
    override val modelChangeId: ModelChangeId = ModelChangeId.ACTING_PLAYER_SET_HAS_PASSED,
    override val modelChangeKey: Nothing?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("actingPlayerSetLeaping")
class ActingPlayerSetLeaping(
    override val modelChangeId: ModelChangeId = ModelChangeId.ACTING_PLAYER_SET_JUMPING,
    override val modelChangeKey: Nothing?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("actingPlayerSetOldPlayerState")
class ActingPlayerSetOldPlayerState(
    override val modelChangeId: ModelChangeId = ModelChangeId.ACTING_PLAYER_SET_OLD_PLAYER_STATE,
    override val modelChangeKey: Nothing?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("actingPlayerSetPlayerAction")
class ActingPlayerSetPlayerAction(
    override val modelChangeId: ModelChangeId = ModelChangeId.ACTING_PLAYER_SET_PLAYER_ACTION,
    override val modelChangeKey: Nothing?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("actingPlayerSetPlayerId")
class ActingPlayerSetPlayerId(
    override val modelChangeId: ModelChangeId = ModelChangeId.ACTING_PLAYER_SET_PLAYER_ID,
    override val modelChangeKey: Nothing?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("actingPlayerSetStandingUp")
class ActingPlayerSetStandingUp(
    override val modelChangeId: ModelChangeId = ModelChangeId.ACTING_PLAYER_SET_STANDING_UP,
    override val modelChangeKey: Nothing?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("actingPlayerSetStrength")
class ActingPlayerSetStrength(
    override val modelChangeId: ModelChangeId = ModelChangeId.ACTING_PLAYER_SET_STRENGTH,
    override val modelChangeKey: Nothing?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("actingPlayerSetSufferingAnimosity")
class ActingPlayerSetSufferingAnimosity(
    override val modelChangeId: ModelChangeId = ModelChangeId.ACTING_PLAYER_SET_SUFFERING_ANIMOSITY,
    override val modelChangeKey: Nothing?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("actingPlayerSetSufferingBloodLust")
class ActingPlayerSetSufferingBloodLust(
    override val modelChangeId: ModelChangeId = ModelChangeId.ACTING_PLAYER_SET_SUFFERING_BLOOD_LUST,
    override val modelChangeKey: Nothing?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("fieldModelAddBloodSpot")
class FieldModelAddBloodSpot(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_ADD_BLOOD_SPOT,
    override val modelChangeKey: String?,
    override val modelChangeValue: BloodSpot
): ModelChange

@Serializable
@SerialName("fieldModelAddCard")
class FieldModelAddCard(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_ADD_CARD,
    override val modelChangeKey: String?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("fieldModelAddCardEffect")
class FieldModelAddCardEffect(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_ADD_CARD_EFFECT,
    override val modelChangeKey: String?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("fieldModelAddDiceDecoration")
class FieldModelAddDiceDecoration(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_ADD_DICE_DECORATION,
    override val modelChangeKey: String?,
    override val modelChangeValue: DiceDecoration
): ModelChange

@Serializable
@SerialName("fieldModelAddIntensiveTraining")
class FieldModelAddIntensiveTraining(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_ADD_INTENSIVE_TRAINING,
    override val modelChangeKey: String?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("fieldModelAddFieldMarker")
class FieldModelAddFieldMarker(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_ADD_FIELD_MARKER,
    override val modelChangeKey: String?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("fieldModelAddMoveSquare")
class FieldModelAddMoveSquare(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_ADD_MOVE_SQUARE,
    override val modelChangeKey: String?,
    override val modelChangeValue: MoveSquare
): ModelChange

@Serializable
@SerialName("fieldModelAddPlayerMarker")
class FieldModelAddPlayerMarker(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_ADD_PLAYER_MARKER,
    override val modelChangeKey: String?,
    override val modelChangeValue: PlayerMarker
): ModelChange

@Serializable
@SerialName("fieldModelAddPrayer")
class FieldModelAddPrayer(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_ADD_PRAYER,
    override val modelChangeKey: String?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("fieldModelAddSkillEnhancements")
class FieldModelAddSkillEnhancements(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_ADD_SKILL_ENHANCEMENTS,
    override val modelChangeKey: String?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("fieldModelAddPushbackSquare")
class FieldModelAddPushbackSquare(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_ADD_PUSHBACK_SQUARE,
    override val modelChangeKey: String?,
    override val modelChangeValue: PushBackSquare
): ModelChange

@Serializable
@SerialName("fieldModelAddTrackNumber")
class FieldModelAddTrackNumber(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_ADD_TRACK_NUMBER,
    override val modelChangeKey: String?,
    override val modelChangeValue: TrackNumber
): ModelChange

@Serializable
@SerialName("fieldModelAddTrapDoor")
class FieldModelAddTrapDoor(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_ADD_TRAP_DOOR,
    override val modelChangeKey: String?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("fieldModelAddWisdom")
class FieldModelAddWisdom(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_ADD_WISDOM,
    override val modelChangeKey: String?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("fieldModelKeepDeactivatedCard")
class FieldModelKeepDeactivatedCard(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_KEEP_DEACTIVATED_CARD,
    override val modelChangeKey: String?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("fieldModelRemoveCard")
class FieldModelRemoveCard(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_REMOVE_CARD,
    override val modelChangeKey: String?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("fieldModelRemoveCardEffect")
class FieldModelRemoveCardEffect(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_REMOVE_CARD_EFFECT,
    override val modelChangeKey: String?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("fieldModelRemoveDiceDecoration")
class FieldModelRemoveDiceDecoration(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_REMOVE_DICE_DECORATION,
    override val modelChangeKey: String?,
    override val modelChangeValue: DiceDecoration
): ModelChange

@Serializable
@SerialName("fieldModelRemoveFieldMarker")
class FieldModelRemoveFieldMarker(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_REMOVE_FIELD_MARKER,
    override val modelChangeKey: String?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("fieldModelRemoveMoveSquare")
class FieldModelRemoveMoveSquare(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_REMOVE_MOVE_SQUARE,
    override val modelChangeKey: String?,
    override val modelChangeValue: MoveSquare
): ModelChange

@Serializable
@SerialName("fieldModelRemovePlayer")
class FieldModelRemovePlayer(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_REMOVE_PLAYER,
    override val modelChangeKey: String?,
    override val modelChangeValue: List<Int>?
): ModelChange

@Serializable
@SerialName("fieldModelRemovePlayerMarker")
class FieldModelRemovePlayerMarker(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_REMOVE_PLAYER_MARKER,
    override val modelChangeKey: String?,
    override val modelChangeValue: PlayerMarker
): ModelChange

@Serializable
@SerialName("fieldModelRemovePrayer")
class FieldModelRemovePrayer(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_REMOVE_PRAYER,
    override val modelChangeKey: String?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("fieldModelRemoveSkillEnhancements")
class FieldModelRemoveSkillEnhancements(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_REMOVE_SKILL_ENHANCEMENTS,
    override val modelChangeKey: String?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("fieldModelRemovePushbackSquare")
class FieldModelRemovePushbackSquare(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_REMOVE_PUSHBACK_SQUARE,
    override val modelChangeKey: String?,
    override val modelChangeValue: PushBackSquare
): ModelChange

@Serializable
@SerialName("fieldModelRemoveTrackNumber")
class FieldModelRemoveTrackNumber(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_REMOVE_TRACK_NUMBER,
    override val modelChangeKey: String?,
    override val modelChangeValue: TrackNumber
): ModelChange

@Serializable
@SerialName("fieldModelRemoveTrapDoor")
class FieldModelRemoveTrapDoor(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_REMOVE_TRAP_DOOR,
    override val modelChangeKey: String?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("fieldModelSetBallCoordinate")
class FieldModelSetBallCoordinate(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_SET_BALL_COORDINATE,
    override val modelChangeKey: String?,
    override val modelChangeValue: List<Int>?
): ModelChange

@Serializable
@SerialName("fieldModelSetBallInPlay")
class FieldModelSetBallInPlay(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_SET_BALL_IN_PLAY,
    override val modelChangeKey: String?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("fieldModelSetBallMoving")
class FieldModelSetBallMoving(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_SET_BALL_MOVING,
    override val modelChangeKey: String?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("fieldModelSetBlitzState")
class FieldModelSetBlitzState(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_SET_BLITZ_STATE,
    override val modelChangeKey: String?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("fieldModelSetBombCoordinate")
class FieldModelSetBombCoordinate(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_SET_BOMB_COORDINATE,
    override val modelChangeKey: String?,
    override val modelChangeValue: List<Int>?
): ModelChange

@Serializable
@SerialName("fieldModelSetBombMoving")
class FieldModelSetBombMoving(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_SET_BOMB_MOVING,
    override val modelChangeKey: String?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("fieldModelSetPlayerCoordinate")
class FieldModelSetPlayerCoordinate(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_SET_PLAYER_COORDINATE,
    override val modelChangeKey: String?,
    override val modelChangeValue: List<Int>?
): ModelChange

@Serializable
@SerialName("fieldModelSetPlayerState")
class FieldModelSetPlayerState(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_SET_PLAYER_STATE,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("fieldModelSetRangeRuler")
class FieldModelSetRangeRuler(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_SET_RANGE_RULER,
    override val modelChangeKey: String?,
    override val modelChangeValue: RangeRuler?
): ModelChange

@Serializable
@SerialName("fieldModelSetTargetSelectionState")
class FieldModelSetTargetSelectionState(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_SET_TARGET_SELECTION_STATE,
    override val modelChangeKey: String?,
    override val modelChangeValue: TargetSelectionState?
): ModelChange

@Serializable
@SerialName("fieldModelSetWeather")
class FieldModelSetWeather(
    override val modelChangeId: ModelChangeId = ModelChangeId.FIELD_MODEL_SET_WEATHER,
    override val modelChangeKey: String?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("gameSetAdminMode")
class GameSetAdminMode(
    override val modelChangeId: ModelChangeId = ModelChangeId.GAME_SET_ADMIN_MODE,
    override val modelChangeKey: String?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("gameSetConcededLegally")
class GameSetConcededLegally(
    override val modelChangeId: ModelChangeId = ModelChangeId.GAME_SET_CONCEDED_LEGALLY,
    override val modelChangeKey: String?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("gameSetConcessionPossible")
class GameSetConcessionPossible(
    override val modelChangeId: ModelChangeId = ModelChangeId.GAME_SET_CONCESSION_POSSIBLE,
    override val modelChangeKey: String?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("gameSetDefenderAction")
class GameSetDefenderAction(
    override val modelChangeId: ModelChangeId = ModelChangeId.GAME_SET_DEFENDER_ACTION,
    override val modelChangeKey: String?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("gameSetDefenderId")
class GameSetDefenderId(
    override val modelChangeId: ModelChangeId = ModelChangeId.GAME_SET_DEFENDER_ID,
    override val modelChangeKey: String?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("gameSetDialogParameter")
class GameSetDialogParameter(
    override val modelChangeId: ModelChangeId = ModelChangeId.GAME_SET_DIALOG_PARAMETER,
    override val modelChangeKey: String?,
    override val modelChangeValue: DialogOptions?
): ModelChange

@Serializable
@SerialName("gameSetFinished")
class GameSetFinished(
    override val modelChangeId: ModelChangeId = ModelChangeId.GAME_SET_FINISHED,
    override val modelChangeKey: String?,
    override val modelChangeValue: LocalDateTime?
): ModelChange

@Serializable
@SerialName("gameSetHalf")
class GameSetHalf(
    override val modelChangeId: ModelChangeId = ModelChangeId.GAME_SET_HALF,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("gameSetHomeFirstOffense")
class GameSetHomeFirstOffense(
    override val modelChangeId: ModelChangeId = ModelChangeId.GAME_SET_HOME_FIRST_OFFENSE,
    override val modelChangeKey: String?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("gameSetHomePlaying")
class GameSetHomePlaying(
    override val modelChangeId: ModelChangeId = ModelChangeId.GAME_SET_HOME_PLAYING,
    override val modelChangeKey: String?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("gameSetId")
class GameSetId(
    override val modelChangeId: ModelChangeId = ModelChangeId.GAME_SET_ID,
    override val modelChangeKey: String?,
    override val modelChangeValue: Long
): ModelChange

@Serializable
@SerialName("gameSetLastDefenderId")
class GameSetLastDefenderId(
    override val modelChangeId: ModelChangeId = ModelChangeId.GAME_SET_LAST_DEFENDER_ID,
    override val modelChangeKey: String?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("gameSetLastTurnMode")
class GameSetLastTurnMode(
    override val modelChangeId: ModelChangeId = ModelChangeId.GAME_SET_LAST_TURN_MODE,
    override val modelChangeKey: String?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("gameSetPassCoordinate")
class GameSetPassCoordinate(
    override val modelChangeId: ModelChangeId = ModelChangeId.GAME_SET_PASS_COORDINATE,
    override val modelChangeKey: String?,
    override val modelChangeValue: List<Int>?
): ModelChange

@Serializable
@SerialName("gameSetScheduled")
class GameSetScheduled(
    override val modelChangeId: ModelChangeId = ModelChangeId.GAME_SET_SCHEDULED,
    override val modelChangeKey: String?,
    override val modelChangeValue: LocalDateTime?
): ModelChange

@Serializable
@SerialName("gameSetSetupOffense")
class GameSetSetupOffense(
    override val modelChangeId: ModelChangeId = ModelChangeId.GAME_SET_SETUP_OFFENSE,
    override val modelChangeKey: String?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("gameSetStarted")
class GameSetStarted(
    override val modelChangeId: ModelChangeId = ModelChangeId.GAME_SET_STARTED,
    override val modelChangeKey: String?,
    override val modelChangeValue: LocalDateTime?
): ModelChange

@Serializable
@SerialName("gameSetTesting")
class GameSetTesting(
    override val modelChangeId: ModelChangeId = ModelChangeId.GAME_SET_TESTING,
    override val modelChangeKey: String?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("gameSetThrowerId")
class GameSetThrowerId(
    override val modelChangeId: ModelChangeId = ModelChangeId.GAME_SET_THROWER_ID,
    override val modelChangeKey: String?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("gameSetThrowerAction")
class GameSetThrowerAction(
    override val modelChangeId: ModelChangeId = ModelChangeId.GAME_SET_THROWER_ACTION,
    override val modelChangeKey: String?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("gameSetTimeoutEnforced")
class GameSetTimeoutEnforced(
    override val modelChangeId: ModelChangeId = ModelChangeId.GAME_SET_TIMEOUT_ENFORCED,
    override val modelChangeKey: String?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("gameSetTimeoutPossible")
class GameSetTimeoutPossible(
    override val modelChangeId: ModelChangeId = ModelChangeId.GAME_SET_TIMEOUT_POSSIBLE,
    override val modelChangeKey: String?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("gameSetTurnMode")
class GameSetTurnMode(
    override val modelChangeId: ModelChangeId = ModelChangeId.GAME_SET_TURN_MODE,
    override val modelChangeKey: String?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("gameSetWaitingForOpponent")
class GameSetWaitingForOpponent(
    override val modelChangeId: ModelChangeId = ModelChangeId.GAME_SET_WAITING_FOR_OPPONENT,
    override val modelChangeKey: String?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("gameOptionsAddOption")
class GameOptionsAddOption(
    override val modelChangeId: ModelChangeId = ModelChangeId.GAME_OPTIONS_ADD_OPTION,
    override val modelChangeKey: String?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("inducementSetActivateCard")
class InducementSetActivateCard(
    override val modelChangeId: ModelChangeId = ModelChangeId.INDUCEMENT_SET_ACTIVATE_CARD,
    override val modelChangeKey: String?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("inducementSetAddAvailableCard")
class InducementSetAddAvailableCard(
    override val modelChangeId: ModelChangeId = ModelChangeId.INDUCEMENT_SET_ADD_AVAILABLE_CARD,
    override val modelChangeKey: String?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("inducementSetAddInducement")
class InducementSetAddInducement(
    override val modelChangeId: ModelChangeId = ModelChangeId.INDUCEMENT_SET_ADD_INDUCEMENT,
    override val modelChangeKey: String?,
    override val modelChangeValue: Inducement
): ModelChange

@Serializable
@SerialName("inducementSetCardChoices")
class InducementSetCardChoices(
    override val modelChangeId: ModelChangeId = ModelChangeId.INDUCEMENT_SET_CARD_CHOICES,
    override val modelChangeKey: String?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("inducementSetDeactivateCard")
class InducementSetDeactivateCard(
    override val modelChangeId: ModelChangeId = ModelChangeId.INDUCEMENT_SET_DEACTIVATE_CARD,
    override val modelChangeKey: String?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("inducementSetAddPrayer")
class InducementSetAddPrayer(
    override val modelChangeId: ModelChangeId = ModelChangeId.INDUCEMENT_SET_ADD_PRAYER,
    override val modelChangeKey: String?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("inducementSetRemoveAvailableCard")
class InducementSetRemoveAvailableCard(
    override val modelChangeId: ModelChangeId = ModelChangeId.INDUCEMENT_SET_REMOVE_AVAILABLE_CARD,
    override val modelChangeKey: String?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("inducementSetRemoveInducement")
class InducementSetRemoveInducement(
    override val modelChangeId: ModelChangeId = ModelChangeId.INDUCEMENT_SET_REMOVE_INDUCEMENT,
    override val modelChangeKey: String?,
    override val modelChangeValue: Inducement
): ModelChange

@Serializable
@SerialName("inducementSetRemovePrayer")
class InducementSetRemovePrayer(
    override val modelChangeId: ModelChangeId = ModelChangeId.INDUCEMENT_SET_REMOVE_PRAYER,
    override val modelChangeKey: String?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("playerMarkSkillUsed")
class PlayerMarkSkillUsed(
    override val modelChangeId: ModelChangeId = ModelChangeId.PLAYER_MARK_SKILL_USED,
    override val modelChangeKey: String?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("playerMarkSkillUnused")
class PlayerMarkSkillUnused(
    override val modelChangeId: ModelChangeId = ModelChangeId.PLAYER_MARK_SKILL_UNUSED,
    override val modelChangeKey: String?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("playerResultSetBlocks")
class PlayerResultSetBlocks(
    override val modelChangeId: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_BLOCKS,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("playerResultSetCasualties")
class PlayerResultSetCasualties(
    override val modelChangeId: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_CASUALTIES,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("playerResultSetCasualtiesWithAdditionalSpp")
class PlayerResultSetCasualtiesWithAdditionalSpp(
    override val modelChangeId: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_CASUALTIES_WITH_ADDITIONAL_SPP,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("playerResultSetCompletions")
class PlayerResultSetCompletions(
    override val modelChangeId: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_COMPLETIONS,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("playerResultSetCompletionsWithAdditionalSpp")
class PlayerResultSetCompletionsWithAdditionalSpp(
    override val modelChangeId: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_COMPLETIONS_WITH_ADDITIONAL_SPP,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("playerResultSetCurrentSpps")
class PlayerResultSetCurrentSpps(
    override val modelChangeId: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_CURRENT_SPPS,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("playerResultSetDefecting")
class PlayerResultSetDefecting(
    override val modelChangeId: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_DEFECTING,
    override val modelChangeKey: String?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("playerResultSetFouls")
class PlayerResultSetFouls(
    override val modelChangeId: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_FOULS,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("playerResultSetHasUsedSecretWeapon")
class PlayerResultSetHasUsedSecretWeapon(
    override val modelChangeId: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_HAS_USED_SECRET_WEAPON,
    override val modelChangeKey: String?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("playerResultSetInterceptions")
class PlayerResultSetInterceptions(
    override val modelChangeId: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_INTERCEPTIONS,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("playerResultSetDeflections")
class PlayerResultSetDeflections(
    override val modelChangeId: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_DEFLECTIONS,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("playerResultSetPassing")
class PlayerResultSetPassing(
    override val modelChangeId: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_PASSING,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("playerResultSetPlayerAwards")
class PlayerResultSetPlayerAwards(
    override val modelChangeId: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_PLAYER_AWARDS,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("playerResultSetRushing")
class PlayerResultSetRushing(
    override val modelChangeId: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_RUSHING,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("playerResultSetSendToBoxByPlayerId")
class PlayerResultSetSendToBoxByPlayerId(
    override val modelChangeId: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_SEND_TO_BOX_BY_PLAYER_ID,
    override val modelChangeKey: String?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("playerResultSetSendToBoxHalf")
class PlayerResultSetSendToBoxHalf(
    override val modelChangeId: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_SEND_TO_BOX_HALF,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("playerResultSetSendToBoxReason")
class PlayerResultSetSendToBoxReason(
    override val modelChangeId: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_SEND_TO_BOX_REASON,
    override val modelChangeKey: String?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("playerResultSetSendToBoxTurn")
class PlayerResultSetSendToBoxTurn(
    override val modelChangeId: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_SEND_TO_BOX_TURN,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("playerResultSetSeriousInjury")
class PlayerResultSetSeriousInjury(
    override val modelChangeId: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_SERIOUS_INJURY,
    override val modelChangeKey: String?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("playerResultSetSeriousInjuryDecay")
class PlayerResultSetSeriousInjuryDecay(
    override val modelChangeId: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_SERIOUS_INJURY_DECAY,
    override val modelChangeKey: String?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("playerResultSetTouchdowns")
class PlayerResultSetTouchdowns(
    override val modelChangeId: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_TOUCHDOWNS,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("playerResultSetTurnsPlayed")
class PlayerResultSetTurnsPlayed(
    override val modelChangeId: ModelChangeId = ModelChangeId.PLAYER_RESULT_SET_TURNS_PLAYED,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("teamResultSetConceded")
class TeamResultSetConceded(
    override val modelChangeId: ModelChangeId = ModelChangeId.TEAM_RESULT_SET_CONCEDED,
    override val modelChangeKey: String?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("teamResultDedicatedFansModifier")
class TeamResultDedicatedFansModifier(
    override val modelChangeId: ModelChangeId = ModelChangeId.TEAM_RESULT_SET_DEDICATED_FANS_MODIFIER,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("teamResultSetFame")
class TeamResultSetFame(
    override val modelChangeId: ModelChangeId = ModelChangeId.TEAM_RESULT_SET_FAME,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("teamResultSetFanFactor")
class TeamResultSetFanFactor(
    override val modelChangeId: ModelChangeId = ModelChangeId.TEAM_RESULT_SET_FAN_FACTOR,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("teamResultSetBadlyHurtSuffered")
class TeamResultSetBadlyHurtSuffered(
    override val modelChangeId: ModelChangeId = ModelChangeId.TEAM_RESULT_SET_BADLY_HURT_SUFFERED,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("teamResultSetFanFactorModifier")
class TeamResultSetFanFactorModifier(
    override val modelChangeId: ModelChangeId = ModelChangeId.TEAM_RESULT_SET_FAN_FACTOR_MODIFIER,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("teamResultSetPenaltyScore")
class TeamResultSetPenaltyScore(
    override val modelChangeId: ModelChangeId = ModelChangeId.TEAM_RESULT_SET_PENALTY_SCORE,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("teamResultSetPettyCashTransferred")
class TeamResultSetPettyCashTransferred(
    override val modelChangeId: ModelChangeId = ModelChangeId.TEAM_RESULT_SET_PETTY_CASH_TRANSFERRED,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("teamResultSetPettyCashUsed")
class TeamResultSetPettyCashUsed(
    override val modelChangeId: ModelChangeId = ModelChangeId.TEAM_RESULT_SET_PETTY_CASH_USED,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("teamResultSetRaisedDead")
class TeamResultSetRaisedDead(
    override val modelChangeId: ModelChangeId = ModelChangeId.TEAM_RESULT_SET_RAISED_DEAD,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("teamResultSetRipSuffered")
class TeamResultSetRipSuffered(
    override val modelChangeId: ModelChangeId = ModelChangeId.TEAM_RESULT_SET_RIP_SUFFERED,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("teamResultSetScore")
class TeamResultSetScore(
    override val modelChangeId: ModelChangeId = ModelChangeId.TEAM_RESULT_SET_SCORE,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("teamResultSetSeriousInjurySuffered")
class TeamResultSetSeriousInjurySuffered(
    override val modelChangeId: ModelChangeId = ModelChangeId.TEAM_RESULT_SET_SERIOUS_INJURY_SUFFERED,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("teamResultSetSpectators")
class TeamResultSetSpectators(
    override val modelChangeId: ModelChangeId = ModelChangeId.TEAM_RESULT_SET_SPECTATORS,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("teamResultSetSpirallingExpenses")
class TeamResultSetSpirallingExpenses(
    override val modelChangeId: ModelChangeId = ModelChangeId.TEAM_RESULT_SET_SPIRALLING_EXPENSES,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("teamResultSetTeamValue")
class TeamResultSetTeamValue(
    override val modelChangeId: ModelChangeId = ModelChangeId.TEAM_RESULT_SET_TEAM_VALUE,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("teamResultSetWinnings")
class TeamResultSetWinnings(
    override val modelChangeId: ModelChangeId = ModelChangeId.TEAM_RESULT_SET_WINNINGS,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("turnDataSetApothecaries")
class TurnDataSetApothecaries(
    override val modelChangeId: ModelChangeId = ModelChangeId.TURN_DATA_SET_APOTHECARIES,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("turnDataSetBlitzUsed")
class TurnDataSetBlitzUsed(
    override val modelChangeId: ModelChangeId = ModelChangeId.TURN_DATA_SET_BLITZ_USED,
    override val modelChangeKey: String?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("turnDataSetBombUsed")
class TurnDataSetBombUsed(
    override val modelChangeId: ModelChangeId = ModelChangeId.TURN_DATA_SET_BOMB_USED,
    override val modelChangeKey: String?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("turnDataSetFirstTurnAfterKickoff")
class TurnDataSetFirstTurnAfterKickoff(
    override val modelChangeId: ModelChangeId = ModelChangeId.TURN_DATA_SET_FIRST_TURN_AFTER_KICKOFF,
    override val modelChangeKey: String?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("turnDataSetFoulUsed")
class TurnDataSetFoulUsed(
    override val modelChangeId: ModelChangeId = ModelChangeId.TURN_DATA_SET_FOUL_USED,
    override val modelChangeKey: String?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("turnDataSetHandOverUsed")
class TurnDataSetHandOverUsed(
    override val modelChangeId: ModelChangeId = ModelChangeId.TURN_DATA_SET_HAND_OVER_USED,
    override val modelChangeKey: String?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("turnDataSetLeaderState")
class TurnDataSetLeaderState(
    override val modelChangeId: ModelChangeId = ModelChangeId.TURN_DATA_SET_LEADER_STATE,
    override val modelChangeKey: String?,
    override val modelChangeValue: String?
): ModelChange

@Serializable
@SerialName("turnDataSetPassUsed")
class TurnDataSetPassUsed(
    override val modelChangeId: ModelChangeId = ModelChangeId.TURN_DATA_SET_PASS_USED,
    override val modelChangeKey: String?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("turnDataSetPlagueDoctors")
class TurnDataSetPlagueDoctors(
    override val modelChangeId: ModelChangeId = ModelChangeId.TURN_DATA_SET_PLAGUE_DOCTORS,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("turnDataSetKtmUsed")
class TurnDataSetKtmUsed(
    override val modelChangeId: ModelChangeId = ModelChangeId.TURN_DATA_SET_KTM_USED,
    override val modelChangeKey: String?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("turnDataSetReRolls")
class TurnDataSetReRolls(
    override val modelChangeId: ModelChangeId = ModelChangeId.TURN_DATA_SET_RE_ROLLS,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("turnDataSetReRollsBrilliantCoachingOneDrive")
class TurnDataSetReRollsBrilliantCoachingOneDrive(
    override val modelChangeId: ModelChangeId = ModelChangeId.TURN_DATA_SET_RE_ROLLS_BRILLIANT_COACHING_ONE_DRIVE,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("turnDataSetReRollsPumpUpTheCrowdOneDrive")
class TurnDataSetReRollsPumpUpTheCrowdOneDrive(
    override val modelChangeId: ModelChangeId = ModelChangeId.TURN_DATA_SET_RE_ROLLS_PUMP_UP_THE_CROWD_ONE_DRIVE,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("turnDataSetReRollsSingleUse")
class TurnDataSetReRollsSingleUse(
    override val modelChangeId: ModelChangeId = ModelChangeId.TURN_DATA_SET_RE_ROLLS_SINGLE_USE,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("turnDataSetReRollUsed")
class TurnDataSetReRollUsed(
    override val modelChangeId: ModelChangeId = ModelChangeId.TURN_DATA_SET_RE_ROLL_USED,
    override val modelChangeKey: String?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("turnDataSetTurnNr")
class TurnDataSetTurnNr(
    override val modelChangeId: ModelChangeId = ModelChangeId.TURN_DATA_SET_TURN_NR,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("turnDataSetTurnStarted")
class TurnDataSetTurnStarted(
    override val modelChangeId: ModelChangeId = ModelChangeId.TURN_DATA_SET_TURN_STARTED,
    override val modelChangeKey: String?,
    override val modelChangeValue: Boolean
): ModelChange

@Serializable
@SerialName("turnDataSetWanderingApothecaries")
class TurnDataSetWanderingApothecaries(
    override val modelChangeId: ModelChangeId = ModelChangeId.TURN_DATA_SET_WANDERING_APOTHECARIES,
    override val modelChangeKey: String?,
    override val modelChangeValue: Int
): ModelChange

@Serializable
@SerialName("turnDataSetCoachBanned")
class TurnDataSetCoachBanned(
    override val modelChangeId: ModelChangeId = ModelChangeId.TURN_DATA_SET_COACH_BANNED,
    override val modelChangeKey: String?,
    override val modelChangeValue: Boolean
): ModelChange
