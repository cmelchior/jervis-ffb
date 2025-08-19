package com.jervisffb.engine.rules.bb2020

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.skills.AnimalSavagery
import com.jervisffb.engine.rules.bb2020.skills.Block
import com.jervisffb.engine.rules.bb2020.skills.BloodLust
import com.jervisffb.engine.rules.bb2020.skills.BoneHead
import com.jervisffb.engine.rules.bb2020.skills.BreakTackle
import com.jervisffb.engine.rules.bb2020.skills.BreatheFire
import com.jervisffb.engine.rules.bb2020.skills.CatchSkill
import com.jervisffb.engine.rules.bb2020.skills.DivingTackle
import com.jervisffb.engine.rules.bb2020.skills.Dodge
import com.jervisffb.engine.rules.bb2020.skills.Duration
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
import com.jervisffb.engine.rules.bb2020.skills.Skill
import com.jervisffb.engine.rules.bb2020.skills.SkillCategory
import com.jervisffb.engine.rules.bb2020.skills.SkillType
import com.jervisffb.engine.rules.bb2020.skills.Sprint
import com.jervisffb.engine.rules.bb2020.skills.Stab
import com.jervisffb.engine.rules.bb2020.skills.Stunty
import com.jervisffb.engine.rules.bb2020.skills.SureFeet
import com.jervisffb.engine.rules.bb2020.skills.SureHands
import com.jervisffb.engine.rules.bb2020.skills.Tackle
import com.jervisffb.engine.rules.bb2020.skills.ThickSkull
import com.jervisffb.engine.rules.bb2020.skills.Timmmber
import com.jervisffb.engine.rules.bb2020.skills.Titchy
import com.jervisffb.engine.rules.bb2020.skills.UnchannelledFury
import com.jervisffb.engine.rules.bb2020.skills.Wrestle
import com.jervisffb.engine.rules.bb2020.specialrules.SneakiestOfTheLot
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * This abstract defines which skills are available inside a ruleset, how they are created and to which
 * categories they are assigned. Each subclass should be tailored for a specific ruleset.
 *
 * Right now, it isn't possible to configure it, but hopefully it is flexible enough, so we can
 * add customization support with relatively little work.
 *
 * @see Rules.getSkillFactory
 * @see Rules.skillSettings
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

@Serializable
class BB2020SkillSettings: SkillSettings() {

