package dk.ilios.jervis.rules.bb2020

import dk.ilios.jervis.rules.skills.SkillCategory

sealed class BB2020SkillCategory(id: Long, name: String): SkillCategory {
    override val id: Long = id
    override val name: String = name
}
data object Agility: BB2020SkillCategory(1, "Agility")
data object General: BB2020SkillCategory(2, "General")
data object Mutations: BB2020SkillCategory(3, "Mutations")
data object Passing: BB2020SkillCategory(4, "Passing")
data object Strength: BB2020SkillCategory(5, "Strength")
data object Traits: BB2020SkillCategory(6, "Traits")
data object StatIncrease: BB2020SkillCategory(7, "Stat Increase")

