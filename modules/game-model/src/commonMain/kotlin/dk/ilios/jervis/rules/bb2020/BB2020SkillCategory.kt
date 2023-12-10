package dk.ilios.jervis.rules.bb2020

import dk.ilios.jervis.rules.skills.SkillCategory

sealed class BB2020SkillCategory(id: Long, name: String): SkillCategory {
    override val id: Long = id
    override val name: String = name
}
data object Agility: BB2020SkillCategory(1, "Agility")
data object General: BB2020SkillCategory(1, "General")
data object Passing: BB2020SkillCategory(1, "Passing")
data object Strength: BB2020SkillCategory(1, "Strength")
data object Traits: BB2020SkillCategory(1, "Traits")