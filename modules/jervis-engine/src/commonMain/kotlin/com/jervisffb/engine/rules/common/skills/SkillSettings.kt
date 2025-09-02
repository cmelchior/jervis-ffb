package com.jervisffb.engine.rules.common.skills

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.SkillId
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * This abstract defines which skills are available inside a ruleset, how they are created and to which
 * categories they are assigned. Each subclass should be tailored for a specific ruleset.
 *
 * Right now, it isn't possible to configure it, but hopefully it is flexible enough, so we can
 * add customization support with relatively little work.
 *
 * @see com.jervisffb.engine.rules.Rules.getSkillFactory
 * @see com.jervisffb.engine.rules.Rules.skillSettings
 */
@Serializable
abstract class SkillSettings {

    // Wrapper class that manages creation of skills
    private class SkillFactory(
        private val type: SkillType,
        private val category: SkillCategory,
        private val defaultValue: Int?,
        private val createFunc: (Player, SkillCategory, Int?, Duration) -> Skill
    ) {
        val defaultSkillId: SkillId = SkillId(type, defaultValue)

        fun createSkill(player: Player, value: Int? = null, expiresAt: Duration): Skill {
            return createFunc(player, category, value, expiresAt)
        }
        // Creates the skill as it is listed in the categories sections in the rulebook.
        // This is only relevant for skills wit values like "Loner (4+)" or "Mighty Blow (+1)".
        fun createDefaultSkill(player: Player, expiresAt: Duration): Skill {
            return createSkill(player, defaultValue, expiresAt)
        }
    }

    // Map between skill type and their factories
    @Transient
    private val skillCache = mutableMapOf<SkillType, SkillFactory>()
    // Map between SkillCategory and skills part of it.
    @Transient
    private val categories = mutableMapOf<SkillCategory, MutableList<SkillFactory>>()

    init {
        // Initialize skill setup. This defines the mapping between type, category and value and the factory
        // for creating instances of the skill. For now, just keep the setup here, but it probably needs to
        // be refactored when we need to customize skills available and their behavior
        initializeSkillCache()
    }

    // Should be populated by sub-classes, where all setup of skill mappings should happen
    protected abstract fun initializeSkillCache()

    /**
     * Initialize a supported category.
     */
    protected fun addCategory(category: SkillCategory) {
        categories[category] = mutableListOf()
    }

    // Supports things like "Loner (4+)" or "Mighty Blow (+1)"
    // We also allow negative modifiers for examples like "Weak (-1)", but not
    // "Name (42-)" since you cannot roll negative values on a die.
    // Fixed values like "Chance (6)" is also not allowed.
    @Transient
    private val niceRegex = "(^[a-zA-Z\\- ]+)\\s*(\\(([\\-+]\\d+|\\d+\\+)\\))?$".toRegex()

    @Transient
    private val skillIdRegex = "(^[a-zA-Z_]+)(\\((\\d+)\\))?$".toRegex()

    /**
     * Converts a string to the appropriate [SkillId], or return `null` if the skill name could not be mapped
     * to a supported skill in this ruleset.
     *
     * The format is expected to be "nice", this can probably vary between APIs but it looks something like this:
     * - "Mighty Blow (+1)"
     * - "Mighty Blow(-1)"
     */
    fun getSkillIdFromNiceDescription(niceSkillName: String): SkillId? {
        return niceRegex.matchEntire(niceSkillName)?.let { match ->
            val name = match.groups[1]?.value?.trim() ?: error("Failed to find skill name in string: $niceSkillName")
            val value = match.groups[3]?.value?.replace("+", "")?.replace("-", "")?.toInt()
            skillCache.keys.firstOrNull { it.description == name }?.let { skillType ->
                SkillId(skillType, value)
            }
        }
    }

    /**
     * Converts a string to the appropriate [SkillId], or return `null` if the skill name could not be mapped
     * to a supported skill in this ruleset.
     *
     * [serializedSkillId] is expected to have a similar format to [SkillId.serialize], example: "MIGHTY_BLOW(1)"
     */
    fun getSkillId(serializedSkillId: String): SkillId? {
        // Split name into name and a value
        return skillIdRegex.matchEntire(serializedSkillId)?.let { match ->
            val name = match.groups[1]?.value?.trim() ?: error("Failed to find skill name in string: $serializedSkillId")
            val value = match.groups[3]?.value?.replace("+", "")?.replace("-", "")?.toInt()
            skillCache.keys.firstOrNull { type -> type.name == name }?.let { skillType ->
                SkillId(skillType, value)
            }
        }
    }

    /**
     * Returns `true` if the provided [SkillType] is supported by this ruleset, `false` if not.
     */
    fun isSkillSupported(skillType: SkillType): Boolean {
        return skillCache.containsKey(skillType)
    }

    /**
     * Returns all available skills for a given category. If a skill has a value
     * associated with it, it is the value defined in the rulebook that is returned,
     * e.g. "Mighty Blow (+1)" and "Loner (4+)".
     *
     * If the category isn't supported, an [IllegalStateException] is thrown.
     */
    fun getAvailableSkills(category: SkillCategory): List<SkillId> {
        return categories[category]?.map {
            it.defaultSkillId
        } ?: error("Skill category not supported: $category")
    }

    /**
     * Creates a skill for a player using the current ruleset settings.
     */
    fun createSkill(
        player: Player,
        skill: SkillId,
        expiresAt: Duration
    ): Skill {
        // If a SkillFactory is created with a default value, we allow parsing in SkillIds
        // with a `null` value. In which case, they will just fall back to the default.
        // The reason for this is mostly compatibility with FUMBBL which seems to be using
        // both skills like "Mighty Blow" or "Mighty Blow (+1)", depending on how old
        // the roster is.
        val factory = skillCache[skill.type] ?: error("Cannot find skill factory for skill: ${skill.type}")
        return if (factory.defaultSkillId.value != null && skill.value == null) {
            factory.createDefaultSkill(player, expiresAt)
        } else {
            factory.createSkill(player, skill.value, expiresAt)
        }
    }

    /**
     * Creates a skill for a player using the current ruleset settings.
     */
    fun createSkill(
        player: Player,
        type: SkillType,
        expiresAt: Duration
    ): Skill {
        return skillCache[type]?.createDefaultSkill(player, expiresAt) ?: error("Cannot find skill factory for skill: $type")
    }

    // Helper method for populating the `skillCache` and `categories` mappings. Should only be called from
    // `initializeSkillCache()`
    protected fun addEntry(
        type: SkillType,
        category: SkillCategory,
        defaultValue: Int? = null,
        createFunc: (Player, SkillCategory, Int?, Duration) -> Skill
    ) {
        val entry = SkillFactory(type, category, defaultValue, createFunc)
        skillCache[type] = entry
        categories[category]?.add(entry) ?: error("Cannot find category: ${category.name}")
    }
}
