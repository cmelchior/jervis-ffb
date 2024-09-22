package dk.ilios.jervis.rules.skills

import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.procedures.DieRoll
import dk.ilios.jervis.procedures.UseStandardSkillReroll
import dk.ilios.jervis.procedures.UseTeamReroll
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

enum class DiceRollType {
    ACCURACY, // For passing
    ARMOUR,
    BAD_HABITS,
    BLOCK,
    BLITZ,
    BLOODLUST,
    BONE_HEAD,
    BOUNCE,
    BRILLIANT_COACHING,
    CASUALTY,
    CATCH,
    CHAINSAW,
    CHEERING_FANS,
    FAN_FACTOR,
    DEFLECTION,
    DODGE,
    DEVIATE,
    FOUl,
    HYPNOTIC_GAZE,
    INJURY,
    INTERCEPT,
    KICK_OFF_TABLE,
    LASTING_INJURY,
    LONER,
    OFFICIOUS_REF_FAN_FACTOR,
    OFFICIOUS_REF_REFEREE,
    PASS,
    PICKUP,
    PITCH_INVASION_FAN_FACTOR,
    PITCH_INVASION_PLAYERS_AFFECTED,
    PRAYERS_TO_NUFFLE,
    REALLY_STUPID,
    REGENERATION,
    PRO,
    QUICK_SNAP,
    RUSH,
    SCATTER,
    SOLID_DEFENSE,
    SWELTERING_HEAT,
    TAKE_ROOT,
    THROW_A_ROCK,
    THROW_TEAM_MATE,
    TREACHEROUS_TRAPDOOR,
    UNCHANNELLED_FURY,
    PROJECTILE_VOMIT,
    WEATHER,
}

@Serializable
sealed interface TeamReroll : RerollSource {
    val carryOverIntoOvertime: Boolean
    // When is this reroll removed from the Team, regardless of it being used or not
    val duration: Duration
    override val rerollProcedure: Procedure
        get() = UseTeamReroll

    override fun canReroll(
        type: DiceRollType,
        value: List<DieRoll<*>>,
        wasSuccess: Boolean?,
    ): Boolean {
        // TODO Some types cannot be rerolled
        return value.all { it.rerollSource == null }
    }

    override fun calculateRerollOptions(
        type: DiceRollType,
        value: List<DieRoll<*>>,
        wasSuccess: Boolean?,
    ): List<DiceRerollOption> {
        return listOf(DiceRerollOption(this, value))
    }
}

@Serializable
class RegularTeamReroll(val index: Int) : TeamReroll {
    override val id: RerollSourceId = RerollSourceId("team-reroll-$index")
    override val carryOverIntoOvertime: Boolean = true
    override val duration = Duration.PERMANENT
    override val rerollResetAt: Duration = Duration.END_OF_HALF
    override val rerollDescription: String = "Team reroll"
    override var rerollUsed: Boolean = false
}

@Serializable
class LeaderTeamReroll(val player: Player) : TeamReroll {
    override val id: RerollSourceId = RerollSourceId("leader-${player.id.value}")
    override val carryOverIntoOvertime: Boolean = true
    override val duration = Duration.SPECIAL
    override val rerollResetAt: Duration = Duration.END_OF_HALF
    override val rerollDescription: String = "Team reroll (Leader)"
    override var rerollUsed: Boolean = false
}

@Serializable
class BrilliantCoachingReroll(val team: Team) : TeamReroll {
    override val id: RerollSourceId = RerollSourceId("brilliant-coaching-${team.id.value}")
    override val carryOverIntoOvertime: Boolean = false
    override val duration = Duration.END_OF_DRIVE
    override val rerollResetAt: Duration = Duration.END_OF_HALF
    override val rerollDescription: String = "Team Reroll (Brilliant Coaching)"
    override var rerollUsed: Boolean = false
}

@Serializable
@JvmInline
value class RerollSourceId(val id: String)

// Should we split this into a "normal dice" and "block dice" interface?
interface RerollSource {
    val id: RerollSourceId // Unique identifier for this reroll. Should only be unique within a single team.
    val rerollResetAt: Duration
    val rerollDescription: String
    var rerollUsed: Boolean
    val rerollProcedure: Procedure

