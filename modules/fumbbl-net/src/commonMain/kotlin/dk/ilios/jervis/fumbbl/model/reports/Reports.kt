package dk.ilios.jervis.fumbbl.model.reports

import dk.ilios.jervis.fumbbl.model.BlockResult
import dk.ilios.jervis.fumbbl.model.CatchModifier
import dk.ilios.jervis.fumbbl.model.Direction
import dk.ilios.jervis.fumbbl.model.FieldCoordinate
import dk.ilios.jervis.fumbbl.model.PlayerAction
import dk.ilios.jervis.fumbbl.model.PushbackMode
import dk.ilios.jervis.fumbbl.model.ReportId
import dk.ilios.jervis.fumbbl.model.change.PlayerId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
@JsonClassDiscriminator("reportId")
sealed interface Report {
    val reportId: ReportId
}

@Serializable
@SerialName("allYouCanEat")
data class AllYouCanEatReport(
    override val reportId: ReportId = ReportId.ALL_YOU_CAN_EAT,
) : Report

@Serializable
@SerialName("alwaysHungryRoll")
data class AlwaysHungryRollReport(
    override val reportId: ReportId = ReportId.ALWAYS_HUNGRY_ROLL,
) : Report

@Serializable
@SerialName("animalSavagery")
data class AnimalSavageryReport(
    override val reportId: ReportId = ReportId.ANIMAL_SAVAGERY,
) : Report

@Serializable
@SerialName("animosityRoll")
data class AnimosityRollReport(
    override val reportId: ReportId = ReportId.ANIMOSITY_ROLL,
) : Report

@Serializable
@SerialName("apothecaryChoice")
data class ApothecaryChoiceReport(
    override val reportId: ReportId = ReportId.APOTHECARY_CHOICE,
) : Report

@Serializable
@SerialName("apothecaryRoll")
data class ApothecaryRollReport(
    override val reportId: ReportId = ReportId.APOTHECARY_ROLL,
) : Report

@Serializable
@SerialName("argueTheCall")
data class ArgueTheCallReport(
    override val reportId: ReportId = ReportId.ARGUE_THE_CALL,
) : Report

@Serializable
@SerialName("balefulHex")
data class BalefulHexReport(
    override val reportId: ReportId = ReportId.BALEFUL_HEX,
) : Report

@Serializable
@SerialName("biasedRef")
data class BiasedRefReport(
    override val reportId: ReportId = ReportId.BIASED_REF,
) : Report

@Serializable
@SerialName("biteSpectator")
data class BiteSpectatorReport(
    override val reportId: ReportId = ReportId.BITE_SPECTATOR,
) : Report

@Serializable
@SerialName("blitzRoll")
data class BlitzRollReport(
    override val reportId: ReportId = ReportId.BLITZ_ROLL,
) : Report

@Serializable
@SerialName("block")
data class BlockReport(
    override val reportId: ReportId = ReportId.BLOCK,
    val defenderId: PlayerId,
) : Report

@Serializable
@SerialName("blockChoice")
data class BlockChoiceReport(
    override val reportId: ReportId = ReportId.BLOCK_CHOICE,
    val nrOfDice: Int,
    val blockRoll: List<Int>,
    val diceIndex: Int,
    val blockResult: BlockResult,
    val defenderId: PlayerId,
    val suppressExtraEffectHandling: Boolean,
    val showNameInReport: Boolean,
    val blockRollId: Int,
) : Report

@Serializable
@SerialName("blockReRoll")
data class BlockReRollReport(
    override val reportId: ReportId = ReportId.BLOCK_RE_ROLL,
) : Report

@Serializable
@SerialName("blockRoll")
data class BlockRollReport(
    override val reportId: ReportId = ReportId.BLOCK_ROLL,
    val choosingTeamId: dk.ilios.jervis.fumbbl.model.change.TeamId,
    val blockRoll: List<Int>,
    val defenderId: PlayerId? = null, // Is this always `null`?
) : Report

@Serializable
@SerialName("bloodLustRoll")
data class BloodLustRollReport(
    override val reportId: ReportId = ReportId.BLOOD_LUST_ROLL,
) : Report

