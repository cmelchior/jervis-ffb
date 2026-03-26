package com.jervisffb.engine.rules.common.skills

import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.PlayerKeyword
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.model.SkillValue
import kotlinx.serialization.Serializable

/**
 * This lists all known skills across all rulesets.
 *
 * Note, a type doesn't carry any meaning, it is just an identifier. The exact
 * behavior is determined by an implementation of the [Skill] interface or a
 * specific [Procedure].
 *
 * It is mostly a work-around, so we can easily detect when a new skill is added,
 * so we remember to update all locations.
 *
 * Skills with values are still only identified by their name here.
 * The unique combination of a type and value is defined by [SkillId].
 * These can be created by calling [SkillType.id].
 *
 * @see SkillSettings
 */
@Serializable
enum class SkillType(val description: String) {

    // Currently all values are sorted after their BB2025 categories. Skills
    // not available in BB2025 are placed in the category for ther version.

    // Agility
    CATCH("Catch"),
    DEFENSIVE("Defensive"),
    DIVING_CATCH("Diving Catch"),
    DIVING_TACKLE("Diving Tackle"),
    DODGE("Dodge"),
    HIT_AND_RUN("Hit and Run"),
    JUMP_UP("Jump Up"),
    LEAP("Leap"),
    SAFE_PAIR_OF_HANDS("Safe Pair of Hands"),
    SIDESTEP("Sidestep"),
    SPRINT("Sprint"),
    SURE_FEET("Sure Feet"),

    // Devious
    DIRTY_PLAYER("Dirty Player"),
    EYE_GOUGE("Eye Gouge"),
    FUMBLEROOSKI("Fumblerooski"),
    LETHAL_FLIGHT("Lethal Flight"),
    LONE_FOULER("Lone Fouler"),
    PILE_DRIVER("Pile Driver"),
    PUT_THE_BOOT_IN("Put the Boot in"),
    QUICK_FOUL("Quick Foul"),
    SABOTEUR("Saboteur"),
    SHADOWING("Shadowing"),
    SNEAKY_GIT("Sneaky Git"),
    VIOLENT_INNOVATOR("Violent Innovator"),

    // General
    BLOCK("Block"),
    DAUNTLESS("Dauntless"),
    FEND("Fend"),
    FRENZY("Frenzy"),
    KICK("Kick"),
    PRO("Pro"),
    STEADY_FOOTING("Steady Footing"),
    STRIP_BALL("Strip ball"),
    SURE_HANDS("Sure Hands"),
    TACKLE("Tackle"),
    TAUNT("Taunt"),
    WRESTLE("Wrestle"),

    // Mutations
    BIG_HAND("Big Hand"),
    CLAWS("Claws"),
    DISTURBING_PRESENCE("Disturbing Presence"),
    EXTRA_ARMS("Extra Arms"),
    FOUL_APPEARANCE("Foul Appearance"),
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
    GIVE_AND_GO("Give and Go"),
    HAIL_MARY_PASS("Hail Mary Pass"),
    LEADER("Leader"),
    NERVES_OF_STEEL("Nerves of Steel"),
    ON_THE_BALL("On the Ball"),
    PASS("Pass"),
    PUNT("Punt"),
    RUNNING_PASS("Running Pass"), // BB2020 Only
    SAFE_PASS("Safe Pass"),

    // Strength
    ARM_BAR("Arm Bar"),
    BRAWLER("Brawler"),
    BREAK_TACKLE("Break Tackle"),
    BULLSEYE("Bullseye"),
    GRAB("Grab"),
    GUARD("Guard"),
    JUGGERNAUT("Juggernaut"),
    MIGHTY_BLOW("Mighty Blow"),
    MULTIPLE_BLOCK("Multiple Block"),
    STAND_FIRM("Stand Firm"),
    STRONG_ARM("Strong Arm"),
    THICK_SKULL("Thick Skull"),

    // Traits
    ALWAYS_HUNGRY("Always Hungry"),
    ANIMAL_SAVAGERY("Animal Savagery"),
    ANIMOSITY("Animosity"),
    BALL_AND_CHAIN("Ball & Chain"),
    BLOOD_LUST("Blood Lust"), // Reference missing
    BOMBARDIER("Bombardier"),
    BONE_HEAD("Bone Head"),
    BREATHE_FIRE("Breathe Fire"), // Reference missing
    CHAINSAW("Chainsaw"),
    DECAY("Decay"),
    DRUNKARD("Drunkard"),
    HATRED("Hatred"),
    HYPNOTIC_GAZE("Hypnotic Gaze"),
    INSIGNIFICANT("Insignificant"),
    KICK_TEAMMATE("Kick Team-mate"),
    LONER("Loner"),
    MY_BALL("My Ball"),
    NO_BALL("No Ball"),
    NO_HANDS("No Hands"),
    PICK_ME_UP("Pick-me-up"),
    PLAGUE_RIDDEN("Plague Ridden"),
    POGO_STICK("Pogo"),
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
    THROW_TEAMMATE("Throw Team-mate"),
    TIMMMBER("Timmm-ber!"),
    TITCHY("Titchy"),
    TRICKSTER("Trickster"),
    UNCHANNELLED_FURY("Unchannelled Fury"),
    UNSTEADY("Unsteady"),

    // Special Rules (Star Players)
    SNEAKIEST_OF_THE_LOT("Sneakiest of the Lot");

    /**
     * Creates an identifier for a skill type including its value. This is a way
     * we can uniquely identify the abstract idea of a skill, without
     * associating it with a player or the specific ruleset.
     *
     * I.e. "Pro" might mean different things depending on the ruleset, but the
     * identifier remains the same.
     *
     * This method is specifically designed to be used by rosters as it makes it
     * possible to define rosters while still sharing them between different
     * rulesets.
     *
     * Design note: The naming of this method is intentionally kept short
     * to make skill lists in rosters more readable.
     */
    fun id(value: Int): SkillId = SkillId(this, SkillValue.Int(value))
    fun id(value: PlayerKeyword): SkillId = SkillId(this, SkillValue.Keyword(value))
    fun id(value: Any? = null): SkillId = SkillId(this, SkillValue.None)
}
