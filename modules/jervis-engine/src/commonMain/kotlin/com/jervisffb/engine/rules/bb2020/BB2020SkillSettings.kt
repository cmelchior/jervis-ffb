package com.jervisffb.engine.rules.bb2020

import com.jervisffb.engine.rules.bb2020.skills.AnimalSavagery
import com.jervisffb.engine.rules.bb2020.skills.Block
import com.jervisffb.engine.rules.bb2020.skills.BloodLust
import com.jervisffb.engine.rules.bb2020.skills.BoneHead
import com.jervisffb.engine.rules.bb2020.skills.BreakTackle
import com.jervisffb.engine.rules.bb2020.skills.BreatheFire
import com.jervisffb.engine.rules.bb2020.skills.CatchSkill
import com.jervisffb.engine.rules.bb2020.skills.DivingTackle
import com.jervisffb.engine.rules.bb2020.skills.Dodge
import com.jervisffb.engine.rules.bb2020.skills.Frenzy
import com.jervisffb.engine.rules.bb2020.skills.Horns
import com.jervisffb.engine.rules.bb2020.skills.Leader
import com.jervisffb.engine.rules.bb2020.skills.Leap
import com.jervisffb.engine.rules.bb2020.skills.Loner
import com.jervisffb.engine.rules.bb2020.skills.MightyBlow
import com.jervisffb.engine.rules.bb2020.skills.MultipleBlock
import com.jervisffb.engine.rules.bb2020.skills.Pass
import com.jervisffb.engine.rules.bb2020.skills.PrehensileTail
import com.jervisffb.engine.rules.bb2020.skills.Pro
import com.jervisffb.engine.rules.bb2020.skills.ProjectileVomit
import com.jervisffb.engine.rules.bb2020.skills.ReallyStupid
import com.jervisffb.engine.rules.bb2020.skills.Regeneration
import com.jervisffb.engine.rules.bb2020.skills.RightStuff
import com.jervisffb.engine.rules.bb2020.skills.Sidestep
import com.jervisffb.engine.rules.bb2020.skills.Sprint
import com.jervisffb.engine.rules.bb2020.skills.Stab
import com.jervisffb.engine.rules.bb2020.skills.Stunty
import com.jervisffb.engine.rules.bb2020.skills.SureFeet
import com.jervisffb.engine.rules.bb2020.skills.SureHands
import com.jervisffb.engine.rules.bb2020.skills.Tackle
import com.jervisffb.engine.rules.bb2020.skills.ThickSkull
import com.jervisffb.engine.rules.bb2020.skills.ThrowTeamMate
import com.jervisffb.engine.rules.bb2020.skills.Timmmber
import com.jervisffb.engine.rules.bb2020.skills.Titchy
import com.jervisffb.engine.rules.bb2020.skills.UnchannelledFury
import com.jervisffb.engine.rules.bb2020.skills.Wrestle
import com.jervisffb.engine.rules.bb2020.specialrules.SneakiestOfTheLot
import com.jervisffb.engine.rules.common.skills.SkillCategory
import com.jervisffb.engine.rules.common.skills.SkillSettings
import com.jervisffb.engine.rules.common.skills.SkillType
import kotlinx.serialization.Serializable

@Serializable
class BB2020SkillSettings: SkillSettings() {

    // Setup skills as they are defined in the BB2020 Rulebook. See page 74.
    override fun initializeSkillCache() {
        addCategory(SkillCategory.AGILITY)
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
                    addNoValueEntry("Catch", type, SkillCategory.AGILITY) { player, category, expiresAt ->
                        CatchSkill(player, category, expiresAt)
                    }
                }
                SkillType.DIVING_CATCH -> {
                    // addEntry(type, SkillCategory.AGILITY) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.DIVING_TACKLE -> {
                    addNoValueEntry("Diving Tackle", type, SkillCategory.AGILITY) { player, category, expiresAt ->
                        DivingTackle(player, category, expiresAt)
                    }
                }
                SkillType.DODGE -> {
                    addNoValueEntry("Dodge", type, SkillCategory.AGILITY) { player, category, expiresAt ->
                        Dodge(player, category, expiresAt)
                    }
                }
                SkillType.DEFENSIVE -> {
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
                    addNoValueEntry("Leap", type, SkillCategory.AGILITY) { player, category, expiresAt ->
                        Leap(player, category, expiresAt)
                    }
                }
                SkillType.SAFE_PAIR_OF_HANDS -> {
                    // addEntry(type, SkillCategory.AGILITY) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.SIDESTEP -> {
                    addNoValueEntry("Sidestep", type, SkillCategory.AGILITY) { player, category, expiresAt ->
                        Sidestep(player, category, expiresAt)
                    }
                }
                SkillType.SNEAKY_GIT -> {
                    // addEntry(type, SkillCategory.AGILITY) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.SPRINT -> {
                    addNoValueEntry("Sprint", type, SkillCategory.AGILITY) { player, category, expiresAt ->
                        Sprint(player, category, expiresAt)
                    }
                }
                SkillType.SURE_FEET -> {
                    addNoValueEntry("Sure Feet", type, SkillCategory.AGILITY) { player, category, expiresAt ->
                        SureFeet(player, category, expiresAt)
                    }
                }

