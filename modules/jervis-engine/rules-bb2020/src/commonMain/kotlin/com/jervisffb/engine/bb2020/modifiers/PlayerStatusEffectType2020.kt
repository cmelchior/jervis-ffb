package com.jervisffb.engine.bb2020.modifiers

import com.jervisffb.engine.model.modifiers.PlayerStatusEffectType
import kotlinx.serialization.Serializable

@Serializable
enum class PlayerStatusEffectType2020(
    override val label: String
): PlayerStatusEffectType {
    // BB2020
    BONE_HEAD("Bone Head"), // Player failed a Bone Head roll
    REALLY_STUPID("Really Stupid"), // Player failed a Really Stupid Roll
    BLOOD_LUST("Blood Lust"), // Player failed a Blood Lust roll
    HYPNOTIC_GAZE("Hypnotic Gaze"), // Player is affected by Hypnotic Gaze
    UNCHANNELLED_FURY("Unchannelled Fury")
}
