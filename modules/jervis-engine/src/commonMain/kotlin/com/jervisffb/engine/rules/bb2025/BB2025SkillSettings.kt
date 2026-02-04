package com.jervisffb.engine.rules.bb2025

import com.jervisffb.engine.rules.bb2025.skills.Accurate
import com.jervisffb.engine.rules.bb2025.skills.AnimalSavagery
import com.jervisffb.engine.rules.bb2025.skills.BigHand
import com.jervisffb.engine.rules.bb2025.skills.Block
import com.jervisffb.engine.rules.bb2025.skills.BloodLust
import com.jervisffb.engine.rules.bb2025.skills.BoneHead
import com.jervisffb.engine.rules.bb2025.skills.BreakTackle
import com.jervisffb.engine.rules.bb2025.skills.BreatheFire
import com.jervisffb.engine.rules.bb2025.skills.Cannoneer
import com.jervisffb.engine.rules.bb2025.skills.CatchSkill
import com.jervisffb.engine.rules.bb2025.skills.CloudBurster
import com.jervisffb.engine.rules.bb2025.skills.DirtyPlayer
import com.jervisffb.engine.rules.bb2025.skills.DivingTackle
import com.jervisffb.engine.rules.bb2025.skills.Dodge
import com.jervisffb.engine.rules.bb2025.skills.ExtraArms
import com.jervisffb.engine.rules.bb2025.skills.Fend
import com.jervisffb.engine.rules.bb2025.skills.Frenzy
import com.jervisffb.engine.rules.bb2025.skills.GiveAndGo
import com.jervisffb.engine.rules.bb2025.skills.Guard
import com.jervisffb.engine.rules.bb2025.skills.HailMaryPass
import com.jervisffb.engine.rules.bb2025.skills.Hatred
import com.jervisffb.engine.rules.bb2025.skills.Horns
import com.jervisffb.engine.rules.bb2025.skills.Juggernaut
import com.jervisffb.engine.rules.bb2025.skills.Kick
import com.jervisffb.engine.rules.bb2025.skills.Leader
import com.jervisffb.engine.rules.bb2025.skills.Leap
import com.jervisffb.engine.rules.bb2025.skills.Loner
import com.jervisffb.engine.rules.bb2025.skills.MightyBlow
import com.jervisffb.engine.rules.bb2025.skills.MultipleBlock
import com.jervisffb.engine.rules.bb2025.skills.MyBall
import com.jervisffb.engine.rules.bb2025.skills.NervesOfSteel
import com.jervisffb.engine.rules.bb2025.skills.NoBall
import com.jervisffb.engine.rules.bb2025.skills.Pass
import com.jervisffb.engine.rules.bb2025.skills.PrehensileTail
import com.jervisffb.engine.rules.bb2025.skills.Pro
import com.jervisffb.engine.rules.bb2025.skills.ProjectileVomit
import com.jervisffb.engine.rules.bb2025.skills.PutTheBootIn
import com.jervisffb.engine.rules.bb2025.skills.QuickFoul
import com.jervisffb.engine.rules.bb2025.skills.ReallyStupid
import com.jervisffb.engine.rules.bb2025.skills.Regeneration
import com.jervisffb.engine.rules.bb2025.skills.RightStuff
import com.jervisffb.engine.rules.bb2025.skills.SafePass
import com.jervisffb.engine.rules.bb2025.skills.Shadowing
import com.jervisffb.engine.rules.bb2025.skills.Sidestep
import com.jervisffb.engine.rules.bb2025.skills.Sprint
import com.jervisffb.engine.rules.bb2025.skills.Stab
import com.jervisffb.engine.rules.bb2025.skills.Stunty
import com.jervisffb.engine.rules.bb2025.skills.SureFeet
import com.jervisffb.engine.rules.bb2025.skills.SureHands
import com.jervisffb.engine.rules.bb2025.skills.Tackle
import com.jervisffb.engine.rules.bb2025.skills.Taunt
import com.jervisffb.engine.rules.bb2025.skills.ThickSkull
import com.jervisffb.engine.rules.bb2025.skills.ThrowTeamMate
import com.jervisffb.engine.rules.bb2025.skills.Timmmber
import com.jervisffb.engine.rules.bb2025.skills.Titchy
import com.jervisffb.engine.rules.bb2025.skills.TwoHeads
import com.jervisffb.engine.rules.bb2025.skills.UnchannelledFury
import com.jervisffb.engine.rules.bb2025.skills.Unsteady
import com.jervisffb.engine.rules.bb2025.skills.VeryLongLegs
import com.jervisffb.engine.rules.bb2025.skills.Wrestle
import com.jervisffb.engine.rules.bb2025.specialrules.SneakiestOfTheLot
import com.jervisffb.engine.rules.common.skills.SkillCategory
import com.jervisffb.engine.rules.common.skills.SkillSettings
import com.jervisffb.engine.rules.common.skills.SkillType
import kotlinx.serialization.Serializable