    // Setup skills as they are defined in the BB2020 Rulebook. See page 74.
    override fun initializeSkillCache() {
        addCategory(SkillCategory.AGILITY)
        addCategory(SkillCategory.GENERAL)
        addCategory(SkillCategory.MUTATIONS)
        addCategory(SkillCategory.PASSING)
        addCategory(SkillCategory.STRENGTH)
        addCategory(SkillCategory.TRAITS)
        addCategory(SkillCategory.SPECIAL_RULES)
        SkillType.entries.forEach { type ->
            when (type) {
                //
                // Agility Category
                //
                SkillType.CATCH -> {
                    addEntry(type, SkillCategory.AGILITY) { player, category, _ , expiresAt ->
                        CatchSkill(player, category, expiresAt)
                    }
                }
                SkillType.DIVING_CATCH -> {
                    // addEntry(type, SkillCategory.AGILITY) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.DIVING_TACKLE -> {
                    addEntry(type, SkillCategory.AGILITY) { player, category, _ , expiresAt ->
                        DivingTackle(player, category, expiresAt)
                    }
                }
                SkillType.DODGE -> {
                    addEntry(type, SkillCategory.AGILITY) { player, category, _ , expiresAt ->
                        Dodge(player, category, expiresAt)
                    }
                }
                SkillType.DEFENSIVE -> {
                    // addEntry(type, SkillCategory.AGILITY) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.JUMP_UP -> {
                    // addEntry(type, SkillCategory.AGILITY) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.LEAP -> {
                    addEntry(type, SkillCategory.AGILITY) { player, category, _ , expiresAt ->
                        Leap(player, category, expiresAt)
                    }
                }
                SkillType.SAFE_PAIR_OF_HANDS -> {
                    // addEntry(type, SkillCategory.AGILITY) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.SIDESTEP -> {
                    addEntry(type, SkillCategory.AGILITY) { player, category, _ , expiresAt ->
                        Sidestep(player, category, expiresAt)
                    }
                }
                SkillType.SNEAKY_GIT -> {
                    // addEntry(type, SkillCategory.AGILITY) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.SPRINT -> {
                    addEntry(type, SkillCategory.AGILITY) { player, category, _ , expiresAt ->
                        Sprint(player, category, expiresAt)
                    }
                }
                SkillType.SURE_FEET -> {
                    addEntry(type, SkillCategory.AGILITY) { player, category, _ , expiresAt ->
                        SureFeet(player, category, expiresAt)
                    }
                }

                //
                // General Category
                //
                SkillType.BLOCK -> {
                    addEntry(type, SkillCategory.GENERAL) { player, category, _ , expiresAt ->
                        Block(player, category, expiresAt)
                    }
                }
                SkillType.DAUNTLESS -> {
                    // addEntry(type, SkillCategory.GENERAL) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.DIRTY_PLAYER -> {
                    // addEntry(type, SkillCategory.GENERAL, 1) { player, category, value , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.FEND -> {
                    // addEntry(type, SkillCategory.GENERAL) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.FRENZY -> {
                    addEntry(type, SkillCategory.GENERAL) { player, category, _ , expiresAt ->
                        Frenzy(player, category, expiresAt)
                    }
                }
                SkillType.KICK -> {
                    // addEntry(type, SkillCategory.GENERAL) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.PRO -> {
                    addEntry(type, SkillCategory.GENERAL) { player, category, _ , expiresAt ->
                        Pro(player, category, expiresAt)
                    }
                }
                SkillType.SHADOWING -> {
                    // addEntry(type, SkillCategory.GENERAL) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.STRIP_BALL -> {
                    // addEntry(type, SkillCategory.GENERAL) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.SURE_HANDS -> {
                    addEntry(type, SkillCategory.GENERAL) { player, category, _ , expiresAt ->
                        SureHands(player, category, expiresAt)
                    }
                }
                SkillType.TACKLE -> {
                    addEntry(type, SkillCategory.GENERAL) { player, category, _ , expiresAt ->
                        Tackle(player, category, expiresAt)
                    }
                }
                SkillType.WRESTLE -> {
                    addEntry(type, SkillCategory.GENERAL) { player, category, _ , expiresAt ->
                        Wrestle(player, category, expiresAt)
                    }
                }

                //
                // Mutations Category
                //
                SkillType.BIG_HAND -> {
                    // addEntry(type, SkillCategory.MUTATIONS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.CLAWS -> {
                    // addEntry(type, SkillCategory.MUTATIONS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.DISTURBING_PRESENCE -> {
                    // addEntry(type, SkillCategory.MUTATIONS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.EXTRA_ARMS -> {
                    // addEntry(type, SkillCategory.MUTATIONS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.FOUL_APPEARANCE -> {
                    // addEntry(type, SkillCategory.MUTATIONS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.HORNS -> {
                    addEntry(type, SkillCategory.MUTATIONS) { player, category, _ , expiresAt ->
                        Horns(player, category, expiresAt)
                    }
                }
                SkillType.IRON_HARD_SKIN -> {
                    // addEntry(type, SkillCategory.MUTATIONS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.MONSTROUS_MOUTH -> {
                    // addEntry(type, SkillCategory.MUTATIONS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.PREHENSILE_TAIL -> {
                    addEntry(type, SkillCategory.MUTATIONS) { player, category, _ , expiresAt ->
                        PrehensileTail(player, category, expiresAt)
                    }
                }
                SkillType.TENTACLE -> {
                    // addEntry(type, SkillCategory.MUTATIONS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.TWO_HEADS -> {
                    // addEntry(type, SkillCategory.MUTATIONS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.VERY_LONG_LEGS -> {
                    // addEntry(type, SkillCategory.MUTATIONS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }

                //
                // Passing Category
                //
                SkillType.ACCURATE -> {
                    // addEntry(type, SkillCategory.PASSING) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.CANNONEER -> {
                    // addEntry(type, SkillCategory.PASSING) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.CLOUD_BURSTER -> {
                    // addEntry(type, SkillCategory.PASSING) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.DUMP_OFF -> {
                    // addEntry(type, SkillCategory.PASSING) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.FUMBLEROOSKIE -> {
                    // addEntry(type, SkillCategory.PASSING) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.HAIL_MARY_PASS -> {
                    // addEntry(type, SkillCategory.PASSING) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.LEADER -> {
                    // addEntry(type, SkillCategory.PASSING) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.NERVES_OF_STEEL -> {
                    // addEntry(type, SkillCategory.PASSING) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.ON_THE_BALL -> {
                    // addEntry(type, SkillCategory.PASSING) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.PASS -> {
                    addEntry(type, SkillCategory.PASSING) { player, category, _ , expiresAt ->
                        Pass(player, category, expiresAt)
                    }
                }
                SkillType.RUNNING_PASS -> {
                    // addEntry(type, SkillCategory.PASSING) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.SAFE_PASS -> {
                    // addEntry(type, SkillCategory.PASSING) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }

                //
                // Strength Category
                //
                SkillType.ARM_BAR -> {
                    // addEntry(type, SkillCategory.STRENGTH) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.BRAWLER -> {
                    // addEntry(type, SkillCategory.STRENGTH) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.BREAK_TACKLE -> {
                    addEntry(type, SkillCategory.STRENGTH) { player, category, _ , expiresAt ->
                        BreakTackle(player, category, expiresAt)
                    }
                }
                SkillType.GRAB -> {
                    // addEntry(type, SkillCategory.STRENGTH) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.GUARD -> {
                    // addEntry(type, SkillCategory.STRENGTH) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.JUGGERNAUT -> {
                    // addEntry(type, SkillCategory.STRENGTH) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.MIGHTY_BLOW -> {
                    addEntry(type, SkillCategory.STRENGTH, 1) { player, category, value , expiresAt ->
                        MightyBlow(player, category, value, expiresAt)
                    }
                }
                SkillType.MULTIPLE_BLOCK -> {
                    addEntry(type, SkillCategory.STRENGTH) { player, category, _ , expiresAt ->
                        MultipleBlock(player, category, expiresAt)
                    }
                }
                SkillType.PILE_DRIVER -> {
                    // addEntry(type, SkillCategory.STRENGTH) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.STAND_FIRM -> {
                    // addEntry(type, SkillCategory.STRENGTH) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.STRONG_ARM -> {
                    // addEntry(type, SkillCategory.STRENGTH) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.THICK_SKULL -> {
                    addEntry(type, SkillCategory.STRENGTH) { player, category, _ , expiresAt ->
                        ThickSkull(player, category, expiresAt)
                    }
                }

                //
                // Traits Category
                //
                SkillType.ANIMAL_SAVAGERY -> {
                    addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                        AnimalSavagery(player, category, expiresAt)
                    }
                }
                SkillType.ANIMOSITY -> {
                    // addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.ALWAYS_HUNGRY -> {
                    // addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.BALL_AND_CHAIN -> {
                    // addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.BOMBARDIER -> {
                    // addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.BONE_HEAD -> {
                    addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                        BoneHead(player, category, expiresAt)
                    }
                }
                SkillType.BLOOD_LUST -> {
                    addEntry(type, SkillCategory.TRAITS, 4) { player, category, value , expiresAt ->
                        BloodLust(player, category, value, expiresAt)
                    }
                }
                SkillType.BREATHE_FIRE -> {
                    addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                        BreatheFire(player, category, expiresAt)
                    }
                }
                SkillType.CHAINSAW -> {
                    // addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.DECAY -> {
                    // addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.HIT_AND_RUN -> {
                    // addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.HYPNOTIC_GAZE -> {
                    // addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.KICK_TEAMMATE -> {
                    // addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.LONER -> {
                    addEntry(type, SkillCategory.TRAITS, 4) { player, category, value , expiresAt ->
                        Loner(player, category, value, expiresAt)
                    }
                }
                SkillType.NO_HANDS -> {
                    // addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.PLAGUE_RIDDEN -> {
                    // addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.POGO_STICK -> {
                    // addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.PROJECTILE_VOMIT -> {
                    addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                        ProjectileVomit(player, category, expiresAt)
                    }
                }
                SkillType.REALLY_STUPID -> {
                    addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                        ReallyStupid(player, category, expiresAt)
                    }
                }
                SkillType.REGENERATION -> {
                    addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                        Regeneration(player, category, expiresAt)
                    }
                }
                SkillType.RIGHT_STUFF -> {
                    // addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.SECRET_WEAPON -> {
                    // addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.STAB -> {
                    addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                        Stab(player, category, expiresAt)
                    }
                }
                SkillType.STUNTY -> {
                    addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                        Stunty(player, category, expiresAt)
                    }
                }
                SkillType.SWARMING -> {
                    // addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.SWOOP -> {
                    // addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.TAKE_ROOT -> {
                    // addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.TITCHY -> {
                    addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                        Titchy(player, category, expiresAt)
                    }
                }
                SkillType.TIMMMBER -> {
                    addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                        Timmmber(player, category, expiresAt)
                    }
                }
                SkillType.THROW_TEAMMATE -> {
                    // addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                    // TODO()
                    // }
                }
                SkillType.UNCHANNELLED_FURY -> {
                    addEntry(type, SkillCategory.TRAITS) { player, category, _ , expiresAt ->
                        UnchannelledFury(player, category, expiresAt)
                    }
                }

                //
                // Special Rules Category
                //
                SkillType.SNEAKIEST_OF_THE_LOT -> {
                    addEntry(type, SkillCategory.SPECIAL_RULES) { player, category, _ , expiresAt ->
                        SneakiestOfTheLot(player, category, expiresAt)
                    }
                }
            }
        }
    }
}
