package com.jervisffb.engine.bb2025.modifiers

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.modifiers.OwnedPlayerStatusEffect
import com.jervisffb.engine.model.modifiers.PlayerStatusEffect
import com.jervisffb.engine.model.modifiers.SimplePlayerStatusEffect
import com.jervisffb.engine.rules.common.skills.Duration

fun PlayerStatusEffect.Companion.chomped(causedBy: Player) = OwnedPlayerStatusEffect(PlayerStatusEffectType2025.CHOMPED, Duration.SPECIAL, causedBy)
fun PlayerStatusEffect.Companion.distracted() = SimplePlayerStatusEffect(PlayerStatusEffectType2025.DISTRACTED, Duration.START_OF_ACTIVATION)
fun PlayerStatusEffect.Companion.dodgySnack() = SimplePlayerStatusEffect(PlayerStatusEffectType2025.DODGY_SNACK, Duration.END_OF_DRIVE)
fun PlayerStatusEffect.Companion.eyeGouge() = SimplePlayerStatusEffect(PlayerStatusEffectType2025.EYE_GOUGE, Duration.START_OF_ACTIVATION)
fun PlayerStatusEffect.Companion.hangover(duration: Duration) = SimplePlayerStatusEffect(PlayerStatusEffectType2025.HANGOVER, duration)

fun Player.isChomped(): Boolean = statusEffects.any { it.type == PlayerStatusEffectType2025.CHOMPED }
fun Player.isEyeGouged(): Boolean = statusEffects.any { it.type == PlayerStatusEffectType2025.EYE_GOUGE }
