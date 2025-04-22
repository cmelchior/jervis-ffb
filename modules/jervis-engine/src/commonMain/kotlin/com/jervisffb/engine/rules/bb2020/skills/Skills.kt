package com.jervisffb.engine.rules.bb2020.skills

import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.RerollSourceId
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.PlayerSpecialActionType
import com.jervisffb.engine.rules.bb2020.procedures.DieRoll
import com.jervisffb.engine.rules.bb2020.procedures.UseStandardSkillReroll
import com.jervisffb.engine.rules.bb2020.procedures.UseTeamReroll
import kotlinx.serialization.Serializable

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
        return listOf(DiceRerollOption(this.id, value))
    }
}

class RegularTeamReroll(val teamId: TeamId, val index: Int) : TeamReroll {
    override val id: RerollSourceId = RerollSourceId("${teamId.value}-reroll-$index")
    override val carryOverIntoOvertime: Boolean = true
    override val duration = Duration.PERMANENT
    override val rerollResetAt: Duration = Duration.END_OF_HALF
    override val rerollDescription: String = "Team reroll"
    override var rerollUsed: Boolean = false
}

class LeaderTeamReroll(val player: PlayerId) : TeamReroll {
    override val id: RerollSourceId = RerollSourceId("${player.value}-leader")
    override val carryOverIntoOvertime: Boolean = true
    override val duration = Duration.SPECIAL
    override val rerollResetAt: Duration = Duration.END_OF_HALF
    override val rerollDescription: String = "Team reroll (Leader)"
    override var rerollUsed: Boolean = false
}

class BrilliantCoachingReroll(val teamId: TeamId) : TeamReroll {
    override val id: RerollSourceId = RerollSourceId("${teamId.value}-brilliant-coaching")
    override val carryOverIntoOvertime: Boolean = false
    override val duration = Duration.END_OF_DRIVE
    override val rerollResetAt: Duration = Duration.END_OF_HALF
    override val rerollDescription: String = "Team Reroll (Brilliant Coaching)"
    override var rerollUsed: Boolean = false
}

// Should we split this into a "normal dice" and "block dice" interface?
interface RerollSource {
    val id: RerollSourceId // Unique identifier for this reroll. Should be unique across both teams.
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
    fun calculateRerollOptions(type: DiceRollType, value: DieRoll<*>, wasSuccess: Boolean?): List<DiceRerollOption> =
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
        return listOf(DiceRerollOption(this.id, value))
    }
}

/**
 * Interface for skills that provide a special player action.
 *
 * Note, for skills that replace blocks, the procedure being referenced is the Standalone
 * variant. Special skills that can be used as part of a Multiple Block
 * are defined in [com.jervisffb.engine.rules.bb2020.procedures.actions.block.MultipleBlockContext] and will
 * be handled separately there.
 */
interface SpecialActionProvider {
    val specialAction: PlayerSpecialActionType
    var isSpecialActionUsed: Boolean
}

@Serializable
data class DiceRerollOption(
    val rerollId: RerollSourceId,
    val dice: List<DieRoll<*>>,
) {
    constructor(rerollId: RerollSourceId, dieRoll: DieRoll<*>): this(rerollId, listOf(dieRoll))
    constructor(source: RerollSource, dieRoll: DieRoll<*>): this(source.id, listOf(dieRoll))

    fun getRerollSource(game: Game): RerollSource {
        return game.getRerollSourceById(rerollId)
    }
}

// When does the "used" state reset?
enum class Duration {
    IMMEDIATE, // The effect expires immediately.
    START_OF_ACTIVATION, // The effect expires when the player is activated
    END_OF_ACTIVATION, // The effect expires at the end of the current players activation
    END_OF_ACTION, // The effect expires at the end of the action, this also includes subactions, like the Block part of a Blitz.
    END_OF_TURN, // The effect expires at the end of the current teams turn.
    END_OF_DRIVE, // The effect expires at the end of the current drive
    END_OF_HALF, // The effect expires at the end of the current half
    END_OF_GAME, // The effect lasts for the entire game, but doesn't carry over to the next game
    SPECIAL, // The duration of this effect is too hard to put into a bucket and must be handled manually.
    STANDING_UP, // The effect expires when the player is going from prone to standing up.
    PERMANENT, // The effect is a permanent change to the team.
}

