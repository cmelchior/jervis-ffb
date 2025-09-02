package com.jervisffb.engine.rules.common.skills

import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.SkillId
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
    fun id(value: Int? = null): SkillId = SkillId(this, value)
}
