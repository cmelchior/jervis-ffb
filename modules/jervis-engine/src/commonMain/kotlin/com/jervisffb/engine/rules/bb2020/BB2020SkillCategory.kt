package com.jervisffb.engine.rules.bb2020

import com.jervisffb.engine.rules.bb2020.skills.AnimalSavagery
import com.jervisffb.engine.rules.bb2020.skills.Block
import com.jervisffb.engine.rules.bb2020.skills.BloodLust
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
import com.jervisffb.engine.rules.bb2020.skills.SkillCategory
import com.jervisffb.engine.rules.bb2020.skills.SkillFactory
import com.jervisffb.engine.rules.bb2020.skills.Sprint
import com.jervisffb.engine.rules.bb2020.skills.Stab
import com.jervisffb.engine.rules.bb2020.skills.Stunty
import com.jervisffb.engine.rules.bb2020.skills.SureFeet
import com.jervisffb.engine.rules.bb2020.skills.SureHands
import com.jervisffb.engine.rules.bb2020.skills.Tackle
import com.jervisffb.engine.rules.bb2020.skills.ThickSkull
import com.jervisffb.engine.rules.bb2020.skills.Timmmber
import com.jervisffb.engine.rules.bb2020.skills.Titchy
import com.jervisffb.engine.rules.bb2020.skills.TwoHeads
import com.jervisffb.engine.rules.bb2020.skills.UnchannelledFury
import com.jervisffb.engine.rules.bb2020.skills.Wrestle
import com.jervisffb.engine.rules.bb2020.specialrules.SneakiestOfTheLot
import kotlinx.serialization.Serializable

@Serializable
enum class BB2020SkillCategory(override val id: Long, override val description: String, val skills: List<SkillFactory>): SkillCategory {
    AGILITY(1, "Agility", listOf(
        CatchSkill.Factory,
        /* DivingCatch.Factory */
        DivingTackle.Factory,
        Dodge.Factory,
        /* Defensive.Factory */
        /* JumpUp.Factory */
        Leap.Factory,
        /* SafePairOfHands.Factory */
        Sidestep.Factory,
        /* SneakyGit.Factory */
        Sprint.Factory,
        SureFeet.Factory,
    )),
    GENERAL(2, "General", listOf(
        Block.Factory,
        Pro.Factory,
        Sprint.Factory,
        SureFeet.Factory,
        SureHands.Factory,
        Tackle.Factory,
        Wrestle.Factory,
    )),
    MUTATIONS(3, "Mutations", listOf(
        Horns.Factory,
        TwoHeads.Factory,
    )),
    PASSING(4, "Passing", listOf(
        Pass.Factory,
    )),
    STRENGTH(5, "Strength", listOf(
        BreakTackle.Factory,
        MightyBlow.Factory(1),
        MultipleBlock.Factory,
        ThickSkull.Factory,
    )),
    TRAITS(6, "Traits", listOf(
        AnimalSavagery.Factory,
        BloodLust.Factory(2),
        BreatheFire.Factory,
        Frenzy.Factory,
        Loner.Factory(4),
        PrehensileTail.Factory,
        ProjectileVomit.Factory,
        ReallyStupid.Factory,
        Regeneration.Factory,
        Stab.Factory,
        Stunty.Factory,
        Timmmber.Factory,
        Titchy.Factory,
        UnchannelledFury.Factory,
    )),
    SPECIAL_RULES(7, "Special Rules", listOf(
        SneakiestOfTheLot.Factory
    ))
}

