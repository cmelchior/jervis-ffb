package com.jervisffb.engine.model

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * Some skills have a value associated with them. For example "Loner (4+)" or
 * "Hatred (Troll)". this interface encapsulates that concept.
 * Skills with no value should use [SkillValue.None] or an optional ?
 *
 * See [com.jervisffb.engine.rules.common.skills.Skill] for usage.
 */
@Serializable
sealed interface SkillValue {

    // Skills that look like `SkillName (+Value)`
    @Serializable
    @JvmInline
    value class IntAdjustment(val value: Int) : SkillValue

    // Skills that look like `SkillName (Value+)`
    @Serializable
    @JvmInline
    value class IntTarget(val value: Int) : SkillValue

    @Serializable
    @JvmInline
    value class Keyword(val value: PlayerKeyword) : SkillValue

    @Serializable
    object None : SkillValue
}
