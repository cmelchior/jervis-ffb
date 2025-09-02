package com.jervisffb.engine.rules.common.skills

/**
 * All Skill Categories across all rulesets. Whether or not they are supported
 * is defined by the relevant [SkillSettings]
 */
enum class SkillCategory(val description: String) {
    AGILITY("Agility"),
    GENERAL("General"),
    MUTATIONS("Mutations"),
    PASSING("Passing"),
    STRENGTH("Strength"),
    TRAITS("Traits"),
    SPECIAL_RULES("Special Rules"),
}
