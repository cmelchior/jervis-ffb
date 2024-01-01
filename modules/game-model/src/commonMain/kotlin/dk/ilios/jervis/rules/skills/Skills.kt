package dk.ilios.jervis.rules.skills

interface Skill {

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
    var used: Boolean

    // Modifiers


}

interface SkillCategory {
    val id: Long
    val name: String
}



//abstract class Skill {
//    val name: String
//    val category: SkillCategory
//}

//object BB2020Skills {
//    data object Block: Skill {
//        override val name: String = "Block"
//        override val category: SkillCategory = AGILITY
//    }
//}

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
