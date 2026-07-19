package com.jervisffb.engine.rules.common.skills

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerKeyword
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.model.SkillValue
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * This abstract defines which skills are available inside a ruleset, how they are created and to which
 * categories they are assigned. Each subclass should be tailored for a specific ruleset.
 *
 * Right now, it isn't possible to configure it, but hopefully it is flexible enough, so we can
 * add customization support with relatively little work.
 *
 * @see com.jervisffb.engine.rules.Rules.skillSettings
 */
@Serializable
abstract class SkillSettings {

    // Map between skill type and their factories
    @Transient
    private val skillCache = mutableMapOf<SkillType, SkillFactory<*>>()
    // Map between SkillCategory and skills part of it.
    @Transient
    private val categories = mutableMapOf<SkillCategory, MutableList<SkillFactory<*>>>()

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

    // Supports things like "Loner (4+)", "Mighty Blow (+1)" or "Hatred (Big Guy)",
    // We also allow negative modifiers for examples like "Weak (-1)", but not
    // "Name (42-)" since you cannot roll negative values on a die. Fixed values
    // like "Chance (6)" is also not allowed.
    @Transient
    private val niceRegex = "(^[a-zA-Z\\- ]+)\\s*(\\(([\\-+]\\d+|\\d+\\+|([a-zA-Z ]+))\\))?$".toRegex()

    /** Will match strings that return the format exposed by [SkillId.serialize] */
    @Transient
    private val skillIdRegex = "(^[a-zA-Z_]+)(\\((.+)\\))?$".toRegex()

    /**
     * Converts a string to the appropriate [SkillId], or return `null` if the skill name could not be mapped
     * to a supported skill in this ruleset.
     *
     * The format is expected to be "nice", this can probably vary between APIs, but it looks something like this:
     * - "Mighty Blow (+1)"
     * - "Mighty Blow(-1)"
     *
     * This function should be used when importing skills from TourPlay and FUMBBL.s
     */
    fun getSkillIdFromNiceDescription(niceSkillName: String): SkillId? {
        return niceRegex.matchEntire(niceSkillName)?.let { match ->
            val name = match.groups[1]?.value?.trim() ?: error("Failed to find skill name in string: $niceSkillName")
            val value = match.groups[3]?.value
            val skillValue = extractValueType(value)
            skillCache.keys.firstOrNull { it.description == name }?.let { skillType ->
                SkillId(skillType, skillValue)
            }
        }
    }

    /**
     * Converts a string to the appropriate [SkillId], or return `null` if the skill name could not be mapped
     * to a supported skill in this ruleset.
     *
     * [serializedSkillId] is expected to have a similar format to [SkillId.serialize], example: "MIGHTY_BLOW(+1)"
     */
    fun getSkillId(serializedSkillId: String): SkillId? {
        // Split name into name and a value
        return skillIdRegex.matchEntire(serializedSkillId)?.let { match ->
            // Should be equal to the enum name for the SkillType, e.g. "BLOCK" for SkillType.BLOCK
            val name = match.groups[1]?.value?.trim() ?: error("Failed to find skill name in string: $serializedSkillId")
            // The "int" value for a skill. e.g. "MIGHTY_BLOW(+1)"
            val value = match.groups[3]?.value
            val skillValue = extractValueType(value)
            skillCache.keys.firstOrNull { type -> type.name == name }?.let { skillType ->
                SkillId(skillType, skillValue)
            }
        }
    }