                //
                // General Category
                //
                SkillType.BLOCK -> {
                    addNoValueEntry("Block", type, SkillCategory.GENERAL) { player, category, expiresAt ->
                        Block(player, category, expiresAt)
                    }
                }
                SkillType.DAUNTLESS -> {
                    // addEntry(type, SkillCategory.GENERAL) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.DIRTY_PLAYER -> {
                    // addEntry(type, SkillCategory.GENERAL, 1) { player, category, value , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.FEND -> {
                    // addEntry(type, SkillCategory.GENERAL) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.FRENZY -> {
                    addNoValueEntry("Frenzy", type, SkillCategory.GENERAL) { player, category, expiresAt ->
                        Frenzy(player, category, expiresAt)
                    }
                }
                SkillType.KICK -> {
                    // addEntry(type, SkillCategory.GENERAL) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.PRO -> {
                    addNoValueEntry("Pro", type, SkillCategory.GENERAL) { player, category, expiresAt ->
                        Pro(player, category, expiresAt)
                    }
                }
                SkillType.SHADOWING -> {
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
                    addNoValueEntry("Sure Hands", type, SkillCategory.GENERAL) { player, category, expiresAt ->
                        SureHands(player, category, expiresAt)
                    }
                }
                SkillType.TACKLE -> {
                    addNoValueEntry("Tackle", type, SkillCategory.GENERAL) { player, category, expiresAt ->
                        Tackle(player, category, expiresAt)
                    }
                }
                SkillType.WRESTLE -> {
                    addNoValueEntry("Wrestle", type, SkillCategory.GENERAL) { player, category, expiresAt ->
                        Wrestle(player, category, expiresAt)
                    }
                }