@Serializable
class BB2025SkillSettings: SkillSettings() {
    override fun initializeSkillCache() {
        addCategory(SkillCategory.AGILITY)
        addCategory(SkillCategory.DEVIOUS)
        addCategory(SkillCategory.GENERAL)
        addCategory(SkillCategory.MUTATIONS)
        addCategory(SkillCategory.PASSING)
        addCategory(SkillCategory.STRENGTH)
        addCategory(SkillCategory.TRAITS)
        addCategory(SkillCategory.SPECIAL_RULES)
        SkillType.entries.forEach { type ->
            when (type) {

                //
                // Agility Category
                //
                SkillType.CATCH -> {
                    addNoValueEntry("Cath", type, SkillCategory.AGILITY) { player, category,expiresAt ->
                        CatchSkill(player, category, expiresAt)
                    }
                }
                SkillType.DIVING_CATCH -> {
                    // addEntry(type, SkillCategory.AGILITY) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.DIVING_TACKLE -> {
                    addNoValueEntry("Diving Tackle", type, SkillCategory.AGILITY) { player, category,expiresAt ->
                        DivingTackle(player, category, expiresAt)
                    }
                }
                SkillType.DODGE -> {
                    addNoValueEntry("Dodge", type, SkillCategory.AGILITY) { player, category,expiresAt ->
                        Dodge(player, category, expiresAt)
                    }
                }
                SkillType.DEFENSIVE -> {
                    // addEntry(type, SkillCategory.AGILITY) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.HIT_AND_RUN -> {
                    // addEntry(type, SkillCategory.AGILITY) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.JUMP_UP -> {
                    // addEntry(type, SkillCategory.AGILITY) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.LEAP -> {
                    addNoValueEntry("Leap", type, SkillCategory.AGILITY) { player, category,expiresAt ->
                        Leap(player, category, expiresAt)
                    }
                }
                SkillType.SAFE_PAIR_OF_HANDS -> {
                    // addEntry(type, SkillCategory.AGILITY) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.SIDESTEP -> {
                    addNoValueEntry("Sidestep", type, SkillCategory.AGILITY) { player, category,expiresAt ->
                        Sidestep(player, category, expiresAt)
                    }
                }
                SkillType.SPRINT -> {
                    addNoValueEntry("Sprint", type, SkillCategory.AGILITY) { player, category,expiresAt ->
                        Sprint(player, category, expiresAt)
                    }
                }
                SkillType.SURE_FEET -> {
                    addNoValueEntry("Sure Feet", type, SkillCategory.AGILITY) { player, category,expiresAt ->
                        SureFeet(player, category, expiresAt)
                    }
                }

                //
                // Devious Category
                //
                SkillType.DIRTY_PLAYER -> {
                    addNoValueEntry("Dirty Player", type, SkillCategory.DEVIOUS) { player, category,expiresAt ->
                        DirtyPlayer(player, category, expiresAt)
                    }
                }
                SkillType.EYE_GOUGE -> {
                    // addEntry(type, SkillCategory.DEVIOUS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.FUMBLEROOSKIE -> {
                    // addEntry(type, SkillCategory.DEVIOUS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.LETHAL_FLIGHT -> {
                    // addEntry(type, SkillCategory.DEVIOUS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.LONE_FOULER -> {
                    // addEntry(type, SkillCategory.DEVIOUS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.PILE_DRIVER -> {
                    // addEntry(type, SkillCategory.DEVIOUS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.PUT_THE_BOOT_IN -> {
                    addNoValueEntry("Put the Boot In", type, SkillCategory.DEVIOUS) { player, category,expiresAt ->
                        PutTheBootIn(player, category, expiresAt)
                    }
                }
                SkillType.QUICK_FOUL -> {
                    addNoValueEntry("Quick Foul", type, SkillCategory.DEVIOUS) { player, category,expiresAt ->
                        QuickFoul(player, category, expiresAt)
                    }
                }
                SkillType.SABOTEUR -> {
                    // addEntry(type, SkillCategory.DEVIOUS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.SHADOWING -> {
                    addNoValueEntry("Shadowing", type, SkillCategory.DEVIOUS) { player, category,expiresAt ->
                        Shadowing(player, category, expiresAt)
                    }
                }
                SkillType.SNEAKY_GIT -> {
                    // addEntry(type, SkillCategory.DEVIOUS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.VIOLENT_INNOVATOR -> {
                    // addEntry(type, SkillCategory.DEVIOUS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }

                //
                // General Category
                //
                SkillType.BLOCK -> {
                    addNoValueEntry("Block", type, SkillCategory.GENERAL) { player, category,expiresAt ->
                        Block(player, category, expiresAt)
                    }
                }
                SkillType.DAUNTLESS -> {
                    // addEntry(type, SkillCategory.GENERAL) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.FEND -> {
                    addNoValueEntry("Fend", type, SkillCategory.GENERAL) { player, category,expiresAt ->
                        Fend(player, category, expiresAt)
                    }
                }
                SkillType.FRENZY -> {
                    addNoValueEntry("Frenzy", type, SkillCategory.GENERAL) { player, category,expiresAt ->
                        Frenzy(player, category, expiresAt)
                    }
                }
                SkillType.KICK -> {
                    addNoValueEntry("Kick", type, SkillCategory.GENERAL) { player, category,expiresAt ->
                        Kick(player, category, expiresAt)
                    }
                }
                SkillType.PRO -> {
                    addNoValueEntry("Pro", type, SkillCategory.GENERAL) { player, category,expiresAt ->
                        Pro(player, category, expiresAt)
                    }
                }
                SkillType.STEADY_FOOTING -> {
                    // addEntry(type, SkillCategory.GENERAL) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.STRIP_BALL -> {
                    // addEntry(type, SkillCategory.GENERAL) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.SURE_HANDS -> {
                    addNoValueEntry("Sure Hands", type, SkillCategory.GENERAL) { player, category,expiresAt ->
                        SureHands(player, category, expiresAt)
                    }
                }
                SkillType.TACKLE -> {
                    addNoValueEntry("Tackle", type, SkillCategory.GENERAL) { player, category,expiresAt ->
                        Tackle(player, category, expiresAt)
                    }
                }
                SkillType.TAUNT -> {
                    addNoValueEntry("Taunt", type, SkillCategory.GENERAL) { player, category,expiresAt ->
                        Taunt(player, category, expiresAt)
                    }
                }
                SkillType.WRESTLE -> {
                    addNoValueEntry("Wrestle", type, SkillCategory.GENERAL) { player, category,expiresAt ->
                        Wrestle(player, category, expiresAt)
                    }
                }

                //
                // Mutations Category
                //
                SkillType.BIG_HAND -> {
                    addNoValueEntry("Big Hand", type, SkillCategory.MUTATIONS) { player, category,expiresAt ->
                        BigHand(player, category, expiresAt)
                    }
                }
                SkillType.CLAWS -> {
                    // addEntry(type, SkillCategory.MUTATIONS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.DISTURBING_PRESENCE -> {
                    // addEntry(type, SkillCategory.MUTATIONS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.EXTRA_ARMS -> {
                    addNoValueEntry("Extra Arms", type, SkillCategory.MUTATIONS) { player, category,expiresAt ->
                        ExtraArms(player, category, expiresAt)
                    }
                }
                SkillType.FOUL_APPEARANCE -> {
                    // addEntry(type, SkillCategory.MUTATIONS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.HORNS -> {
                    addNoValueEntry("Horns", type, SkillCategory.MUTATIONS) { player, category,expiresAt ->
                        Horns(player, category, expiresAt)
                    }
                }
                SkillType.IRON_HARD_SKIN -> {
                    // addEntry(type, SkillCategory.MUTATIONS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.MONSTROUS_MOUTH -> {
                    // addEntry(type, SkillCategory.MUTATIONS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.PREHENSILE_TAIL -> {
                    addNoValueEntry("Prehensile Tail", type, SkillCategory.MUTATIONS) { player, category,expiresAt ->
                        PrehensileTail(player, category, expiresAt)
                    }
                }
                SkillType.TENTACLE -> {
                    // addEntry(type, SkillCategory.MUTATIONS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.TWO_HEADS -> {
                    addNoValueEntry("Two Heads", type, SkillCategory.MUTATIONS) { player, category,expiresAt ->
                        TwoHeads(player, category, expiresAt)
                    }
                }
                SkillType.VERY_LONG_LEGS -> {
                    addNoValueEntry("Very Long Legs", type, SkillCategory.MUTATIONS) { player, category,expiresAt ->
                        VeryLongLegs(player, category, expiresAt)
                    }
                }

                //
                // Passing Category
                //
                SkillType.ACCURATE -> {
                    addNoValueEntry("Accurate", type, SkillCategory.PASSING) { player, category,expiresAt ->
                        Accurate(player, category, expiresAt)
                    }
                }
                SkillType.CANNONEER -> {
                    addNoValueEntry("Cannoneer", type, SkillCategory.PASSING) { player, category,expiresAt ->
                        Cannoneer(player, category, expiresAt)
                    }
                }
                SkillType.CLOUD_BURSTER -> {
                    addNoValueEntry("Cloud Burster", type, SkillCategory.PASSING) { player, category,expiresAt ->
                        CloudBurster(player, category, expiresAt)
                    }
                }
                SkillType.DUMP_OFF -> {
                    // addEntry(type, SkillCategory.PASSING) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.GIVE_AND_GO -> {
                    addNoValueEntry("Give and Go", type, SkillCategory.PASSING) { player, category,expiresAt ->
                        GiveAndGo(player, category, expiresAt)
                    }
                }
                SkillType.HAIL_MARY_PASS -> {
                    addNoValueEntry("Hail Mary Pass", type, SkillCategory.PASSING) { player, category,expiresAt ->
                        HailMaryPass(player, category, expiresAt)
                    }
                }
                SkillType.LEADER -> {
                    addNoValueEntry("Leader", type, SkillCategory.PASSING) { player, category,expiresAt ->
                        Leader(player, category, expiresAt)
                    }
                }
                SkillType.NERVES_OF_STEEL -> {
                    addNoValueEntry("Nerves of Steel", type, SkillCategory.PASSING) { player, category,expiresAt ->
                        NervesOfSteel(player, category, expiresAt)
                    }
                }
                SkillType.ON_THE_BALL -> {
                    // addEntry(type, SkillCategory.PASSING) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.PASS -> {
                    addNoValueEntry("Pass", type, SkillCategory.PASSING) { player, category,expiresAt ->
                        Pass(player, category, expiresAt)
                    }
                }
                SkillType.PUNT -> {
                    // addEntry(type, SkillCategory.PASSING) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.SAFE_PASS -> {
                    addNoValueEntry("Safe Pass", type, SkillCategory.PASSING) { player, category,expiresAt ->
                        SafePass(player, category, expiresAt)
                    }
                }

                //
                // Strength Category
                //
                SkillType.ARM_BAR -> {
                    // addEntry(type, SkillCategory.STRENGTH) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.BRAWLER -> {
                    // addEntry(type, SkillCategory.STRENGTH) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.BREAK_TACKLE -> {
                    addNoValueEntry("Break Tackle", type, SkillCategory.STRENGTH) { player, category,expiresAt ->
                        BreakTackle(player, category, expiresAt)
                    }
                }
                SkillType.BULLSEYE -> {
                    // addEntry(type, SkillCategory.STRENGTH) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.GRAB -> {
                    // addEntry(type, SkillCategory.STRENGTH) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.GUARD -> {
                    addNoValueEntry("Guard", type, SkillCategory.STRENGTH) { player, category,expiresAt ->
                        Guard(player, category, expiresAt)
                    }
                }
                SkillType.JUGGERNAUT -> {
                    addNoValueEntry("Juggernaut", type, SkillCategory.STRENGTH) { player, category,expiresAt ->
                        Juggernaut(player, category, expiresAt)
                    }
                }
                SkillType.MIGHTY_BLOW -> {
                    addIntEntry("Mighty Blow", type, SkillCategory.STRENGTH, 1) { player, category, _, expiresAt ->
                        MightyBlow(player, category, expiresAt)
                    }
                }
                SkillType.MULTIPLE_BLOCK -> {
                    addNoValueEntry("Multiple Block", type, SkillCategory.STRENGTH) { player, category,expiresAt ->
                        MultipleBlock(player, category, expiresAt)
                    }
                }
                SkillType.STAND_FIRM -> {
                    // addEntry(type, SkillCategory.STRENGTH) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.STRONG_ARM -> {
                    // addEntry(type, SkillCategory.STRENGTH) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.THICK_SKULL -> {
                    addNoValueEntry("Thick Skull", type, SkillCategory.STRENGTH) { player, category,expiresAt ->
                        ThickSkull(player, category, expiresAt)
                    }
                }

                //
                // Traits Category
                //
                SkillType.ANIMAL_SAVAGERY -> {
                    addNoValueEntry("Animal Savagery", type, SkillCategory.TRAITS) { player, category,expiresAt ->
                        AnimalSavagery(player, category, expiresAt)
                    }
                }
                SkillType.ANIMOSITY -> {
                    // addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.ALWAYS_HUNGRY -> {
                    // addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.BALL_AND_CHAIN -> {
                    // addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.BOMBARDIER -> {
                    // addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.BONE_HEAD -> {
                    addNoValueEntry("Bone Head", type, SkillCategory.TRAITS) { player, category,expiresAt ->
                        BoneHead(player, category, expiresAt)
                    }
                }
                SkillType.BLOOD_LUST -> {
                    addIntEntry("Blood Lust(X+)", type, SkillCategory.TRAITS, 4) { player, category, value, expiresAt ->
                        BloodLust(player, category, value, expiresAt)
                    }
                }
                SkillType.BREATHE_FIRE -> {
                    addNoValueEntry("Breathe Fire", type, SkillCategory.TRAITS) { player, category,expiresAt ->
                        BreatheFire(player, category, expiresAt)
                    }
                }
                SkillType.CHAINSAW -> {
                    // addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.DECAY -> {
                    // addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.DRUNKARD -> {
                    // TODO()
                }
                SkillType.HATRED -> {
                    addKeywordEntry("Hatred(X)", type, SkillCategory.TRAITS) { player, category, value,expiresAt ->
                        Hatred(player, category, value, expiresAt)
                    }
                }
                SkillType.HYPNOTIC_GAZE -> {
                    // addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.INSIGNIFICANT -> {
                    // TODO()
                }
                SkillType.KICK_TEAMMATE -> {
                    // addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.LONER -> {
                    addIntEntry("Loner(X+)", type, SkillCategory.TRAITS, 4) { player, category, value, expiresAt ->
                        Loner(player, category, value, expiresAt)
                    }
                }
                SkillType.MY_BALL -> {
                    addNoValueEntry("My Ball", type, SkillCategory.TRAITS) { player, category,expiresAt ->
                        MyBall(player, category, expiresAt)
                    }
                }
                SkillType.NO_BALL -> {
                    addNoValueEntry("No Ball", type, SkillCategory.TRAITS) { player, category,expiresAt ->
                        NoBall(player, category, expiresAt)
                    }
                }
                SkillType.PICK_ME_UP -> {
                    // TODO()
                }
                SkillType.PLAGUE_RIDDEN -> {
                    // addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.POGO_STICK -> {
                    // addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.PROJECTILE_VOMIT -> {
                    addNoValueEntry("Projectile Vomit", type, SkillCategory.TRAITS) { player, category,expiresAt ->
                        ProjectileVomit(player, category, expiresAt)
                    }
                }
                SkillType.REALLY_STUPID -> {
                    addNoValueEntry("Really Stupid", type, SkillCategory.TRAITS) { player, category,expiresAt ->
                        ReallyStupid(player, category, expiresAt)
                    }
                }
                SkillType.REGENERATION -> {
                    addNoValueEntry("Regeneration", type, SkillCategory.TRAITS) { player, category,expiresAt ->
                        Regeneration(player, category, expiresAt)
                    }
                }
                SkillType.RIGHT_STUFF -> {
                    addNoValueEntry("Right Stuff", type, SkillCategory.TRAITS) { player, category,expiresAt ->
                        RightStuff(player, category, expiresAt)
                    }
                }
                SkillType.SECRET_WEAPON -> {
                    // addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.STAB -> {
                    addNoValueEntry("Stab", type, SkillCategory.TRAITS) { player, category,expiresAt ->
                        Stab(player, category, expiresAt)
                    }
                }
                SkillType.STUNTY -> {
                    addNoValueEntry("Stunty", type, SkillCategory.TRAITS) { player, category,expiresAt ->
                        Stunty(player, category, expiresAt)
                    }
                }
                SkillType.SWARMING -> {
                    // addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.SWOOP -> {
                    // addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.TAKE_ROOT -> {
                    // addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.TITCHY -> {
                    addNoValueEntry("Titchy", type, SkillCategory.TRAITS) { player, category,expiresAt ->
                        Titchy(player, category, expiresAt)
                    }
                }
                SkillType.TIMMMBER -> {
                    addNoValueEntry("Timmm-ber", type, SkillCategory.TRAITS) { player, category,expiresAt ->
                        Timmmber(player, category, expiresAt)
                    }
                }
                SkillType.THROW_TEAMMATE -> {
                    addNoValueEntry("Throw Team-mate", type, SkillCategory.TRAITS) { player, category,expiresAt ->
                        ThrowTeamMate(player, category, expiresAt)
                    }
                }
                SkillType.TRICKSTER -> {

                }
                SkillType.UNCHANNELLED_FURY -> {
                    addNoValueEntry("Unchannelled Fury", type, SkillCategory.TRAITS) { player, category,expiresAt ->
                        UnchannelledFury(player, category, expiresAt)
                    }
                }
                SkillType.UNSTEADY -> {
                    addNoValueEntry("Unsteady", type, SkillCategory.TRAITS) { player, category, expiresAt ->
                        Unsteady(player, category, expiresAt)
                    }
                }

                //
                // Special Rules Category
                //
                SkillType.SNEAKIEST_OF_THE_LOT -> {
                    addNoValueEntry("Sneakiest of the Lot", type, SkillCategory.SPECIAL_RULES) { player, category,expiresAt ->
                        SneakiestOfTheLot(player, category, expiresAt)
                    }
                }

                // Skills not supported in this ruleset
                SkillType.NO_HANDS,
                SkillType.RUNNING_PASS -> {
                    // Ignore
                }
            }
        }
    }
}