@Serializable
@SerialName("bombExplodesAfterCatch")
data class BombExplodesAfterCatchReport(
    override val reportId: ReportId = ReportId.BOMB_EXPLODES_AFTER_CATCH,
) : Report

@Serializable
@SerialName("bombOutOfBounds")
data class BombOutOfBoundsReport(
    override val reportId: ReportId = ReportId.BOMB_OUT_OF_BOUNDS,
) : Report

@Serializable
@SerialName("briberyAndCorruptionReRoll")
data class BriberyAndCorruptionReRollReport(
    override val reportId: ReportId = ReportId.BRIBERY_AND_CORRUPTION_RE_ROLL,
) : Report

@Serializable
@SerialName("bribesRoll")
data class BribesRollReport(
    override val reportId: ReportId = ReportId.BRIBES_ROLL,
) : Report

@Serializable
@SerialName("brilliantCoachingReRoll")
data class BrilliantCoachingReRollReport(
    override val reportId: ReportId = ReportId.BRILLIANT_COACHING_RE_ROLLS_LOST,
) : Report

@Serializable
@SerialName("cardsAndInducementsBought")
data class CardsAndInducementsBoughtReport(
    override val reportId: ReportId = ReportId.CARDS_AND_INDUCEMENTS_BOUGHT,
) : Report

@Serializable
@SerialName("cardsBought")
data class CardsBoughtReport(
    override val reportId: ReportId = ReportId.CARDS_BOUGHT,
) : Report

@Serializable
@SerialName("cardDeactivated")
data class CardDeactivatedReport(
    override val reportId: ReportId = ReportId.CARD_DEACTIVATED,
) : Report

@Serializable
@SerialName("cardEffectRoll")
data class CardEffectRollReport(
    override val reportId: ReportId = ReportId.CARD_EFFECT_ROLL,
) : Report

@Serializable
@SerialName("catchRoll")
data class CatchRollReport(
    override val reportId: ReportId = ReportId.CATCH_ROLL,
    val playerId: String,
    val successful: Boolean,
    val roll: Int,
    val minimumRoll: Int,
    val reRolled: Boolean,
    val rollModifiers: List<CatchModifier> = emptyList(),
    val bomb: Boolean,
) : Report

@Serializable
@SerialName("cloudBurster")
data class CloudBursterReport(
    override val reportId: ReportId = ReportId.CLOUD_BURSTER,
) : Report

@Serializable
@SerialName("chainsawRoll")
data class ChainsawRollReport(
    override val reportId: ReportId = ReportId.CHAINSAW_ROLL,
) : Report

@Serializable
@SerialName("coinThrow")
data class CoinThrowReport(
    override val reportId: ReportId = ReportId.COIN_THROW,
    val coach: String,
    val coinThrowHeads: Boolean,
    val coinChoiceHeads: Boolean,
) : Report

@Serializable
@SerialName("confusionRoll")
data class ConfusionRollReport(
    override val reportId: ReportId = ReportId.CONFUSION_ROLL,
) : Report

@Serializable
@SerialName("dauntlessRoll")
data class DauntlessRollReport(
    override val reportId: ReportId = ReportId.DAUNTLESS_ROLL,
) : Report

@Serializable
@SerialName("dedicatedFans")
data class DedicatedFansReport(
    override val reportId: ReportId = ReportId.DEDICATED_FANS,
) : Report

@Serializable
@SerialName("defectingPlayers")
data class DefectingPlayersReport(
    override val reportId: ReportId = ReportId.DEFECTING_PLAYERS,
) : Report

@Serializable
@SerialName("dodgeRoll")
data class DodgeRollReport(
    override val reportId: ReportId = ReportId.DODGE_ROLL,
) : Report

@Serializable
@SerialName("doubleHiredStaff")
data class DoubleHiredStaffReport(
    override val reportId: ReportId = ReportId.DOUBLE_HIRED_STAFF,
) : Report

@Serializable
@SerialName("doubleHiredStarPlayer")
data class DoubleHiredStarPlayerReport(
    override val reportId: ReportId = ReportId.DOUBLE_HIRED_STAR_PLAYER,
) : Report

@Serializable
@SerialName("escapeRoll")
data class EscapeRollReport(
    override val reportId: ReportId = ReportId.ESCAPE_ROLL,
) : Report

