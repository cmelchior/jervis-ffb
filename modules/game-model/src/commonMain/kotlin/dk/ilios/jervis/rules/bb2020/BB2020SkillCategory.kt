package dk.ilios.jervis.rules.bb2020

import dk.ilios.jervis.rules.skills.CatchSkill
import dk.ilios.jervis.rules.skills.DivingTackle
import dk.ilios.jervis.rules.skills.Dodge
import dk.ilios.jervis.rules.skills.SkillCategory
import dk.ilios.jervis.rules.skills.SkillFactory
import dk.ilios.jervis.rules.skills.Sprint
import dk.ilios.jervis.rules.skills.SureFeet
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

