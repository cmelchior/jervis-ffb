package com.jervisffb.engine.model.modifiers

import com.jervisffb.engine.model.modifiers.StatModifier.Type
import com.jervisffb.engine.rules.common.skills.Duration

/**
 * Interface describing a change to a player's base line stat.
 *
 * This, among other things, includes injuries, characteristic improvements, prayer
 * of nuffle effects and so on.
 */
interface StatModifier {
    enum class Type {
        AV, MA, PA, AG, ST
    }
    val description: String
    val modifier: Int
    val type: Type
    val expiresAt: Duration
}

// Fake StatModifier constructor for generic stat modifiers
fun StatModifier(
    type: Type,
    modifier: Int,
    description: String,
    expiresAt: Duration = Duration.PERMANENT
): StatModifier =
    object : StatModifier {
        override val type: Type = type
        override val modifier: Int = modifier
        override val description: String = description
        override val expiresAt: Duration = expiresAt
    }

enum class SkillStatModifier(
    override val description: String,
    override val modifier: Int,
    override val type: Type,
    override val expiresAt: Duration = Duration.PERMANENT
): StatModifier {
    HORNS("Horns (+1 ST)", 1, Type.ST, expiresAt = Duration.END_OF_ACTION),
}
