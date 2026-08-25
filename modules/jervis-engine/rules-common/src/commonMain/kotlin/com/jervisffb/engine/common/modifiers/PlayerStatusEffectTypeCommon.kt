package com.jervisffb.engine.common.modifiers

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.modifiers.PlayerStatusEffectType
import kotlinx.serialization.Serializable

@Serializable
enum class PlayerStatusEffectTypeCommon(
    override val label: String
): PlayerStatusEffectType {
    ROOTED("Rooted"),
    BLOOD_LUST("Blood Lust"), // Player failed a Blood Lust roll
}

fun Player.hasBloodLust() = statusEffects.any { it.type == PlayerStatusEffectTypeCommon.BLOOD_LUST }
