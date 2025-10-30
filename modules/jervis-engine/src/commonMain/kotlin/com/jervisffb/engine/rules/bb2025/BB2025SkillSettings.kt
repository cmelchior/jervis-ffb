package com.jervisffb.engine.rules.bb2025

import com.jervisffb.engine.rules.bb2025.skills.AnimalSavagery
import com.jervisffb.engine.rules.bb2025.skills.Block
import com.jervisffb.engine.rules.bb2025.skills.BloodLust
import com.jervisffb.engine.rules.bb2025.skills.BoneHead
import com.jervisffb.engine.rules.bb2025.skills.BreakTackle
import com.jervisffb.engine.rules.bb2025.skills.BreatheFire
import com.jervisffb.engine.rules.bb2025.skills.CatchSkill
import com.jervisffb.engine.rules.bb2025.skills.DivingTackle
import com.jervisffb.engine.rules.bb2025.skills.Dodge
import com.jervisffb.engine.rules.bb2025.skills.Frenzy
import com.jervisffb.engine.rules.bb2025.skills.Horns
import com.jervisffb.engine.rules.bb2025.skills.Leader
import com.jervisffb.engine.rules.bb2025.skills.Leap
import com.jervisffb.engine.rules.bb2025.skills.Loner
import com.jervisffb.engine.rules.bb2025.skills.MightyBlow
import com.jervisffb.engine.rules.bb2025.skills.MultipleBlock
import com.jervisffb.engine.rules.bb2025.skills.Pass
import com.jervisffb.engine.rules.bb2025.skills.PrehensileTail
import com.jervisffb.engine.rules.bb2025.skills.Pro
import com.jervisffb.engine.rules.bb2025.skills.ProjectileVomit
import com.jervisffb.engine.rules.bb2025.skills.ReallyStupid
import com.jervisffb.engine.rules.bb2025.skills.Regeneration
import com.jervisffb.engine.rules.bb2025.skills.RightStuff
import com.jervisffb.engine.rules.bb2025.skills.Sidestep
import com.jervisffb.engine.rules.bb2025.skills.Sprint
import com.jervisffb.engine.rules.bb2025.skills.Stab
import com.jervisffb.engine.rules.bb2025.skills.Stunty
import com.jervisffb.engine.rules.bb2025.skills.SureFeet
import com.jervisffb.engine.rules.bb2025.skills.SureHands
import com.jervisffb.engine.rules.bb2025.skills.Tackle
import com.jervisffb.engine.rules.bb2025.skills.ThickSkull
import com.jervisffb.engine.rules.bb2025.skills.ThrowTeamMate
import com.jervisffb.engine.rules.bb2025.skills.Timmmber
import com.jervisffb.engine.rules.bb2025.skills.Titchy
import com.jervisffb.engine.rules.bb2025.skills.UnchannelledFury
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
                    addNoValueEntry(type, SkillCategory.AGILITY) { player, category,expiresAt ->
                        CatchSkill(player, category, expiresAt)
                    }
                }
                SkillType.DIVING_CATCH -> {
                    // addEntry(type, SkillCategory.AGILITY) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.DIVING_TACKLE -> {
                    addNoValueEntry(type, SkillCategory.AGILITY) { player, category,expiresAt ->
                        DivingTackle(player, category, expiresAt)
                    }
                }
                SkillType.DODGE -> {
                    addNoValueEntry(type, SkillCategory.AGILITY) { player, category,expiresAt ->
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
                    addNoValueEntry(type, SkillCategory.AGILITY) { player, category,expiresAt ->
                        Leap(player, category, expiresAt)
                    }
                }
                SkillType.SAFE_PAIR_OF_HANDS -> {
                    // addEntry(type, SkillCategory.AGILITY) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.SIDESTEP -> {
                    addNoValueEntry(type, SkillCategory.AGILITY) { player, category,expiresAt ->
                        Sidestep(player, category, expiresAt)
                    }
                }
                SkillType.SPRINT -> {
                    addNoValueEntry(type, SkillCategory.AGILITY) { player, category,expiresAt ->
                        Sprint(player, category, expiresAt)
                    }
                }
                SkillType.SURE_FEET -> {
                    addNoValueEntry(type, SkillCategory.AGILITY) { player, category,expiresAt ->
                        SureFeet(player, category, expiresAt)
                    }
                }

                //
                // Devious Category
                //
                SkillType.DIRTY_PLAYER -> {
                    // addEntry(type, SkillCategory.DEVIOUS, 1) { player, category, value , expiresAt ->
                    // TODO()
                    // }
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
                    // addEntry(type, SkillCategory.DEVIOUS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.QUICK_FOUL -> {
                    // addEntry(type, SkillCategory.DEVIOUS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.SABOTEUR -> {
                    // addEntry(type, SkillCategory.DEVIOUS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.SHADOWING -> {
                    // addEntry(type, SkillCategory.DEVIOUS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
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
                    addNoValueEntry(type, SkillCategory.GENERAL) { player, category,expiresAt ->
                        Block(player, category, expiresAt)
                    }
                }
                SkillType.DAUNTLESS -> {
                    // addEntry(type, SkillCategory.GENERAL) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.FEND -> {
                    // addEntry(type, SkillCategory.GENERAL) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.FRENZY -> {
                    addNoValueEntry(type, SkillCategory.GENERAL) { player, category,expiresAt ->
                        Frenzy(player, category, expiresAt)
                    }
                }
                SkillType.KICK -> {
                    // addEntry(type, SkillCategory.GENERAL) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.PRO -> {
                    addNoValueEntry(type, SkillCategory.GENERAL) { player, category,expiresAt ->
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
                    addNoValueEntry(type, SkillCategory.GENERAL) { player, category,expiresAt ->
                        SureHands(player, category, expiresAt)
                    }
                }
                SkillType.TACKLE -> {
                    addNoValueEntry(type, SkillCategory.GENERAL) { player, category,expiresAt ->
                        Tackle(player, category, expiresAt)
                    }
                }
                SkillType.TAUNT -> {
                    // addEntry(type, SkillCategory.GENERAL) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.WRESTLE -> {
                    addNoValueEntry(type, SkillCategory.GENERAL) { player, category,expiresAt ->
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
                    addNoValueEntry(type, SkillCategory.MUTATIONS) { player, category,expiresAt ->
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
                    addNoValueEntry(type, SkillCategory.MUTATIONS) { player, category,expiresAt ->
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
                SkillType.GIVE_AND_GO -> {
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
                    addNoValueEntry(type, SkillCategory.PASSING) { player, category,expiresAt ->
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
                    addNoValueEntry(type, SkillCategory.PASSING) { player, category,expiresAt ->
                        Pass(player, category, expiresAt)
                    }
                }
                SkillType.PUNT -> {
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
                    addNoValueEntry(type, SkillCategory.STRENGTH) { player, category,expiresAt ->
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
                    addIntEntry(type, SkillCategory.STRENGTH, 1) { player, category, _, expiresAt ->
                        MightyBlow(player, category, expiresAt)
                    }
                }
                SkillType.MULTIPLE_BLOCK -> {
                    addNoValueEntry(type, SkillCategory.STRENGTH) { player, category,expiresAt ->
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
                    addNoValueEntry(type, SkillCategory.STRENGTH) { player, category,expiresAt ->
                        ThickSkull(player, category, expiresAt)
                    }
                }

                //
                // Traits Category
                //
                SkillType.ANIMAL_SAVAGERY -> {
                    addNoValueEntry(type, SkillCategory.TRAITS) { player, category,expiresAt ->
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
                    addNoValueEntry(type, SkillCategory.TRAITS) { player, category,expiresAt ->
                        BoneHead(player, category, expiresAt)
                    }
                }
                SkillType.BLOOD_LUST -> {
                    addIntEntry(type, SkillCategory.TRAITS, 4) { player, category, value, expiresAt ->
                        BloodLust(player, category, value, expiresAt)
                    }
                }
                SkillType.BREATHE_FIRE -> {
                    addNoValueEntry(type, SkillCategory.TRAITS) { player, category,expiresAt ->
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
                    addNoValueEntry(type, SkillCategory.TRAITS) { player, category,expiresAt ->
                        TODO()
                    }
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
                SkillType.INSIGNIFICANT -> {
                    // TODO()
                }
                SkillType.KICK_TEAMMATE -> {
                    // addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.LONER -> {
                    addIntEntry(type, SkillCategory.TRAITS, 4) { player, category, value, expiresAt ->
                        Loner(player, category, value, expiresAt)
                    }
                }
                SkillType.MY_BALL -> {
                    // TODO()
                }
                SkillType.NO_BALL -> {
                    // TODO
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
                    addNoValueEntry(type, SkillCategory.TRAITS) { player, category,expiresAt ->
                        ProjectileVomit(player, category, expiresAt)
                    }
                }
                SkillType.REALLY_STUPID -> {
                    addNoValueEntry(type, SkillCategory.TRAITS) { player, category,expiresAt ->
                        ReallyStupid(player, category, expiresAt)
                    }
                }
                SkillType.REGENERATION -> {
                    addNoValueEntry(type, SkillCategory.TRAITS) { player, category,expiresAt ->
                        Regeneration(player, category, expiresAt)
                    }
                }
                SkillType.RIGHT_STUFF -> {
                    addNoValueEntry(type, SkillCategory.TRAITS) { player, category,expiresAt ->
                        RightStuff(player, category, expiresAt)
                    }
                }
                SkillType.SECRET_WEAPON -> {
                    // addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.STAB -> {
                    addNoValueEntry(type, SkillCategory.TRAITS) { player, category,expiresAt ->
                        Stab(player, category, expiresAt)
                    }
                }
                SkillType.STUNTY -> {
                    addNoValueEntry(type, SkillCategory.TRAITS) { player, category,expiresAt ->
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
                    addNoValueEntry(type, SkillCategory.TRAITS) { player, category,expiresAt ->
                        Titchy(player, category, expiresAt)
                    }
                }
                SkillType.TIMMMBER -> {
                    addNoValueEntry(type, SkillCategory.TRAITS) { player, category,expiresAt ->
                        Timmmber(player, category, expiresAt)
                    }
                }
                SkillType.THROW_TEAMMATE -> {
                    addNoValueEntry(type, SkillCategory.TRAITS) { player, category,expiresAt ->
                        ThrowTeamMate(player, category, expiresAt)
                    }
                }
                SkillType.TRICKSTER -> {

                }
                SkillType.UNCHANNELLED_FURY -> {
                    addNoValueEntry(type, SkillCategory.TRAITS) { player, category,expiresAt ->
                        UnchannelledFury(player, category, expiresAt)
                    }
                }
                SkillType.UNSTEADY -> {
                    // TODO()
                }

                //
                // Special Rules Category
                //
                SkillType.SNEAKIEST_OF_THE_LOT -> {
                    addNoValueEntry(type, SkillCategory.SPECIAL_RULES) { player, category,expiresAt ->
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