/**
 * This enum enumerate all known skills across all rulesets.
 *
 * It is mostly a work-around so we can easily detect when a new skill is added so
 * we remember to update all locations.
 *
 * Skills with values are still only identified by their name here.
 * The unique combintation of a type and value is defined by [SkillId].
 * These can be created by calling [SkillType.id].
 **
 * @see com.jervisffb.engine.rules.bb2020.SkillSettings
 */
@Serializable
enum class SkillType(val description: String) {
    // Agility
    CATCH("Catch"),
    DIVING_CATCH("Diving Catch"),
    DIVING_TACKLE("Diving Tackle"),
    DODGE("Dodge"),
    DEFENSIVE("Defensive"),
    JUMP_UP("Jump Up"),
    LEAP("Leap"),
    SAFE_PAIR_OF_HANDS("Safe Pair of Hands"),
    SIDESTEP("Sidestep"),
    SNEAKY_GIT("Sneaky Git"),
    SPRINT("Sprint"),
    SURE_FEET("Sure Feet"),

    // General
    BLOCK("Block"),
    DAUNTLESS("Dauntless"),
    DIRTY_PLAYER("Dirty Player"),
    FEND("Fend"),
    FRENZY("Frenzy"),
    KICK("Kick"),
    PRO("Pro"),
    SHADOWING("Shadowing"),
    STRIP_BALL("Strip ball"),
    SURE_HANDS("Sure Hands"),
    TACKLE("Tackle"),
    WRESTLE("Wrestle"),

    // Mutations
    BIG_HAND("Big Hand"),
    CLAWS("Claws"),
    DISTURBING_PRESENCE("Disturbing Presence"),
    EXTRA_ARMS("Extra Arms"),
    FOUL_APPEARANCE("Full Appearance"),
    HORNS("Horns"),
    IRON_HARD_SKIN("Iron Hard Skin"),
    MONSTROUS_MOUTH("Monstrous Mouth"),
    PREHENSILE_TAIL("Prehensile Tail"),
    TENTACLE("Tentacle"),
    TWO_HEADS("Two Heads"),
    VERY_LONG_LEGS("Very Long Legs"),

    // Passing
    ACCURATE("Accurate"),
    CANNONEER("Cannoneer"),
    CLOUD_BURSTER("Cloud Burster"),
    DUMP_OFF("Dump-off"),
    FUMBLEROOSKIE("Fumblerooskie"),
    HAIL_MARY_PASS("Hail Mary Pass"),
    LEADER("Leader"),
    NERVES_OF_STEEL("Nerves of Steel"),
    ON_THE_BALL("On the Ball"),
    PASS("Pass"),
    RUNNING_PASS("Running Pass"),
    SAFE_PASS("Safe Pass"),

    // Strength
    ARM_BAR("Arm Bar"),
    BRAWLER("Brawler"),
    BREAK_TACKLE("Break Tackle"),
    GRAB("Grab"),
    GUARD("Guard"),
    JUGGERNAUT("Juggernaut"),
    MIGHTY_BLOW("Mighty Blow"),
    MULTIPLE_BLOCK("Multiple Block"),
    PILE_DRIVER("Pile Driver"),
    STAND_FIRM("Stand Firm"),
    STRONG_ARM("Strong Arm"),
    THICK_SKULL("Thick Skull"),