@Serializable
@SerialName("event")
data class EventReport(
    override val reportId: ReportId = ReportId.EVENT,
) : Report

@Serializable
@SerialName("fanFactor")
data class FanFactorReport(
    override val reportId: ReportId = ReportId.FAN_FACTOR,
    val teamId: String,
    val dedicatedFans: Int,
    val dedicatedFansRoll: Int,
    val dedicatedFansResult: Int,
) : Report

@Serializable
@SerialName("fanFactorRoll")
data class FanFactorRollReport(
    override val reportId: ReportId = ReportId.FAN_FACTOR_ROLL_POST_MATCH,
) : Report

@Serializable
@SerialName("foul")
data class FoulReport(
    override val reportId: ReportId = ReportId.FOUL,
) : Report

@Serializable
@SerialName("foulAppearanceRoll")
data class FoulAppearanceRollReport(
    override val reportId: ReportId = ReportId.FOUL_APPEARANCE_ROLL,
) : Report

@Serializable
@SerialName("freePettyCash")
data class FreePettyCashReport(
    override val reportId: ReportId = ReportId.FREE_PETTY_CASH,
) : Report

@Serializable
@SerialName("fumbblResultUpload")
data class FumbblResultUploadReport(
    override val reportId: ReportId = ReportId.FUMBBL_RESULT_UPLOAD,
) : Report

@Serializable
@SerialName("fumblerooskie")
data class FumblerooskieReport(
    override val reportId: ReportId = ReportId.FUMBLEROOSKIE,
) : Report

@Serializable
@SerialName("gameOptions")
data class GameOptionsReport(
    override val reportId: ReportId = ReportId.GAME_OPTIONS,
) : Report

@Serializable
@SerialName("goForItRoll")
data class GoForItRollReport(
    override val reportId: ReportId = ReportId.GO_FOR_IT_ROLL,
    val playerId: PlayerId,
    val successful: Boolean,
    val roll: Int,
    val minimumRoll: Int,
    val reRolled: Boolean,
) : Report

@Serializable
@SerialName("handOver")
data class HandOverReport(
    override val reportId: ReportId = ReportId.HAND_OVER,
) : Report

@Serializable
@SerialName("hitAndRun")
data class HitAndRunReport(
    override val reportId: ReportId = ReportId.HIT_AND_RUN,
) : Report

@Serializable
@SerialName("hypnoticGazeRoll")
data class HypnoticGazeRollReport(
    override val reportId: ReportId = ReportId.HYPNOTIC_GAZE_ROLL,
) : Report

@Serializable
@SerialName("indomitable")
data class IndomitableReport(
    override val reportId: ReportId = ReportId.INDOMITABLE,
) : Report

@Serializable
@SerialName("inducement")
data class InducementReport(
    override val reportId: ReportId = ReportId.INDUCEMENT,
) : Report

@Serializable
@SerialName("inducementsBought")
data class InducementsBoughtReport(
    override val reportId: ReportId = ReportId.INDUCEMENTS_BOUGHT,
) : Report

@Serializable
@SerialName("injury")
data class InjuryReport(
    override val reportId: ReportId = ReportId.INJURY,
    val defenderId: PlayerId,
    val injuryType: String,
    val armorBroken: Boolean,
    val armorRoll: List<Int>?,
    val injuryRoll: List<Int>?,
    val casualtyRoll: List<Int>?,
    val seriousInjury: String?, // Type?
    val casualtyRollDecay: String?,  // Type?
    val seriousInjuryDecay: String?, // Type?
    val seriousInjuryOld: String?, // Type?
    val injury: Int,
    val injuryDecay: String?, // Type?
    val attackerId: PlayerId?,
    val armorModifiers: List<String>,
    val injuryModifiers: List<String>,
    val casualtyModifiers: List<String>,
    val skipInjuryParts: String,
) : Report

@Serializable
@SerialName("interceptionRoll")
data class InterceptionRollReport(
    override val reportId: ReportId = ReportId.INTERCEPTION_ROLL,
) : Report

@Serializable
@SerialName("leapRoll")
data class LeapRollReport(
    override val reportId: ReportId = ReportId.JUMP_ROLL,
) : Report

@Serializable
@SerialName("jumpUpRoll")
data class JumpUpRollReport(
    override val reportId: ReportId = ReportId.JUMP_UP_ROLL,
) : Report

