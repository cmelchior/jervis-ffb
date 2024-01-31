package dk.ilios.jervis.rules.skills

import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.procedures.UseTeamReroll

public object DiceRoll {
    val CATCH = DiceRollType.CatchRoll
}


sealed interface DiceRollType {
    //data object ArmourRoll: DiceRollType
//data object BloodLustRoll: DiceRollType
//data object BoneHeadRoll: DiceRollType
//data object CasultyRoll: DiceRollType
    data object CatchRoll : DiceRollType

    //data object DodgeRoll: DiceRollType
//data object FoulRoll: DiceRollType
//data object HypnoticGazeRoll: DiceRollType
//data object InjuryRoll: DiceRollType
//data object InterceptRoll: DiceRollType
//data object KickOffTableRoll: DiceRollType
//data object LonerRoll: DiceRollType
//data object PassRoll: DiceRollType
//data object PickUpRoll: DiceRollType
//data object ReallyStupidRoll: DiceRollType
//data object RegenerationRoll: DiceRollType
    data object ProRoll : DiceRollType
//data object RushRoll: DiceRollType
//data object TakeRootRoll: DiceRollType
//data object ThrowTeamMateRoll: DiceRollType
//data object WeatherRoll: DiceRollType
//data object WildAnimalRoll: DiceRollType
//data class CustomRoll(val id: String): DiceRollType

}
enum class TeamRerollDuration {
    END_OF_HALF,
    END_OF_DRIVE
}

sealed interface TeamReroll: RerollSource {
    val carryOverIntoOvertime: Boolean
    val isTemporary: Boolean
    override val rerollProcedure: Procedure
        get() = UseTeamReroll
}

class RegularTeamReroll(val team: Team): TeamReroll {
    override var rerollUsed: Boolean = false
    override val carryOverIntoOvertime: Boolean = true
    override val isTemporary: Boolean = false
    override val rerollDescription: String = "Team reroll"
    override fun canReroll(type: DiceRollType, wasSuccess: Boolean): Boolean {
        return true // TODO Some rolls cannot be re-rolled
    }
}

class LeaderTeamReroll(val player: Player): TeamReroll {
    override var rerollUsed: Boolean = false
    override val carryOverIntoOvertime: Boolean = true
    override val isTemporary: Boolean = true
    override val rerollDescription: String = "Team reroll (Leader)"
    override fun canReroll(type: DiceRollType, wasSuccess: Boolean): Boolean {
        return true
    }
}

//enum class TeamRerollType {
//    REGULAR,
//    LEADER,
//    END_OF_DRIVE,
//    END_OF_HALF,
//    END_OF_GAME
//}

interface RerollSource {
    val rerollDescription: String
    var rerollUsed: Boolean
    val rerollProcedure: Procedure
    fun canReroll(type: DiceRollType, wasSuccess: Boolean): Boolean
}

interface Skill: RerollSource {
    enum class UsageType {
        ALWAYS,
        ONCE_PR_TURN,
        ONCE_PR_DRIVE,
        ONCE_PR_HALF,
        ONCE_PR_GAME,
        SPECIAL //
    }
    val id: Long
    val name: String
    val usage: UsageType
    val category: SkillCategory
}

interface SkillCategory {
    val id: Long
    val name: String
}

class BB2016Skills {
//Agility
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
//General
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
//Mutations
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
//Passing
//    - Accurate
//    - Dump-off
//    - Hail Mary Pass
//    - Leader
//    - Nerves of Steel
//    - Pass
//    - Safe Pass
//
//Strength
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
//Extraordinary
//    - Always Hungry
//    - Bone Head
//    - Loner
//    - Really Stupid
//    - Regeneration
//    - Right Stuff
//    - Stunty
//    - Throw Team-mate
}

sealed interface BB2020Skill: Skill


enum class BB2020SkillsList {
//
//Agility
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
//General
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
//Passing
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
//Strength
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
//Traits
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