    // Traits
    ANIMAL_SAVAGERY("Animal Savagery"),
    ANIMOSITY("Animosity"),
    ALWAYS_HUNGRY("Always Hungry"),
    BALL_AND_CHAIN("Ball & Chain"),
    BOMBARDIER("Bombardier"),
    BONE_HEAD("Bone Head"),
    BLOOD_LUST("Blood Lust"), // Reference missing
    BREATHE_FIRE("Breathe Fire"), // Reference missing
    CHAINSAW("Chainsaw"),
    DECAY("Decay"),
    HIT_AND_RUN("Hit and Run"),
    HYPNOTIC_GAZE("Hypnotic Gaze"),
    KICK_TEAMMATE("Kick Team-mate"),
    LONER("Loner"),
    NO_HANDS("No Hands"),
    PLAGUE_RIDDEN("Plague Ridden"),
    POGO_STICK("Pojo Stick"),
    PROJECTILE_VOMIT("Projectile Vomit"),
    REALLY_STUPID("Really Stupid"),
    REGENERATION("Regeneration"),
    RIGHT_STUFF("Right Stuff"),
    SECRET_WEAPON("Secret Weapon"),
    STAB("Stab"),
    STUNTY("Stunty"),
    SWARMING("Swarming"),
    SWOOP("Swoop"),
    TAKE_ROOT("Take Root"),
    TITCHY("Titchy"),
    TIMMMBER("Timmm-ber!"),
    THROW_TEAMMATE("Throw Team-mate"),
    UNCHANNELLED_FURY("Unchannelled Fury"),

    // Special Rules (Star Players)
    SNEAKIEST_OF_THE_LOT("Sneakiest of the Lot");

    /**
     * Creates an identifier for a skill type including its value.
     * This is a way we can uniquely identify the abstract idea of a
     * a skill within a ruleset, without associating it with a player
     * or the specific ruleset.
     *
     * I.e. "Pro" might mean different things depending on the ruleset,
     * but the identifier remain the same.
     *
     * This method is specifically designed to be used by rosters as it
     * makes it possible to define rosters while still sharing them
     * between different rulesets.
     *
     * Design note: The naming of this method is intentionally kept short
     * to make skill lists in rosters more readable
     */
    fun id(value: Int? = null): SkillId = SkillId(this, value)
}

/**
 * This interface represents player Skills. Since these skills are stateful
 * they are required to have a [skillId] that is unique across the entire game.
 */
interface Skill {
    // The player this skill is assigne to
    val player: Player
    // Unique identifier for the skill
    // The same skill across multiple players have the same id,
    // so use `player.id + skillId` to uniquely identify a skill
    val skillId: SkillId
    // Skill type, effectively the same as checking the KClass, but enums
    // make it easier to enumerate all options.
    val type: SkillType
    // Represents any value in brackes, like Might Blow(1+) or Loner(4+). It is up to the context to correctly
    // interpret this value
    val value: Int?
    // Which category does this skill belong to?
    val category: SkillCategory
    // Human readable name of this skill
    val name: String
    // Whether this skill is compulsory to use
    val compulsory: Boolean
    // Whether this skill count as being "used". The meaning of this is interpreted in the context it is used.
    // If the skill is always available, this should always be false.
    // Note, this specifically does not apply to a "reroll" part of a skill.
    var used: Boolean
    // When the `used` state reset back to `false`?
    val resetAt: Duration
    // Whether this skill works when the player has lost its tackle zones
    val workWithoutTackleZones: Boolean
    // Whether this skill works when the player is prone or stunned
    val workWhenProne: Boolean
    // If the skill is temporary, this defines when the skill expires and is removed
    val expiresAt: Duration
    // Whether this skill is temporary (removed at latest and end of game) or not
    val isTemporary: Boolean
        get() = (expiresAt != Duration.PERMANENT)
}

/**
 * All Skill Categories across all rulesets. Whether or not they are supported
 * is defined by the relevant [com.jervisffb.engine.rules.bb2020.SkillSettings]
 */
enum class SkillCategory(val description: String) {
    AGILITY("Agility"),
    GENERAL("General"),
    MUTATIONS("Mutations"),
    PASSING("Passing"),
    STRENGTH("Strength"),
    TRAITS("Traits"),
    SPECIAL_RULES("Special Rules"),
}

sealed interface BB2020Skill : Skill
