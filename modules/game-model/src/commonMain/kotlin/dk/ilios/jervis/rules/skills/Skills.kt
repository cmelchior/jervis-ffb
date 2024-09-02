package dk.ilios.jervis.rules.skills

import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.procedures.DieRoll
import dk.ilios.jervis.procedures.UseStandardSkillReroll
import dk.ilios.jervis.procedures.UseTeamReroll
import kotlinx.serialization.Serializable

enum class DiceRollType {
    ACCURACY, // For passing
    ARMOUR,
    BLOCK,
    BLOODLUST,
    BONE_HEAD,
    BOUNCE,
    CASUALTY,
    CATCH,
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
    PASS,
    PICKUP,
    REALLY_STUPID,
    REGENERATION,
    PRO,
    RUSH,
    SCATTER,
    SWELTERING_HEAT,
    TAKE_ROOT,
    THROW_TEAM_MATE,
    WEATHER,
    WILD_ANIMAL,
}

enum class TeamRerollDuration {
    END_OF_HALF,
    END_OF_DRIVE,
}

sealed interface TeamReroll : RerollSource {
    val carryOverIntoOvertime: Boolean
    val isTemporary: Boolean
    override val rerollProcedure: Procedure
        get() = UseTeamReroll

    override fun canReroll(
        type: DiceRollType,
        value: List<DieRoll<*, *>>,
        wasSuccess: Boolean?,
    ): Boolean {
        // TODO Some types cannot be rerolled
        return value.all { it.rerollSource == null }
    }

    override fun calculateRerollOptions(
        type: DiceRollType,
        value: List<DieRoll<*, *>>,
        wasSuccess: Boolean?,
    ): List<DiceRerollOption> {
        return listOf(DiceRerollOption(this, value))
    }
}

class RegularTeamReroll(val team: Team) : TeamReroll {
    override val carryOverIntoOvertime: Boolean = true
    override val isTemporary: Boolean = false
    override val rerollResetAt: ResetPolicy = ResetPolicy.END_OF_HALF
    override val rerollDescription: String = "Team reroll"
    override var rerollUsed: Boolean = false
}

class LeaderTeamReroll(val player: Player) : TeamReroll {
    override val carryOverIntoOvertime: Boolean = true
    override val isTemporary: Boolean = true
    override val rerollResetAt: ResetPolicy = ResetPolicy.END_OF_HALF
    override val rerollDescription: String = "Team reroll (Leader)"
    override var rerollUsed: Boolean = false
}

// Should we split this into a "normal dice" and "block dice" interface?
interface RerollSource {
    val rerollResetAt: ResetPolicy
    val rerollDescription: String
    var rerollUsed: Boolean
    val rerollProcedure: Procedure

    fun canReroll(
        type: DiceRollType,
        value: List<DieRoll<*, *>>,
        wasSuccess: Boolean? = null,
    ): Boolean

    fun calculateRerollOptions(
        type: DiceRollType,
        value: List<DieRoll<*, *>>,
        wasSuccess: Boolean? = null,
    ): List<DiceRerollOption>

    fun calculateRerollOptions(
        type: DiceRollType,
        value: DieRoll<*, *>,
        wasSuccess: Boolean,
    ): List<DiceRerollOption> =
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
        value: List<DieRoll<*, *>>,
        wasSuccess: Boolean?,
    ): List<DiceRerollOption> {
        // For standard skills
        if (value.size != 1) error("Unsupported number of dice: ${value.joinToString()}")
        return listOf(DiceRerollOption(this, value))
    }
}

// `rerollSource` is not set yet
@Serializable
data class DiceRerollOption(
    val source: RerollSource,
    val dice: List<DieRoll<*, *>>,
)

// When does the "used" state reset?
enum class ResetPolicy {
    NEVER,
    END_OF_TURN,
    END_OF_DRIVE,
    END_OF_HALF,
    SPECIAL,
}

@Serializable
sealed interface Skill {
    // Unique identifier for this skill
    val id: String
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
    val resetAt: ResetPolicy
    // Which category does this skill belong to?
    val category: SkillCategory
    // Whether this skill works when the player has lost its tackle zones
    val workWithoutTackleZones: Boolean
    // Whether this skill works when the player is prone or stunned
    val workWhenProne: Boolean
}

// TODO Find a better way to serialize these,
//  so it also works well when naming the skill in the Position list
//  Ideally this `listOf(SureHands)`, not `listOf(SureHands.Factory)`
@Serializable
sealed interface SkillFactory {
    fun createSkill(): Skill
}

interface SkillCategory {
    val id: Long
    val name: String
}

@Serializable
sealed interface BB2020Skill : Skill

enum class BB2020SkillsList {
//
// Agility
//    - Catch
//    - Diving Catch
//    - Diving Tackle
//    - Dodge
//    - Defensive
//    - Jump Up
//    - Leap
//    - Safe Pair of Hands
//    - Sidestep
//    - Sneaky Git
//    - Sprint
//    - Sure Feet
//
// General
//    - Block
//    - Dauntless
//    - Dirty Player (+1)
//    - Fend
//    - Frenzy*
//    - Kick
//    - Pro
//    - Shadowing
//    - Strip Ball
//    - Sure Hands
//    - Tackle
//    - Wrestle
//
// Passing
//    - Accurate
//    - Cannoneer
//    - Cloud Burster
//    - Dump-off
//    - Fumblerooskie
//    - Hail Mary Pass
//    - Leader
//    - Nerves of Steel
//    - On the Ball
//    - Pass
//    - Running Pass
//    - Safe Pass
//
// Strength
//    - Arm Bar
//    - Brawler
//    - Break Tackle
//    - Grab
//    - Guard
//    - Juggernaut
//    - Might Blow (+1)
//    - Multiple Block
//    - Pile Driver
//    - Stand Firm
//    - Strong Arm
//    - Thick Skull
//
// Traits
//    - Animal Savagery*
//    - Animosity*
//    - Always Hungry*
//    - Ball & Chain*
//    - Bombadier
//    - Bone Head*
//    - Chainsaw*
//    - Decay*
//    - Hypnotic Gaze
//    - Kick Team-mate
//    - Loner (X+)*
//    - No Hands*
//    - Plague Ridden
//    - Pogo Stick
//    - Projectile Vomit
//    - Really Stupid*
//    - Regeneration
//    - Right Stuff*
//    - Secret Weapon*
//    - Stab
//    - Stunty*
//    - Swarming
//    - Swoop
//    - Take Root*
//    - Titchy*
//    - Timmm-ber!
//    - Throw Team-mate
//    - Unchannelled Fury*
}
