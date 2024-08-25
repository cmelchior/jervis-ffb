package dk.ilios.jervis.rules.skills

import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.procedures.DieRoll
import dk.ilios.jervis.procedures.UseStandardSkillReroll
import dk.ilios.jervis.procedures.UseTeamReroll
import kotlinx.serialization.Serializable

public object DiceRoll {
    val AMOUR = DiceRollType.ArmourRoll
    val BLOCK = DiceRollType.BlockRoll
    val CASULTY = DiceRollType.CasultyRoll
    val CATCH = DiceRollType.CatchRoll
    val INJURY = DiceRollType.InjuryRoll
    val PICKUP = DiceRollType.PickUpRoll
    val PRO = DiceRollType.ProRoll
    val WEATHER = DiceRollType.WeatherRoll
}

// Enumerate all different roll types
sealed interface DiceRollType {
    data object ArmourRoll : DiceRollType
    data object BlockRoll : DiceRollType
    // data object BloodLustRoll: DiceRollType
    // data object BoneHeadRoll: DiceRollType
    data object CasultyRoll : DiceRollType
    data object CatchRoll : DiceRollType
    // data object DodgeRoll: DiceRollType
    // data object FoulRoll: DiceRollType
    // data object HypnoticGazeRoll: DiceRollType
    data object InjuryRoll: DiceRollType
    // data object InterceptRoll: DiceRollType
    // data object KickOffTableRoll: DiceRollType
    // data object LonerRoll: DiceRollType
    // data object PassRoll: DiceRollType
    data object PickUpRoll : DiceRollType
    // data object ReallyStupidRoll: DiceRollType
    // data object RegenerationRoll: DiceRollType
    data object ProRoll : DiceRollType
    // data object RushRoll: DiceRollType
    // data object TakeRootRoll: DiceRollType
    // data object ThrowTeamMateRoll: DiceRollType
    data object WeatherRoll : DiceRollType
    // data object WildAnimalRoll: DiceRollType
    // data class CustomRoll(val id: String): DiceRollType
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
    override val rerollDescription: String = "Team reroll"
    override var rerollUsed: Boolean = false
}

class LeaderTeamReroll(val player: Player) : TeamReroll {
    override val carryOverIntoOvertime: Boolean = true
    override val isTemporary: Boolean = true
    override val rerollDescription: String = "Team reroll (Leader)"
    override var rerollUsed: Boolean = false
}

// Should we split this into a "normal dice" and "block dice" interface?
interface RerollSource {
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

@Serializable
sealed interface Skill {
    companion object {
        const val NO_LIMIT = -1
    }

    enum class ResetPolicy {
        NEVER,
        END_OF_TURN,
        END_OF_DRIVE,
        END_OF_HALF,
        SPECIAL,
    }

    val id: String
    val name: String
    val limit: Int
    var used: Int
    val resetAt: ResetPolicy
    val category: SkillCategory

    fun isAvailable(): Boolean {
        return used < limit
    }
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

class BB2016Skills {
// Agility
//    - Catch
//    - Diving Catch
//    - Diving Tackle
//    - Dodge
//    - Jump Up
//    - Leap
//    - Side Step
//    - Sneaky Git
//    - Sprint
//    - Sure Feet
//
// General
//    - Block
//    - Dauntless
//    - Dirty Player
//    - Fend
//    - Frenzy
//    - Kick
//    - Kick-off Return
//    - Pass Block
//    - Pro
//    - Shadowing
//    - Strip Ball
//    - Sure Hands
//    - Tackle
//    - Wrestle
//
// Mutations
//    - Big Hand
//    - Claw/Claws
//    - Disturbing Presence
//    - Extra Arms
//    - Foul Appearance
//    - Horns
//    - Prehensile Tail
//    - Tentacles
//    - Two Heads
//    - Very Long Legs
//
// Passing
//    - Accurate
//    - Dump-off
//    - Hail Mary Pass
//    - Leader
//    - Nerves of Steel
//    - Pass
//    - Safe Pass
//
// Strength
//    - Break Tackle
//    - Grab
//    - Guard
//    - Juggernaut
//    - Might Blow
//    - Multiple Block
//    - Stand Firm
//    - Strong Arm
//    - Thick Skull
//
// Extraordinary
//    - Always Hungry
//    - Bone Head
//    - Loner
//    - Really Stupid
//    - Regeneration
//    - Right Stuff
//    - Stunty
//    - Throw Team-mate
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