@Serializable
@SerialName("cheeringFans")
data class CheeringFansReport(
    override val reportId: ReportId = ReportId.KICKOFF_CHEERING_FANS,
) : Report

@Serializable
@SerialName("extraReRoll")
data class ExtraReRollReport(
    override val reportId: ReportId = ReportId.KICKOFF_EXTRA_RE_ROLL,
) : Report

@Serializable
@SerialName("kickoffOfficiousRef")
data class KickoffOfficiousRefReport(
    override val reportId: ReportId = ReportId.KICKOFF_OFFICIOUS_REF,
) : Report

@Serializable
@SerialName("kickoffPitchInvasion")
data class KickoffPitchInvasionReport(
    override val reportId: ReportId = ReportId.KICKOFF_PITCH_INVASION,
    val rollHome: Int,
    val rollAway: Int,
    val amount: Int,
    val playerIds: List<PlayerId>,
) : Report

@Serializable
@SerialName("kickoffResult")
data class KickoffResultReport(
    override val reportId: ReportId = ReportId.KICKOFF_RESULT,
    val kickoffResult: String,
    val kickoffRoll: List<Int>,
) : Report

@Serializable
@SerialName("kickoffRiot")
data class KickoffRiotReport(
    override val reportId: ReportId = ReportId.KICKOFF_RIOT,
) : Report

@Serializable
@SerialName("kickoffScatter")
data class KickoffScatterReport(
    override val reportId: ReportId = ReportId.KICKOFF_SCATTER,
    val ballCoordinateEnd: FieldCoordinate,
    val scatterDirection: Direction,
    val rollScatterDirection: Int,
    val rollScatterDistance: Int,
) : Report

@Serializable
@SerialName("kickoffSequenceActivationsCount")
data class KickoffSequenceActivationsCountReport(
    override val reportId: ReportId = ReportId.KICKOFF_SEQUENCE_ACTIVATIONS_COUNT,
) : Report

@Serializable
@SerialName("kickoffSequenceActivationsExhausted")
data class KickoffSequenceActivationsExhaustedReport(
    override val reportId: ReportId = ReportId.KICKOFF_SEQUENCE_ACTIVATIONS_EXHAUSTED,
) : Report

@Serializable
@SerialName("kickoffThrowARock")
data class KickoffThrowARockReport(
    override val reportId: ReportId = ReportId.KICKOFF_THROW_A_ROCK,
) : Report

@Serializable
@SerialName("kickoffTimeout")
data class KickoffTimeoutReport(
    override val reportId: ReportId = ReportId.KICKOFF_TIMEOUT,
) : Report

@Serializable
@SerialName("kickTeamMateFumble")
data class KickTeamMateFumbleReport(
    override val reportId: ReportId = ReportId.KICK_TEAM_MATE_FUMBLE,
) : Report

@Serializable
@SerialName("kickTeamMateRoll")
data class KickTeamMateRollReport(
    override val reportId: ReportId = ReportId.KICK_TEAM_MATE_ROLL,
) : Report

@Serializable
@SerialName("leader")
data class LeaderReport(
    override val reportId: ReportId = ReportId.LEADER,
) : Report

@Serializable
@SerialName("lookIntoMyEyesRoll")
data class LookIntoMyEyesRollReport(
    override val reportId: ReportId = ReportId.LOOK_INTO_MY_EYES_ROLL,
) : Report

@Serializable
@SerialName("masterChefRoll")
data class MasterChefRollReport(
    override val reportId: ReportId = ReportId.MASTER_CHEF_ROLL,
) : Report

@Serializable
@SerialName("modifiedDodgeResultSuccessful")
data class ModifiedDodgeResultSuccessfulReport(
    override val reportId: ReportId = ReportId.MODIFIED_DODGE_RESULT_SUCCESSFUL,
) : Report

@Serializable
@SerialName("modifiedPassResult")
data class ModifiedPassResultReport(
    override val reportId: ReportId = ReportId.MODIFIED_PASS_RESULT,
) : Report

@Serializable
@SerialName("mostValuablePlayers")
data class MostValuablePlayersReport(
    override val reportId: ReportId = ReportId.MOST_VALUABLE_PLAYERS,
) : Report