    // Returns `true` if `calculateRerollOptions` will return a non-empty list
    fun canReroll(type: DiceRollType, value: List<DieRoll<*>>, wasSuccess: Boolean? = null): Boolean

    fun calculateRerollOptions(
        // What kind of dice roll
        type: DiceRollType,
        // All dice part of the roll
        value: List<DieRoll<*>>,
        // If the roll was "successful" (as some skills only allow rerolls if unsuccessful). For some roll types
        // this concept doesn't make sense, like Block rolls or rolling for a table result.
        wasSuccess: Boolean? = null,
    ): List<DiceRerollOption>

    // Helper method, for just rolling a single dice. Which is by far, the most common scenario.
    fun calculateRerollOptions(type: DiceRollType, value: DieRoll<*>, wasSuccess: Boolean, ): List<DiceRerollOption> =
        calculateRerollOptions(
            type,
            listOf(value),
            wasSuccess,
        )
}

interface D6StandardSkillReroll : RerollSource {
    override val rerollProcedure: Procedure
        get() = UseStandardSkillReroll

    override fun calculateRerollOptions(
        type: DiceRollType,
        value: List<DieRoll<*>>,
        wasSuccess: Boolean?,
    ): List<DiceRerollOption> {
        // For standard skills
        if (value.size != 1) error("Unsupported number of dice: ${value.joinToString()}")
        return listOf(DiceRerollOption(this, value))
    }
}

@Serializable
data class DiceRerollOption(
    val source: RerollSource,
    val dice: List<DieRoll<*>>,
) {
    constructor(source: RerollSource, dieRoll: DieRoll<*>): this(source, listOf(dieRoll))
}

// When does the "used" state reset?
// TODO Rename to Duration?
enum class Duration {
    IMMEDIATE, // The effect expires immediately.
    START_OF_ACTIVATION, // The effect expires when the player is activated
    END_OF_ACTIVATION, // The effect expires at the end of the current players activation
    END_OF_TURN, // The effect expires at the end of the current teams turn.
    END_OF_DRIVE, // The effect expires at the end of the current drive
    END_OF_HALF, // The effect expires at the end of the current half
    END_OF_GAME, // The effect lasts for the entire game, but doesn't carry over to the next game
    SPECIAL, // The duration of this effect is too hard to put into a bucket and must be handled manually.
    STANDING_UP, // The effect expires when the player is going from prone to standing up.
    PERMANENT, // The effect is a permanent change to the team.
}

@Serializable
sealed interface Skill {
    // Unique identifier for this skill
    val skillId: String
    // Human readable name of this skill
    val name: String
    // Whether or not this skill is compulsory to use
    val compulsory: Boolean
    // Whether this skill count as being "used". The meaning of this is interpreted in the context it is used.
    // Note, this specifically does not apply to a "reroll" part of a skill.
    // See
    var used: Boolean
    // Represents any value in brackes, like Might Blow(1+) or Loner(4+). It is up to the context to correctly interpret this value
    val value: Int?
    // When the `used` state reset back to `false`?
    val resetAt: Duration
    // Which category does this skill belong to?
    val category: SkillCategory
    // Whether this skill works when the player has lost its tackle zones
    val workWithoutTackleZones: Boolean
    // Whether this skill works when the player is prone or stunned
    val workWhenProne: Boolean
    // Whether or not this skill is temporary
    val isTemporary: Boolean
    val expiresAt: Duration
}

// TODO Not really liking this API. Is there a good way to serialize them?
//  Also it doesn't look nice when naming the skill in the Position list
//  Ideally this `listOf(SureHands)`, not `listOf(SureHands.Factory)`
@Serializable
sealed interface SkillFactory {
    val value: Int?
    fun createSkill(isTemporary: Boolean = false, expiresAt: Duration = Duration.PERMANENT): Skill
}

interface SkillCategory {
    val id: Long
    val description: String
}

@Serializable
sealed interface BB2020Skill : Skill
