package com.jervisffb.engine.rules.common.skills

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerKeyword
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.model.SkillValue

// We need a factory for each type of value that skills can use. This seems to be the only to keep
// the type-system somewhat happy.
sealed interface SkillFactory<T> {
    // Display named used to identify the factory. In most cases, this is just the same as
    // the type of skill it creates, but in cases where a value is required, it will be different
    // e.g the factory is called "Loner(X+)", but the skill is called "Loner(4+)".
    val name: String
    val type: SkillType
    val defaultSkillId: SkillId
    fun createSkill(player: Player, value: SkillValue?, expiresAt: Duration): Skill<T>
}

class NoValueSkillFactory(
    override val name: String,
    override val type: SkillType,
    private val category: SkillCategory,
    private val createFunc: ((Player, SkillCategory, Duration) -> Skill<Unit>),
): SkillFactory<Unit> {
    override val defaultSkillId: SkillId = SkillId(type, SkillValue.None)
    override fun createSkill(player: Player, value: SkillValue?, expiresAt: Duration): Skill<Unit> {
        return createFunc(player, category, expiresAt)
    }
}

class IntTargetSkillFactory(
    override val name: String,
    override val type: SkillType,
    private val category: SkillCategory,
    private val defaultValue: Int?,
    private val createFunc: ((Player, SkillCategory, Int?, Duration) -> Skill<Int>),
): SkillFactory<Int> {
    override val defaultSkillId: SkillId =
        when (defaultValue) {
            null -> SkillId(type, SkillValue.None)
            else -> SkillId(type, SkillValue.IntTarget(defaultValue))
        }
    override fun createSkill(player: Player, value: SkillValue?, expiresAt: Duration): Skill<Int> {
        return if (defaultSkillId.value != SkillValue.None && value == SkillValue.None) {
            createFunc(player, category, defaultValue, expiresAt)
        } else {
            val value = when (value) {
                null -> null
                is SkillValue.IntTarget -> value.value
                else -> error("Unsupported value type: $value for $type")
            }
            createFunc(player, category, value, expiresAt)
        }
    }
}

class IntAdjustmentSkillFactory(
    override val name: String,
    override val type: SkillType,
    private val category: SkillCategory,
    private val defaultValue: Int?,
    private val createFunc: ((Player, SkillCategory, Int?, Duration) -> Skill<Int>),
): SkillFactory<Int> {
    override val defaultSkillId: SkillId =
        when (defaultValue) {
            null -> SkillId(type, SkillValue.None)
            else -> SkillId(type, SkillValue.IntAdjustment(defaultValue))
        }
    override fun createSkill(player: Player, value: SkillValue?, expiresAt: Duration): Skill<Int> {
        return if (defaultSkillId.value != SkillValue.None && value == SkillValue.None) {
            createFunc(player, category, defaultValue, expiresAt)
        } else {
            val value = when (value) {
                null -> null
                is SkillValue.IntAdjustment -> value.value
                else -> error("Unsupported value type: $value for $type")
            }
            createFunc(player, category, value, expiresAt)
        }
    }
}

class KeywordSkillFactory(
    override val name: String,
    override val type: SkillType,
    private val category: SkillCategory,
    private val defaultValue: PlayerKeyword?,
    private val createFunc: ((Player, SkillCategory, PlayerKeyword?, Duration) -> Skill<PlayerKeyword>),
): SkillFactory<PlayerKeyword> {
    override val defaultSkillId: SkillId =
        when (defaultValue) {
            null -> SkillId(type, SkillValue.None)
            else -> SkillId(type, SkillValue.Keyword(defaultValue))
        }
    override fun createSkill(player: Player, value: SkillValue?, expiresAt: Duration): Skill<PlayerKeyword> {
        return if (defaultSkillId.value != SkillValue.None && value == SkillValue.None) {
            createFunc(player, category, defaultValue, expiresAt)
        } else {
            val value = when (value) {
                null,
                is SkillValue.None -> null
                is SkillValue.Keyword -> value.value
                else -> error("Unsupported value type [$type]: $value")
            }
            createFunc(player, category, value, expiresAt)
        }
    }
}
