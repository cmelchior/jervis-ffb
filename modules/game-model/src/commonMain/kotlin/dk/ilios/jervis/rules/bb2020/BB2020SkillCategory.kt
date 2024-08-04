package dk.ilios.jervis.rules.bb2020

import dk.ilios.jervis.rules.skills.SkillCategory
import kotlinx.serialization.Serializable

@Serializable
sealed class BB2020SkillCategory(
    override val id: Long,
    override val name: String,
) : SkillCategory

@Serializable
data object Agility : BB2020SkillCategory(1, "Agility")

@Serializable
data object General : BB2020SkillCategory(2, "General")

@Serializable
data object Mutations : BB2020SkillCategory(3, "Mutations")

@Serializable
data object Passing : BB2020SkillCategory(4, "Passing")

@Serializable
data object Strength : BB2020SkillCategory(5, "Strength")

@Serializable
data object Traits : BB2020SkillCategory(6, "Traits")

@Serializable
data object StatIncrease : BB2020SkillCategory(7, "Stat Increase")