@Serializable
@SerialName("nervesOfSteel")
data class NervesOfSteelReport(
    override val reportId: ReportId = ReportId.NERVES_OF_STEEL,
) : Report

@Serializable
@SerialName("none")
data class NoneReport(
    override val reportId: ReportId = ReportId.NONE,
) : Report

@Serializable
@SerialName("noPlayersToField")
data class NoPlayersToFieldReport(
    override val reportId: ReportId = ReportId.NO_PLAYERS_TO_FIELD,
) : Report

@Serializable
@SerialName("officiousRefRoll")
data class OfficiousRefRollReport(
    override val reportId: ReportId = ReportId.OFFICIOUS_REF_ROLL,
) : Report

@Serializable
@SerialName("oldPro")
data class OldProReport(
    override val reportId: ReportId = ReportId.OLD_PRO,
) : Report

@Serializable
@SerialName("passBlock")
data class PassBlockReport(
    override val reportId: ReportId = ReportId.PASS_BLOCK,
) : Report

@Serializable
@SerialName("passDeviate")
data class PassDeviateReport(
    override val reportId: ReportId = ReportId.PASS_DEVIATE,
) : Report

@Serializable
@SerialName("passRoll")
data class PassRollReport(
    override val reportId: ReportId = ReportId.PASS_ROLL,
) : Report

@Serializable
@SerialName("penaltyShootout")
data class PenaltyShootoutReport(
    override val reportId: ReportId = ReportId.PENALTY_SHOOTOUT,
) : Report

@Serializable
@SerialName("pettyCash")
data class PettyCashReport(
    override val reportId: ReportId = ReportId.PETTY_CASH,
) : Report

@Serializable
@SerialName("pickMeUp")
data class PickMeUpReport(
    override val reportId: ReportId = ReportId.PICK_ME_UP,
) : Report

@Serializable
@SerialName("pickUpRoll")
data class PickUpRollReport(
    override val reportId: ReportId = ReportId.PICK_UP_ROLL,
    val playerId: String,
    val successful: Boolean,
    val roll: Int,
    val minimumRoll: Int,
    val reRolled: Boolean,
) : Report

@Serializable
@SerialName("pilingOn")
data class PilingOnReport(
    override val reportId: ReportId = ReportId.PILING_ON,
) : Report

@Serializable
@SerialName("placedBallDirection")
data class PlacedBallDirectionReport(
    override val reportId: ReportId = ReportId.PLACE_BALL_DIRECTION,
) : Report

@Serializable
@SerialName("playerAction")
data class PlayerActionReport(
    override val reportId: ReportId = ReportId.PLAYER_ACTION,
    val actingPlayerId: PlayerId,
    val playerAction: PlayerAction,
) : Report

@Serializable
@SerialName("playerEvent")
data class PlayerEventReport(
    override val reportId: ReportId = ReportId.PLAYER_EVENT,
) : Report

@Serializable
@SerialName("playCard")
data class PlayCardReport(
    override val reportId: ReportId = ReportId.PLAY_CARD,
) : Report

@Serializable
@SerialName("prayerAmount")
data class PrayerAmountReport(
    override val reportId: ReportId = ReportId.PRAYER_AMOUNT,
) : Report

@Serializable
@SerialName("prayerEnd")
data class PrayerEndReport(
    override val reportId: ReportId = ReportId.PRAYER_END,
) : Report

@Serializable
@SerialName("prayerRoll")
data class PrayerRollReport(
    override val reportId: ReportId = ReportId.PRAYER_ROLL,
) : Report

@Serializable
@SerialName("prayerWasted")
data class PrayerWastedReport(
    override val reportId: ReportId = ReportId.PRAYER_WASTED,
) : Report

@Serializable
@SerialName("projectileVomit")
data class ProjectileVomitReport(
    override val reportId: ReportId = ReportId.PROJECTILE_VOMIT,
) : Report

@Serializable
@SerialName("pumpUpTheCrowdReRoll")
data class PumpUpTheCrowdReRollReport(
    override val reportId: ReportId = ReportId.PUMP_UP_THE_CROWD_RE_ROLL,
) : Report

@Serializable
@SerialName("pumpUpTheCrowdReRollLost")
data class PumpUpTheCrowdReRollLostReport(
    override val reportId: ReportId = ReportId.PUMP_UP_THE_CROWD_RE_ROLLS_LOST,
) : Report

