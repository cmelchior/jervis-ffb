package com.jervisffb.engine.model.modifiers

import com.jervisffb.engine.rules.common.skills.Duration
import kotlinx.serialization.Serializable

/**
 * Some effects are hard to put into other buckets, like a player that failed a Blood Lust roll
 * or a player that was added to the pitch through Spot The Sneak. In these cases, we might want
 * to mark the player somehow. This is done through this enum and is added to players through
 * [com.jervisffb.model.Player.temporaryEffects].
 *
 * It is up to the individual procedures to check for effects and react to them. Effects are
 * removed again as part of checking for any other modifiers that has a [Duration].
 */
@Serializable
enum class TemporaryEffectType {
    BONE_HEAD, // Player failed a Bone Head roll
    REALLY_STUPID, // Player failed a Really Stupid Roll
    BLOOD_LUST, // Player failed a Blood Lust roll
    HYPNOTIC_GAZE, // Player is affected by Hypnotic Gaze
    UNCHANNELLED_FURY
}

@Serializable
data class TemporaryEffect(
    val type: TemporaryEffectType,
    val duration: Duration,
) {
    companion object {
        fun unchannelledFury() = TemporaryEffect(TemporaryEffectType.UNCHANNELLED_FURY, Duration.START_OF_ACTIVATION)
        fun boneHead() = TemporaryEffect(TemporaryEffectType.BLOOD_LUST, Duration.START_OF_ACTIVATION)
        fun bloodLust() = TemporaryEffect(TemporaryEffectType.BLOOD_LUST, Duration.END_OF_ACTIVATION)
    }
}
