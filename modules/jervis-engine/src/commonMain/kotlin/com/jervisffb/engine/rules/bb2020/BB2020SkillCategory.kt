package com.jervisffb.engine.rules.bb2020

import com.jervisffb.engine.rules.bb2020.skills.CatchSkill
import com.jervisffb.engine.rules.bb2020.skills.DivingTackle
import com.jervisffb.engine.rules.bb2020.skills.Dodge
import com.jervisffb.engine.rules.bb2020.skills.SkillCategory
import com.jervisffb.engine.rules.bb2020.skills.SkillFactory
import com.jervisffb.engine.rules.bb2020.skills.Sprint
import com.jervisffb.engine.rules.bb2020.skills.SureFeet
import kotlinx.serialization.Serializable

@Serializable
enum class BB2020SkillCategory(override val id: Long, override val description: String, val skills: List<SkillFactory>):
    SkillCategory {
    AGILITY(1, "Agility", listOf(
        CatchSkill.Factory,
        /* DivingCatch.Factory */
        DivingTackle.Factory,
        Dodge.Factory,
        /* Defensive.Factory */
        /* JumpUp.Factory */
        /* Leap.Factory */
        /* SafePairOfHands.Factory */
        /* SideStep.Factory, */
        /* SneakyGit.Factory */
        Sprint.Factory,
        SureFeet.Factory,
    )),
    GENERAL(2, "General", listOf()),
    MUTATIONS(3, "Mutations", listOf()),
    PASSING(4, "Passing", listOf()),
    STRENGTH(5, "Strength", listOf()),
    TRAITS(6, "Traits", listOf()),
}