@Serializable
@SerialName("pushback")
data class PushbackReport(
    override val reportId: ReportId = ReportId.PUSHBACK,
    val defenderId: PlayerId,
    val pushbackMode: PushbackMode
) : Report

@Serializable
@SerialName("quickSnapRoll")
data class QuickSnapRollReport(
    override val reportId: ReportId = ReportId.QUICK_SNAP_ROLL,
) : Report

@Serializable
@SerialName("raidingParty")
data class RaidingPartyReport(
    override val reportId: ReportId = ReportId.RAIDING_PARTY,
) : Report

@Serializable
@SerialName("raiseDead")
data class RaiseDeadReport(
    override val reportId: ReportId = ReportId.RAISE_DEAD,
) : Report

@Serializable
@SerialName("receiveChoice")
data class ReceiveChoiceReport(
    override val reportId: ReportId = ReportId.RECEIVE_CHOICE,
    val teamId: String,
    val receiveChoice: Boolean,
) : Report

@Serializable
@SerialName("referee")
data class RefereeReport(
    override val reportId: ReportId = ReportId.REFEREE,
) : Report

@Serializable
@SerialName("regenerationRoll")
data class RegenerationRollReport(
    override val reportId: ReportId = ReportId.REGENERATION_ROLL,
) : Report

@Serializable
@SerialName("reRoll")
data class ReRollReport(
    override val reportId: ReportId = ReportId.RE_ROLL,
    val playerId: PlayerId,
    val reRollSource: String,
    val successful: Boolean,
    val roll: Int
) : Report

@Serializable
@SerialName("rightStuffRoll")
data class RightStuffRollReport(
    override val reportId: ReportId = ReportId.RIGHT_STUFF_ROLL,
) : Report

@Serializable
@SerialName("riotousRookies")
data class RiotousRookiesReport(
    override val reportId: ReportId = ReportId.RIOTOUS_ROOKIES,
) : Report

@Serializable
@SerialName("safeThrowRoll")
data class SafeThrowRollReport(
    override val reportId: ReportId = ReportId.SAFE_THROW_ROLL,
) : Report

@Serializable
@SerialName("scatterBall")
data class ScatterBallReport(
    override val reportId: ReportId = ReportId.SCATTER_BALL,
    val directionArray: List<Direction>,
    val rolls: List<Int>,
    val gustOfWind: Boolean,
) : Report

@Serializable
@SerialName("scatterPlayer")
data class ScatterPlayerReport(
    override val reportId: ReportId = ReportId.SCATTER_PLAYER,
) : Report

@Serializable
@SerialName("secretWeaponBan")
data class SecretWeaponBanReport(
    override val reportId: ReportId = ReportId.SECRET_WEAPON_BAN,
) : Report

@Serializable
@SerialName("selectBlitzTarget")
data class SelectBlitzTargetReport(
    override val reportId: ReportId = ReportId.SELECT_BLITZ_TARGET,
    val attackerId: PlayerId,
    val defenderId: PlayerId,
) : Report

@Serializable
@SerialName("selectGazeTarget")
data class SelectGazeTargetReport(
    override val reportId: ReportId = ReportId.SELECT_GAZE_TARGET,
) : Report

@Serializable
@SerialName("skillUse")
data class SkillUseReport(
    override val reportId: ReportId = ReportId.SKILL_USE,
    val playerId: PlayerId,
    val skill: String,
    val used: Boolean,
    val skillUse: String,
) : Report

@Serializable
@SerialName("skillUseOtherPlayer")
data class SkillUseOtherPlayerReport(
    override val reportId: ReportId = ReportId.SKILL_USE_OTHER_PLAYER,
) : Report

@Serializable
@SerialName("skillWasted")
data class SkillWastedReport(
    override val reportId: ReportId = ReportId.SKILL_WASTED,
) : Report

@Serializable
@SerialName("solidDefenceRoll")
data class SolidDefenceRollReport(
    override val reportId: ReportId = ReportId.SOLID_DEFENCE_ROLL,
) : Report

@Serializable
@SerialName("spectators")
data class SpectatorsReport(
    override val reportId: ReportId = ReportId.SPECTATORS,
) : Report

