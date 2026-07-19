package com.jervisffb.engine.model.modifiers

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.rules.common.skills.Duration
import kotlinx.serialization.Serializable

@Serializable
sealed interface PlayerStatusEffect {
    val type: PlayerStatusEffectType
    val duration: Duration

    companion object {
        fun chomped(causedBy: Player) = OwnedPlayerStatusEffect(PlayerStatusEffectType.CHOMPED, Duration.SPECIAL, causedBy)
        fun distracted() = SimplePlayerStatusEffect(PlayerStatusEffectType.DISTRACTED, Duration.START_OF_ACTIVATION)
        fun dodgySnack() = SimplePlayerStatusEffect(PlayerStatusEffectType.DODGY_SNACK, Duration.END_OF_DRIVE)
        fun eyeGouge() = SimplePlayerStatusEffect(PlayerStatusEffectType.EYE_GOUGE, Duration.START_OF_ACTIVATION)
        fun unchannelledFury() = SimplePlayerStatusEffect(PlayerStatusEffectType.UNCHANNELLED_FURY, Duration.START_OF_ACTIVATION)
        fun boneHead() = SimplePlayerStatusEffect(PlayerStatusEffectType.BONE_HEAD, Duration.START_OF_ACTIVATION)
        fun reallyStupid() = SimplePlayerStatusEffect(PlayerStatusEffectType.REALLY_STUPID, Duration.START_OF_ACTIVATION)
        fun bloodLust() = SimplePlayerStatusEffect(PlayerStatusEffectType.BLOOD_LUST, Duration.END_OF_ACTIVATION)
        // Will be removed at end-of-drive, unless manually removed before (by being knocked down or placed prone)
        fun rooted() = SimplePlayerStatusEffect(PlayerStatusEffectType.ROOTED, Duration.END_OF_DRIVE)
    }
}

data class SimplePlayerStatusEffect(
    override val type: PlayerStatusEffectType,
    override val duration: Duration
) : PlayerStatusEffect

data class OwnedPlayerStatusEffect(
    override val type: PlayerStatusEffectType,
    override val duration: Duration,
    val causedBy: Player,
) : PlayerStatusEffect