                //
                // Mutations Category
                //
                SkillType.BIG_HAND -> {
                    // addEntry(type, SkillCategory.MUTATIONS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
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
                    // addEntry(type, SkillCategory.MUTATIONS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.FOUL_APPEARANCE -> {
                    // addEntry(type, SkillCategory.MUTATIONS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.HORNS -> {
                    addNoValueEntry("Horns", type, SkillCategory.MUTATIONS) { player, category, expiresAt ->
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
                    addNoValueEntry("Prehesile Tail", type, SkillCategory.MUTATIONS) { player, category, expiresAt ->
                        PrehensileTail(player, category, expiresAt)
                    }
                }
                SkillType.TENTACLE -> {
                    // addEntry(type, SkillCategory.MUTATIONS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.TWO_HEADS -> {
                    // addEntry(type, SkillCategory.MUTATIONS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.VERY_LONG_LEGS -> {
                    // addEntry(type, SkillCategory.MUTATIONS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }

                //
                // Passing Category
                //
                SkillType.ACCURATE -> {
                    // addEntry(type, SkillCategory.PASSING) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.CANNONEER -> {
                    // addEntry(type, SkillCategory.PASSING) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.CLOUD_BURSTER -> {
                    // addEntry(type, SkillCategory.PASSING) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.DUMP_OFF -> {
                    // addEntry(type, SkillCategory.PASSING) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.FUMBLEROOSKIE -> {
                    // addEntry(type, SkillCategory.PASSING) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.HAIL_MARY_PASS -> {
                    // addEntry(type, SkillCategory.PASSING) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.LEADER -> {
                    addNoValueEntry("Leader", type, SkillCategory.PASSING) { player, category, expiresAt ->
                        Leader(player, category, expiresAt)
                    }
                }
                SkillType.NERVES_OF_STEEL -> {
                    // addEntry(type, SkillCategory.PASSING) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.ON_THE_BALL -> {
                    // addEntry(type, SkillCategory.PASSING) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.PASS -> {
                    addNoValueEntry("Pass", type, SkillCategory.PASSING) { player, category, expiresAt ->
                        Pass(player, category, expiresAt)
                    }
                }
                SkillType.RUNNING_PASS -> {
                    // addEntry(type, SkillCategory.PASSING) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.SAFE_PASS -> {
                    // addEntry(type, SkillCategory.PASSING) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
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
                    addNoValueEntry("Break Tackle", type, SkillCategory.STRENGTH) { player, category, expiresAt ->
                        BreakTackle(player, category, expiresAt)
                    }
                }
                SkillType.GRAB -> {
                    // addEntry(type, SkillCategory.STRENGTH) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.GUARD -> {
                    // addEntry(type, SkillCategory.STRENGTH) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.JUGGERNAUT -> {
                    // addEntry(type, SkillCategory.STRENGTH) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.MIGHTY_BLOW -> {
                    addIntEntry("Mighty Blow", type, SkillCategory.STRENGTH, 1) { player, category, value, expiresAt ->
                        MightyBlow(player, category, value, expiresAt)
                    }
                }
                SkillType.MULTIPLE_BLOCK -> {
                    addNoValueEntry("Multiple Block", type, SkillCategory.STRENGTH) { player, category, expiresAt ->
                        MultipleBlock(player, category, expiresAt)
                    }
                }
                SkillType.PILE_DRIVER -> {
                    // addEntry(type, SkillCategory.STRENGTH) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
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
                    addNoValueEntry("Thick Skull", type, SkillCategory.STRENGTH) { player, category, expiresAt ->
                        ThickSkull(player, category, expiresAt)
                    }
                }

                //
                // Traits Category
                //
                SkillType.ANIMAL_SAVAGERY -> {
                    addNoValueEntry("Animal Savagery", type, SkillCategory.TRAITS) { player, category, expiresAt ->
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
                    addNoValueEntry("Bone Head", type, SkillCategory.TRAITS) { player, category, expiresAt ->
                        BoneHead(player, category, expiresAt)
                    }
                }
                SkillType.BLOOD_LUST -> {
                    addIntEntry("Blood Lust(X+)", type, SkillCategory.TRAITS, 4) { player, category, value, expiresAt ->
                        BloodLust(player, category, value, expiresAt)
                    }
                }
                SkillType.BREATHE_FIRE -> {
                    addNoValueEntry("Breathe Fire", type, SkillCategory.TRAITS) { player, category, expiresAt ->
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
                SkillType.HIT_AND_RUN -> {
                    // addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.HYPNOTIC_GAZE -> {
                    // addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
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
                SkillType.NO_HANDS -> {
                    // addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
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
                    addNoValueEntry("Projectile Vomit", type, SkillCategory.TRAITS) { player, category, expiresAt ->
                        ProjectileVomit(player, category, expiresAt)
                    }
                }
                SkillType.REALLY_STUPID -> {
                    addNoValueEntry("Really Stupid", type, SkillCategory.TRAITS) { player, category, expiresAt ->
                        ReallyStupid(player, category, expiresAt)
                    }
                }
                SkillType.REGENERATION -> {
                    addNoValueEntry("Regeneration", type, SkillCategory.TRAITS) { player, category, expiresAt ->
                        Regeneration(player, category, expiresAt)
                    }
                }
                SkillType.RIGHT_STUFF -> {
                    addNoValueEntry("Right Stuff", type, SkillCategory.TRAITS) { player, category, expiresAt ->
                        RightStuff(player, category, expiresAt)
                    }
                }
                SkillType.SECRET_WEAPON -> {
                    // addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.STAB -> {
                    addNoValueEntry("Stab", type, SkillCategory.TRAITS) { player, category, expiresAt ->
                        Stab(player, category, expiresAt)
                    }
                }
                SkillType.STUNTY -> {
                    addNoValueEntry("Stunty", type, SkillCategory.TRAITS) { player, category, expiresAt ->
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
                    addNoValueEntry("Titchy", type, SkillCategory.TRAITS) { player, category, expiresAt ->
                        Titchy(player, category, expiresAt)
                    }
                }
                SkillType.TIMMMBER -> {
                    addNoValueEntry("Timmm-ber", type, SkillCategory.TRAITS) { player, category, expiresAt ->
                        Timmmber(player, category, expiresAt)
                    }
                }
                SkillType.THROW_TEAMMATE -> {
                    addNoValueEntry("Throw Team-mate", type, SkillCategory.TRAITS) { player, category, expiresAt ->
                        ThrowTeamMate(player, category, expiresAt)
                    }
                }
                SkillType.UNCHANNELLED_FURY -> {
                    addNoValueEntry("Unchannelled Fury", type, SkillCategory.TRAITS) { player, category, expiresAt ->
                        UnchannelledFury(player, category, expiresAt)
                    }
                }

                //
                // Special Rules Category
                //
                SkillType.SNEAKIEST_OF_THE_LOT -> {
                    addNoValueEntry("Sneakiest of the Lot", type, SkillCategory.SPECIAL_RULES) { player, category, expiresAt ->
                        SneakiestOfTheLot(player, category, expiresAt)
                    }
                }

                // Skills not supported in this ruleset (or not supported yet)
                SkillType.BULLSEYE,
                SkillType.DRUNKARD,
                SkillType.EYE_GOUGE,
                SkillType.GIVE_AND_GO,
                SkillType.HATRED,
                SkillType.INSIGNIFICANT,
                SkillType.LETHAL_FLIGHT,
                SkillType.LONE_FOULER,
                SkillType.MY_BALL,
                SkillType.NO_BALL,
                SkillType.PICK_ME_UP,
                SkillType.PUNT,
                SkillType.PUT_THE_BOOT_IN,
                SkillType.QUICK_FOUL,
                SkillType.SABOTEUR,
                SkillType.STEADY_FOOTING,
                SkillType.TAUNT,
                SkillType.TRICKSTER,
                SkillType.UNSTEADY,
                SkillType.VIOLENT_INNOVATOR -> {
                    // Ignore
                }
            }
        }
    }
}
