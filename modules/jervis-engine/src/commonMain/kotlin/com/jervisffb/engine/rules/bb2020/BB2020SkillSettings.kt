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
import com.jervisffb.engine.rules.bb2020.skills.Sidestep
import com.jervisffb.engine.rules.bb2020.skills.Sprint
import com.jervisffb.engine.rules.bb2020.skills.Stab
import com.jervisffb.engine.rules.bb2020.skills.Stunty
import com.jervisffb.engine.rules.bb2020.skills.SureFeet
import com.jervisffb.engine.rules.bb2020.skills.SureHands
import com.jervisffb.engine.rules.bb2020.skills.Tackle
import com.jervisffb.engine.rules.bb2020.skills.ThickSkull
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
                    addEntry(type, SkillCategory.AGILITY) { player, category, _ , expiresAt ->
                        CatchSkill(player, category, expiresAt)
                    }
                }
                SkillType.DIVING_CATCH -> {
                    // addEntry(type, SkillCategory.AGILITY) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.DIVING_TACKLE -> {
                    addEntry(type, SkillCategory.AGILITY) { player, category, _ , expiresAt ->
                        DivingTackle(player, category, expiresAt)
                    }
                }
                SkillType.DODGE -> {
                    addEntry(type, SkillCategory.AGILITY) { player, category, _ , expiresAt ->
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
                    addEntry(type, SkillCategory.AGILITY) { player, category, _ , expiresAt ->
                        Leap(player, category, expiresAt)
                    }
                }
                SkillType.SAFE_PAIR_OF_HANDS -> {
                    // addEntry(type, SkillCategory.AGILITY) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.SIDESTEP -> {
                    addEntry(type, SkillCategory.AGILITY) { player, category, _ , expiresAt ->
                        Sidestep(player, category, expiresAt)
                    }
                }
                SkillType.SNEAKY_GIT -> {
                    // addEntry(type, SkillCategory.AGILITY) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.SPRINT -> {
                    addEntry(type, SkillCategory.AGILITY) { player, category, _ , expiresAt ->
                        Sprint(player, category, expiresAt)
                    }
                }
                SkillType.SURE_FEET -> {
                    addEntry(type, SkillCategory.AGILITY) { player, category, _ , expiresAt ->
                        SureFeet(player, category, expiresAt)
                    }
                }

                //
                // General Category
                //
                SkillType.BLOCK -> {
                    addEntry(type, SkillCategory.GENERAL) { player, category, _ , expiresAt ->
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
                    addEntry(type, SkillCategory.GENERAL) { player, category, _ , expiresAt ->
                        Frenzy(player, category, expiresAt)
                    }
                }
                SkillType.KICK -> {
                    // addEntry(type, SkillCategory.GENERAL) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.PRO -> {
                    addEntry(type, SkillCategory.GENERAL) { player, category, _ , expiresAt ->
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
                    addEntry(type, SkillCategory.GENERAL) { player, category, _ , expiresAt ->
                        SureHands(player, category, expiresAt)
                    }
                }
                SkillType.TACKLE -> {
                    addEntry(type, SkillCategory.GENERAL) { player, category, _ , expiresAt ->
                        Tackle(player, category, expiresAt)
                    }
                }
                SkillType.WRESTLE -> {
                    addEntry(type, SkillCategory.GENERAL) { player, category, _ , expiresAt ->
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
                    addEntry(type, SkillCategory.MUTATIONS) { player, category, _ , expiresAt ->
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
                    addEntry(type, SkillCategory.MUTATIONS) { player, category, _ , expiresAt ->
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
                    // addEntry(type, SkillCategory.PASSING) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
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
                    addEntry(type, SkillCategory.PASSING) { player, category, _ , expiresAt ->
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
                    addEntry(type, SkillCategory.STRENGTH) { player, category, _ , expiresAt ->
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
                    addEntry(type, SkillCategory.STRENGTH, 1) { player, category, value , expiresAt ->
                        MightyBlow(player, category, value, expiresAt)
                    }
                }
                SkillType.MULTIPLE_BLOCK -> {
                    addEntry(type, SkillCategory.STRENGTH) { player, category, _ , expiresAt ->
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
                    addEntry(type, SkillCategory.STRENGTH) { player, category, _ , expiresAt ->
                        ThickSkull(player, category, expiresAt)
                    }
                }

                //
                // Traits Category
                //
                SkillType.ANIMAL_SAVAGERY -> {
                    addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
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
                    addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                        BoneHead(player, category, expiresAt)
                    }
                }
                SkillType.BLOOD_LUST -> {
                    addEntry(type, SkillCategory.TRAITS, 4) { player, category, value , expiresAt ->
                        BloodLust(player, category, value, expiresAt)
                    }
                }
                SkillType.BREATHE_FIRE -> {
                    addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
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
                    addEntry(type, SkillCategory.TRAITS, 4) { player, category, value , expiresAt ->
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
                    addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                        ProjectileVomit(player, category, expiresAt)
                    }
                }
                SkillType.REALLY_STUPID -> {
                    addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                        ReallyStupid(player, category, expiresAt)
                    }
                }
                SkillType.REGENERATION -> {
                    addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                        Regeneration(player, category, expiresAt)
                    }
                }
                SkillType.RIGHT_STUFF -> {
                    // addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.SECRET_WEAPON -> {
                    // addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.STAB -> {
                    addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                        Stab(player, category, expiresAt)
                    }
                }
                SkillType.STUNTY -> {
                    addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
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
                    addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                        Titchy(player, category, expiresAt)
                    }
                }
                SkillType.TIMMMBER -> {
                    addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                        Timmmber(player, category, expiresAt)
                    }
                }
                SkillType.THROW_TEAMMATE -> {
                    // addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.UNCHANNELLED_FURY -> {
                    addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                        UnchannelledFury(player, category, expiresAt)
                    }
                }

                //
                // Special Rules Category
                //
                SkillType.SNEAKIEST_OF_THE_LOT -> {
                    addEntry(type, SkillCategory.SPECIAL_RULES) { player, category, _ , expiresAt ->
                        SneakiestOfTheLot(player, category, expiresAt)
                    }
                }
            }
        }
    }
}
