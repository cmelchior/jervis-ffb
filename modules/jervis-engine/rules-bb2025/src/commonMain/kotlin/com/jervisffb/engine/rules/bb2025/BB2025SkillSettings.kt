package com.jervisffb.engine.rules.bb2025

import com.jervisffb.engine.rules.bb2025.skills.Accurate
import com.jervisffb.engine.rules.bb2025.skills.AlwaysHungry
import com.jervisffb.engine.rules.bb2025.skills.AnimalSavagery
import com.jervisffb.engine.rules.bb2025.skills.ArmBar
import com.jervisffb.engine.rules.bb2025.skills.BigHand
import com.jervisffb.engine.rules.bb2025.skills.Block
import com.jervisffb.engine.rules.bb2025.skills.BloodLust
import com.jervisffb.engine.rules.bb2025.skills.BoneHead
import com.jervisffb.engine.rules.bb2025.skills.Brawler
import com.jervisffb.engine.rules.bb2025.skills.BreakTackle
import com.jervisffb.engine.rules.bb2025.skills.BreatheFire
import com.jervisffb.engine.rules.bb2025.skills.Bullseye
import com.jervisffb.engine.rules.bb2025.skills.Cannoneer
import com.jervisffb.engine.rules.bb2025.skills.CatchSkill
import com.jervisffb.engine.rules.bb2025.skills.Chainsaw
import com.jervisffb.engine.rules.bb2025.skills.Claws
import com.jervisffb.engine.rules.bb2025.skills.CloudBurster
import com.jervisffb.engine.rules.bb2025.skills.Dauntless
import com.jervisffb.engine.rules.bb2025.skills.Decay
import com.jervisffb.engine.rules.bb2025.skills.Defensive
import com.jervisffb.engine.rules.bb2025.skills.DirtyPlayer
import com.jervisffb.engine.rules.bb2025.skills.DisturbingPresence
import com.jervisffb.engine.rules.bb2025.skills.DivingCatch
import com.jervisffb.engine.rules.bb2025.skills.DivingTackle
import com.jervisffb.engine.rules.bb2025.skills.Dodge
import com.jervisffb.engine.rules.bb2025.skills.ExtraArms
import com.jervisffb.engine.rules.bb2025.skills.EyeGouge
import com.jervisffb.engine.rules.bb2025.skills.Fend
import com.jervisffb.engine.rules.bb2025.skills.FoulAppearance
import com.jervisffb.engine.rules.bb2025.skills.Frenzy
import com.jervisffb.engine.rules.bb2025.skills.Fumblerooski
import com.jervisffb.engine.rules.bb2025.skills.GiveAndGo
import com.jervisffb.engine.rules.bb2025.skills.Grab
import com.jervisffb.engine.rules.bb2025.skills.Guard
import com.jervisffb.engine.rules.bb2025.skills.HailMaryPass
import com.jervisffb.engine.rules.bb2025.skills.Hatred
import com.jervisffb.engine.rules.bb2025.skills.HitAndRun
import com.jervisffb.engine.rules.bb2025.skills.Horns
import com.jervisffb.engine.rules.bb2025.skills.HypnoticGaze
import com.jervisffb.engine.rules.bb2025.skills.Insignificant
import com.jervisffb.engine.rules.bb2025.skills.IronHardSkin
import com.jervisffb.engine.rules.bb2025.skills.Juggernaut
import com.jervisffb.engine.rules.bb2025.skills.JumpUp
import com.jervisffb.engine.rules.bb2025.skills.Kick
import com.jervisffb.engine.rules.bb2025.skills.Leader
import com.jervisffb.engine.rules.bb2025.skills.Leap
import com.jervisffb.engine.rules.bb2025.skills.LethalFlight
import com.jervisffb.engine.rules.bb2025.skills.LoneFouler
import com.jervisffb.engine.rules.bb2025.skills.Loner
import com.jervisffb.engine.rules.bb2025.skills.MightyBlow
import com.jervisffb.engine.rules.bb2025.skills.MonstrousMouth
import com.jervisffb.engine.rules.bb2025.skills.MyBall
import com.jervisffb.engine.rules.bb2025.skills.NervesOfSteel
import com.jervisffb.engine.rules.bb2025.skills.NoBall
import com.jervisffb.engine.rules.bb2025.skills.Pass
import com.jervisffb.engine.rules.bb2025.skills.PileDriver
import com.jervisffb.engine.rules.bb2025.skills.Pogo
import com.jervisffb.engine.rules.bb2025.skills.PrehensileTail
import com.jervisffb.engine.rules.bb2025.skills.Pro
import com.jervisffb.engine.rules.bb2025.skills.ProjectileVomit
import com.jervisffb.engine.rules.bb2025.skills.Punt
import com.jervisffb.engine.rules.bb2025.skills.PutTheBootIn
import com.jervisffb.engine.rules.bb2025.skills.QuickFoul
import com.jervisffb.engine.rules.bb2025.skills.ReallyStupid
import com.jervisffb.engine.rules.bb2025.skills.Regeneration
import com.jervisffb.engine.rules.bb2025.skills.RightStuff
import com.jervisffb.engine.rules.bb2025.skills.SafePairOfHands
import com.jervisffb.engine.rules.bb2025.skills.SafePass
import com.jervisffb.engine.rules.bb2025.skills.SecretWeapon
import com.jervisffb.engine.rules.bb2025.skills.Shadowing
import com.jervisffb.engine.rules.bb2025.skills.Sidestep
import com.jervisffb.engine.rules.bb2025.skills.SneakyGit
import com.jervisffb.engine.rules.bb2025.skills.Sprint
import com.jervisffb.engine.rules.bb2025.skills.Stab
import com.jervisffb.engine.rules.bb2025.skills.StandFirm
import com.jervisffb.engine.rules.bb2025.skills.SteadyFooting
import com.jervisffb.engine.rules.bb2025.skills.StripBall
import com.jervisffb.engine.rules.bb2025.skills.StrongArm
import com.jervisffb.engine.rules.bb2025.skills.Stunty
import com.jervisffb.engine.rules.bb2025.skills.SureFeet
import com.jervisffb.engine.rules.bb2025.skills.SureHands
import com.jervisffb.engine.rules.bb2025.skills.Swoop
import com.jervisffb.engine.rules.bb2025.skills.Tackle
import com.jervisffb.engine.rules.bb2025.skills.TakeRoot
import com.jervisffb.engine.rules.bb2025.skills.Taunt
import com.jervisffb.engine.rules.bb2025.skills.Tentacles
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
                    addNoValueEntry("Catch", type, SkillCategory.AGILITY) { player, category,expiresAt ->
                        CatchSkill(player, category, expiresAt)
                    }
                }
                SkillType.DIVING_CATCH -> {
                    addNoValueEntry("Diving Catch", type, SkillCategory.AGILITY) { player, category,expiresAt ->
                        DivingCatch(player, category, expiresAt)
                    }
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
                    addNoValueEntry("Defensive", type, SkillCategory.AGILITY) { player, category,expiresAt ->
                        Defensive(player, category, expiresAt)
                    }
                }
                SkillType.HIT_AND_RUN -> {
                    addNoValueEntry("Hit and Run", type, SkillCategory.AGILITY) { player, category,expiresAt ->
                        HitAndRun(player, category, expiresAt)
                    }
                }
                SkillType.JUMP_UP -> {
                    addNoValueEntry("Jump Up", type, SkillCategory.AGILITY) { player, category,expiresAt ->
                        JumpUp(player, category, expiresAt)
                    }
                }
                SkillType.LEAP -> {
                    addNoValueEntry("Leap", type, SkillCategory.AGILITY) { player, category,expiresAt ->
                        Leap(player, category, expiresAt)
                    }
                }
                SkillType.SAFE_PAIR_OF_HANDS -> {
                    addNoValueEntry("Safe Pair of Hands", type, SkillCategory.AGILITY) { player, category,expiresAt ->
                        SafePairOfHands(player, category, expiresAt)
                    }
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
                    addIntAdjustmentEntry("Dirty Player", type, SkillCategory.DEVIOUS, defaultValue = 1) { player, category, value, expiresAt ->
                        DirtyPlayer(player, category, value!!, expiresAt)
                    }
                }
                SkillType.EYE_GOUGE -> {
                    addNoValueEntry("Eye Gouge", type, SkillCategory.DEVIOUS) { player, category,expiresAt ->
                        EyeGouge(player, category, expiresAt)
                    }
                }
                SkillType.FUMBLEROOSKI -> {
                    addNoValueEntry("Fumblerooski", type, SkillCategory.DEVIOUS) { player, category,expiresAt ->
                        Fumblerooski(player, category, expiresAt)
                    }
                }
                SkillType.LETHAL_FLIGHT -> {
                    addNoValueEntry("Lethal Flight", type, SkillCategory.DEVIOUS) { player, category,expiresAt ->
                        LethalFlight(player, category, expiresAt)
                    }
                }
                SkillType.LONE_FOULER -> {
                    addNoValueEntry("Lone Fouler", type, SkillCategory.DEVIOUS) { player, category,expiresAt ->
                        LoneFouler(player, category, expiresAt)
                    }
                }
                SkillType.PILE_DRIVER -> {
                    addNoValueEntry("Pile Driver", type, SkillCategory.DEVIOUS) { player, category,expiresAt ->
                        PileDriver(player, category, expiresAt)
                    }
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
                    addNoValueEntry("Sneaky Git", type, SkillCategory.DEVIOUS) { player, category,expiresAt ->
                        SneakyGit(player, category, expiresAt)
                    }
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
                    addNoValueEntry("Dauntless", type, SkillCategory.GENERAL) { player, category,expiresAt ->
                        Dauntless(player, category, expiresAt)
                    }
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
                    addNoValueEntry("Steady Footing", type, SkillCategory.GENERAL) { player, category,expiresAt ->
                        SteadyFooting(player, category, expiresAt)
                    }
                }
                SkillType.STRIP_BALL -> {
                    addNoValueEntry("Strip Ball", type, SkillCategory.GENERAL) { player, category,expiresAt ->
                        StripBall(player, category, expiresAt)
                    }
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
                    addNoValueEntry("Claws", type, SkillCategory.MUTATIONS) { player, category,expiresAt ->
                        Claws(player, category, expiresAt)
                    }
                }
                SkillType.DISTURBING_PRESENCE -> {
                    addNoValueEntry("Disturbing Presence", type, SkillCategory.MUTATIONS) { player, category,expiresAt ->
                        DisturbingPresence(player, category, expiresAt)
                    }
                }
                SkillType.EXTRA_ARMS -> {
                    addNoValueEntry("Extra Arms", type, SkillCategory.MUTATIONS) { player, category,expiresAt ->
                        ExtraArms(player, category, expiresAt)
                    }
                }
                SkillType.FOUL_APPEARANCE -> {
                    addNoValueEntry("Foul Appearance", type, SkillCategory.MUTATIONS) { player, category,expiresAt ->
                        FoulAppearance(player, category, expiresAt)
                    }
                }
                SkillType.HORNS -> {
                    addNoValueEntry("Horns", type, SkillCategory.MUTATIONS) { player, category,expiresAt ->
                        Horns(player, category, expiresAt)
                    }
                }
                SkillType.IRON_HARD_SKIN -> {
                    addNoValueEntry("Iron Hard Skin", type, SkillCategory.MUTATIONS) { player, category,expiresAt ->
                        IronHardSkin(player, category, expiresAt)
                    }
                }
                SkillType.MONSTROUS_MOUTH -> {
                    addNoValueEntry("Monstrous Mouth", type, SkillCategory.MUTATIONS) { player, category,expiresAt ->
                        MonstrousMouth(player, category, expiresAt)
                    }
                }
                SkillType.PREHENSILE_TAIL -> {
                    addNoValueEntry("Prehensile Tail", type, SkillCategory.MUTATIONS) { player, category,expiresAt ->
                        PrehensileTail(player, category, expiresAt)
                    }
                }
                SkillType.TENTACLES -> {
                    addNoValueEntry("Tentacles", type, SkillCategory.MUTATIONS) { player, category,expiresAt ->
                        Tentacles(player, category, expiresAt)
                    }
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
                    addNoValueEntry("Punt", type, SkillCategory.PASSING) { player, category, expiresAt ->
                        Punt(player, category, expiresAt)
                    }
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
                    addNoValueEntry("Arm Bar", type, SkillCategory.STRENGTH) { player, category,expiresAt ->
                        ArmBar(player, category, expiresAt)
                    }
                }
                SkillType.BRAWLER -> {
                    addNoValueEntry("Brawler", type, SkillCategory.STRENGTH) { player, category,expiresAt ->
                        Brawler(player, category, expiresAt)
                    }
                }
                SkillType.BREAK_TACKLE -> {
                    addNoValueEntry("Break Tackle", type, SkillCategory.STRENGTH) { player, category,expiresAt ->
                        BreakTackle(player, category, expiresAt)
                    }
                }
                SkillType.BULLSEYE -> {
                    addNoValueEntry("Bullseye", type, SkillCategory.STRENGTH) { player, category,expiresAt ->
                        Bullseye(player, category, expiresAt)
                    }
                }
                SkillType.GRAB -> {
                    addNoValueEntry("Grab", type, SkillCategory.STRENGTH) { player, category,expiresAt ->
                        Grab(player, category, expiresAt)
                    }
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
                    addIntAdjustmentEntry("Mighty Blow", type, SkillCategory.STRENGTH, 1) { player, category, value, expiresAt ->
                        MightyBlow(player, category, value!!, expiresAt)
                    }
                }
                SkillType.MULTIPLE_BLOCK -> {
                    // To many things missing, so disable for now
                    //    addNoValueEntry("Multiple Block", type, SkillCategory.STRENGTH) { player, category,expiresAt ->
                    //        MultipleBlock(player, category, expiresAt)
                    //    }
                }
                SkillType.STAND_FIRM -> {
                    addNoValueEntry("Stand Firm", type, SkillCategory.STRENGTH) { player, category,expiresAt ->
                        StandFirm(player, category, expiresAt)
                    }
                }
                SkillType.STRONG_ARM -> {
                    addNoValueEntry("Strong Arm", type, SkillCategory.STRENGTH) { player, category,expiresAt ->
                        StrongArm(player, category, expiresAt)
                    }
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
                    addNoValueEntry("Always Hungry", type, SkillCategory.TRAITS) { player, category,expiresAt ->
                        AlwaysHungry(player, category, expiresAt)
                    }
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
                    addIntTargetEntry("Blood Lust(X+)", type, SkillCategory.TRAITS, 4) { player, category, value, expiresAt ->
                        BloodLust(player, category, value, expiresAt)
                    }
                }
                SkillType.BREATHE_FIRE -> {
                    addNoValueEntry("Breathe Fire", type, SkillCategory.TRAITS) { player, category,expiresAt ->
                        BreatheFire(player, category, expiresAt)
                    }
                }
                SkillType.CHAINSAW -> {
                    addNoValueEntry("Chainsaw", type, SkillCategory.TRAITS) { player, category,expiresAt ->
                        Chainsaw(player, category, expiresAt)
                    }
                }
                SkillType.DECAY -> {
                    addNoValueEntry("Decay", type, SkillCategory.TRAITS) { player, category,expiresAt ->
                        Decay(player, category, expiresAt)
                    }
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
                    addNoValueEntry("Hypnotic Gaze", type, SkillCategory.TRAITS) { player, category, expiresAt ->
                        HypnoticGaze(player, category, expiresAt)
                    }
                }
                SkillType.INSIGNIFICANT -> {
                    addNoValueEntry("Insignificant", type, SkillCategory.TRAITS) { player, category,expiresAt ->
                        Insignificant(player, category, expiresAt)
                    }
                }
                SkillType.KICK_TEAMMATE -> {
                    // addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.LONER -> {
                    addIntTargetEntry("Loner(X+)", type, SkillCategory.TRAITS, 4) { player, category, value, expiresAt ->
                        Loner(player, category, value!!, expiresAt)
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
                    addNoValueEntry("Pogo", type, SkillCategory.TRAITS) { player, category,expiresAt ->
                        Pogo(player, category, expiresAt)
                    }
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
                    addNoValueEntry("Secret Weapon", type, SkillCategory.TRAITS) { player, category,expiresAt ->
                        SecretWeapon(player, category, expiresAt)
                    }
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
                    addNoValueEntry("Swoop", type, SkillCategory.TRAITS) { player, category,expiresAt ->
                        Swoop(player, category, expiresAt)
                    }
                }
                SkillType.TAKE_ROOT -> {
                    addNoValueEntry("Take Root", type, SkillCategory.TRAITS) { player, category,expiresAt ->
                        TakeRoot(player, category, expiresAt)
                    }
                }
                SkillType.TITCHY -> {
                    addNoValueEntry("Titchy", type, SkillCategory.TRAITS) { player, category,expiresAt ->
                        Titchy(player, category, expiresAt)
                    }
                }
                SkillType.TIMMMBER -> {
                    addNoValueEntry("Timmm-ber!", type, SkillCategory.TRAITS) { player, category,expiresAt ->
                        Timmmber(player, category, expiresAt)
                    }
                }
                SkillType.THROW_TEAMMATE -> {
                    addNoValueEntry("Throw Team-mate", type, SkillCategory.TRAITS) { player, category,expiresAt ->
                        ThrowTeamMate(player, category, expiresAt)
                    }
                }
                SkillType.TRICKSTER -> {
                    //    addNoValueEntry("Trickster", type, SkillCategory.TRAITS) { player, category, expiresAt ->
                    //        Trickster(player, category, expiresAt)
                    //    }
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
