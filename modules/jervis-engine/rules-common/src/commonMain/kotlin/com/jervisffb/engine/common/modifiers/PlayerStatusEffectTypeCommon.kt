package com.jervisffb.engine.common.modifiers

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.modifiers.PlayerStatusEffectType
import kotlinx.serialization.Serializable

@Serializable
enum class PlayerStatusEffectTypeCommon(
    override val label: String
): PlayerStatusEffectType {
    BANNED("Banned"),
    FAINTED("Fainted"),
    ROOTED("Rooted"),
    BLOOD_LUST("Blood Lust"), // Player failed a Blood Lust roll
    HYPNOTIC_GAZE("Hypnotic Gaze"), // Player is affected by Hypnotic Gaze
}

fun Player.hasBloodLust() = statusEffects.any { it.type == PlayerStatusEffectTypeCommon.BLOOD_LUST }
fun Player.isFainted() = statusEffects.any { it.type == PlayerStatusEffectTypeCommon.FAINTED }
