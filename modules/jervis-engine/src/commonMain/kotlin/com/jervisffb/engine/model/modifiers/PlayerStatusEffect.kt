package com.jervisffb.engine.model.modifiers

import com.jervisffb.engine.rules.common.skills.Duration
import kotlinx.serialization.Serializable

/**
 * Some effects are hard to put into other buckets, like a player that failed a Blood Lust roll
 * or a player that was added to the pitch through Spot The Sneak. In these cases, we might want
 * to mark the player somehow. This is done through this enum and is added to players through
 * [com.jervisffb.engine.commands.AddPlayerStatusEffect].
 *
 * It is up to the individual procedures to check for effects and react to them. Effects are
 * removed again as part of checking for any other modifiers that has a [Duration].
 */
@Serializable
enum class PlayerStatusEffectType(val description: String) {
    // BB 2025
    ROOTED("Rooted"),
    DISTRACTED("Distracted"),
    CHOMPED("Chomped"),
    EYE_GOUGE("Eye Gouge"),
    // This is just a marker. Stat decreases are added separately
    DODGY_SNACK("Dodgy Snack"),

    // BB2020
    BONE_HEAD("Bone Head"), // Player failed a Bone Head roll
    REALLY_STUPID("Really Stupid"), // Player failed a Really Stupid Roll
    BLOOD_LUST("Blood Lust"), // Player failed a Blood Lust roll
    HYPNOTIC_GAZE("Hypnotic Gaze"), // Player is affected by Hypnotic Gaze
    UNCHANNELLED_FURY("Unchannelled Fury")
}

@Serializable
data class PlayerStatusEffect(
    val type: PlayerStatusEffectType,
    val duration: Duration,
) {
    companion object {
        fun distracted() = PlayerStatusEffect(PlayerStatusEffectType.DISTRACTED, Duration.START_OF_ACTIVATION)
        fun dodgySnack() = PlayerStatusEffect(PlayerStatusEffectType.DODGY_SNACK, Duration.END_OF_DRIVE)
        fun eyeGouge() = PlayerStatusEffect(PlayerStatusEffectType.EYE_GOUGE, Duration.START_OF_ACTIVATION)
        fun unchannelledFury() = PlayerStatusEffect(PlayerStatusEffectType.UNCHANNELLED_FURY, Duration.START_OF_ACTIVATION)
        fun boneHead() = PlayerStatusEffect(PlayerStatusEffectType.BONE_HEAD, Duration.START_OF_ACTIVATION)
        fun reallyStupid() = PlayerStatusEffect(PlayerStatusEffectType.REALLY_STUPID, Duration.START_OF_ACTIVATION)
        fun bloodLust() = PlayerStatusEffect(PlayerStatusEffectType.BLOOD_LUST, Duration.END_OF_ACTIVATION)
    }
}