@Serializable
@SerialName("spellEffectRoll")
data class SpellEffectRollReport(
    override val reportId: ReportId = ReportId.SPELL_EFFECT_ROLL,
) : Report

@Serializable
@SerialName("stallerDetected")
data class StallerDetectedReport(
    override val reportId: ReportId = ReportId.STALLER_DETECTED,
) : Report

@Serializable
@SerialName("standUpRoll")
data class StandUpRollReport(
    override val reportId: ReportId = ReportId.STAND_UP_ROLL,
) : Report

@Serializable
@SerialName("startHalf")
data class StartHalfReport(
    override val reportId: ReportId = ReportId.START_HALF,
) : Report

@Serializable
@SerialName("swarmingPlayersRoll")
data class SwarmingPlayersRollReport(
    override val reportId: ReportId = ReportId.SWARMING_PLAYERS_ROLL,
) : Report

@Serializable
@SerialName("swoopPlayer")
data class SwoopPlayerReport(
    override val reportId: ReportId = ReportId.SWOOP_PLAYER,
) : Report

@Serializable
@SerialName("tentaclesShadowingRoll")
data class TentaclesShadowingRollReport(
    override val reportId: ReportId = ReportId.TENTACLES_SHADOWING_ROLL,
) : Report

@Serializable
@SerialName("thrownKeg")
data class ThrownKegReport(
    override val reportId: ReportId = ReportId.THROWN_KEG,
) : Report

@Serializable
@SerialName("throwAtStallingPlayer")
data class ThrowAtStallingPlayerReport(
    override val reportId: ReportId = ReportId.THROW_AT_STALLING_PLAYER,
) : Report

@Serializable
@SerialName("throwIn")
data class ThrowInReport(
    override val reportId: ReportId = ReportId.THROW_IN,
) : Report

@Serializable
@SerialName("throwTeamMateRoll")
data class ThrowTeamMateRollReport(
    override val reportId: ReportId = ReportId.THROW_TEAM_MATE_ROLL,
) : Report

@Serializable
@SerialName("timeoutEnforced")
data class TimeoutEnforcedReport(
    override val reportId: ReportId = ReportId.TIMEOUT_ENFORCED,
) : Report

@Serializable
@SerialName("trapDoor")
data class TrapDoorReport(
    override val reportId: ReportId = ReportId.TRAP_DOOR,
) : Report

@Serializable
@SerialName("turnEnd")
data class TurnEndReport(
    override val reportId: ReportId = ReportId.TURN_END,
    val playerIdTouchdown: PlayerId?,
    val heartRoll: Int = 0,

//    "playerIdTouchdown" : null,
//    "knockoutRecoveryArray" : [ ],
//"heatExhaustionArray" : [ ],
//"unzapArray" : [ ],
//"heatRoll" : 0

) : Report

@Serializable
@SerialName("twoForOne")
data class TwoForOneReport(
    override val reportId: ReportId = ReportId.TWO_FOR_ONE,
) : Report

@Serializable
@SerialName("weather")
data class WeatherReport(
    override val reportId: ReportId = ReportId.WEATHER,
    val weather: String,
    val weatherRoll: List<Int>,
) : Report

@Serializable
@SerialName("weatherMageResult")
data class WeatherMageResultReport(
    override val reportId: ReportId = ReportId.WEATHER_MAGE_RESULT,
) : Report

@Serializable
@SerialName("weatherMageRoll")
data class WeatherMageRollReport(
    override val reportId: ReportId = ReportId.WEATHER_MAGE_ROLL,
) : Report

@Serializable
@SerialName("weepingDaggerRoll")
data class WeepingDaggerRollReport(
    override val reportId: ReportId = ReportId.WEEPING_DAGGER_ROLL,
) : Report

@Serializable
@SerialName("winnings")
data class WinningsReport(
    override val reportId: ReportId = ReportId.WINNINGS,
) : Report

@Serializable
@SerialName("winningsRoll")
data class WinningsRollReport(
    override val reportId: ReportId = ReportId.WINNINGS_ROLL,
) : Report

@Serializable
@SerialName("wizardUse")
data class WizardUseReport(
    override val reportId: ReportId = ReportId.WIZARD_USE,
) : Report