    // This method assumes that the value was matched using the skill regex, which limits
    // the scenarios we need to check for
    private fun extractValueType(value: String?): SkillValue {
        if (value == null) return SkillValue.None
        return if (value.startsWith("+") || value.startsWith("-")) {
            value.toIntOrNull()?.let { intAdjustment ->
                SkillValue.IntAdjustment(intAdjustment)
            } ?: SkillValue.None
        } else if (value.endsWith("+")) {
            value.substringBeforeLast("+").toIntOrNull()?.let { intTaget ->
                SkillValue.IntTarget(intTaget)
            } ?: SkillValue.None
        } else {
            // TODO: It is not clear how Tourplay and Fumbble represent Hatred and Animosity
            val keywordValue = PlayerKeyword.entries.firstOrNull { it.description.equals(value, ignoreCase = true) }
            when (keywordValue != null) {
                true -> SkillValue.Keyword(keywordValue)
                false -> SkillValue.None
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
    fun getAvailableSkillsIds(category: SkillCategory): List<SkillId> {
        return categories[category]?.map {
            it.defaultSkillId
        } ?: error("Skill category not supported: $category")
    }
    fun getAvailableSkills(category: SkillCategory): List<SkillFactory<*>> {
        return categories[category] ?: error("Skill category not supported: $category")
    }

    /**
     * Creates a skill for a player using the current ruleset settings. If the SkillId
     * is associated with a skill (or skill variant) not supporte, an error is thrown.
     */
    fun createSkill(
        player: Player,
        skill: SkillId,
        expiresAt: Duration
    ): Skill<*> {
        // If a SkillFactory is created with a default value, we allow parsing in SkillIds
        // with a `null` value. In which case, they will just fall back to the default.
        // The reason for this is mostly compatibility with FUMBBL which seems to be using
        // both skills like "Mighty Blow" or "Mighty Blow (+1)", depending on how old
        // the roster is.
        val factory = skillCache[skill.type] ?: error("Cannot find skill factory for skill: ${skill.type}")
        return factory.createSkill(player, skill.value, expiresAt)
    }

    /**
     * Creates a skill for a player using the current ruleset settings.
     */
    fun createSkill(
        player: Player,
        type: SkillType,
        expiresAt: Duration
    ): Skill<*> {
        return skillCache[type]?.createSkill(player, null, expiresAt) ?: error("Cannot find skill factory for skill: $type")
    }

    // Helper method for populating the `skillCache` and `categories` mappings. Should only be called from
    // `initializeSkillCache()`

    // Creates a new skill that has an associated Int value that acts as a target, like Loner (4+)
    protected fun addIntTargetEntry(
        name: String,
        type: SkillType,
        category: SkillCategory,
        defaultValue: Int? = null,
        createFunc: (Player, SkillCategory, Int?, Duration) -> Skill<Int>
    ) {
        val entry = IntTargetSkillFactory(name, type, category, defaultValue, createFunc)
        skillCache[type] = entry
        categories[category]?.add(entry) ?: error("Cannot find category: ${category.name}")
    }

    // Creates a new skill that has Int adjustment to some value, like Mighty Block (+1)
    protected fun addIntAdjustmentEntry(
        name: String,
        type: SkillType,
        category: SkillCategory,
        defaultValue: Int? = null,
        createFunc: (Player, SkillCategory, Int?, Duration) -> Skill<Int>
    ) {
        val entry = IntAdjustmentSkillFactory(name, type, category, defaultValue, createFunc)
        skillCache[type] = entry
        categories[category]?.add(entry) ?: error("Cannot find category: ${category.name}")
    }

    // Creates a new skill that doesn't have an associate value, e.g. Dodge or Block.
    protected fun addNoValueEntry(
        name: String,
        type: SkillType,
        category: SkillCategory,
        createFunc: (Player, SkillCategory, Duration) -> Skill<Unit>
    ) {
        val entry = NoValueSkillFactory(name, type, category, createFunc)
        skillCache[type] = entry
        categories[category]?.add(entry) ?: error("Cannot find category: ${category.name}")
    }

    // Creates a new skill that has an associated Keyword value e.g. Hatred (Big Guy)
    protected fun addKeywordEntry(
        name: String,
        type: SkillType,
        category: SkillCategory,
        defaultValue: PlayerKeyword? = null,
        createFunc: (Player, SkillCategory, PlayerKeyword?, Duration) -> Skill<PlayerKeyword>
    ) {
        val entry = KeywordSkillFactory(name, type, category, defaultValue, createFunc)
        skillCache[type] = entry
        categories[category]?.add(entry) ?: error("Cannot find category: ${category.name}")
    }
}
