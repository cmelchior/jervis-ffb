package com.jervisffb.engine.rules.common.skills

/**
 * All Skill Categories across all rulesets. Whether they are supported
 * is defined by the relevant [SkillSettings]
 */
enum class SkillCategory(val description: String) {
    AGILITY("Agility"),
    GENERAL("General"),
    DEVIOUS("Devious"),
    MUTATIONS("Mutations"),
    PASSING("Passing"),
    STRENGTH("Strength"),
    TRAITS("Traits"),
    // Technically, player Special Rules are not skills, it just makes it a
    // easier to treat them that way.
    //
    // The dividing line between a `PlayerSpecialRule` and a `Skill` with the
    // `SkillCategory.SPECIAL_RULE` category isn't 100% clear, but generally
    // all star players special rules are treated as skills, and other things
    // like inducement special rules are treated as player special rules.
    SPECIAL_RULES("Special Rules"),
}
