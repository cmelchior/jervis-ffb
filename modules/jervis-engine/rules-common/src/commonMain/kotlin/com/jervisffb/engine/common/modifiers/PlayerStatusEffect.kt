package com.jervisffb.engine.common.modifiers

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.modifiers.PlayerStatusEffect
import com.jervisffb.engine.model.modifiers.SimplePlayerStatusEffect
import com.jervisffb.engine.rules.common.skills.Duration

fun PlayerStatusEffect.Companion.banned() = SimplePlayerStatusEffect(PlayerStatusEffectTypeCommon.BANNED, Duration.END_OF_GAME)
fun PlayerStatusEffect.Companion.fainted(duration: Duration) = SimplePlayerStatusEffect(PlayerStatusEffectTypeCommon.FAINTED, duration)
// Wll be removed at end-of-drive, unless manually removed before (by being knocked down or placed prone)
fun PlayerStatusEffect.Companion.rooted() = SimplePlayerStatusEffect(PlayerStatusEffectTypeCommon.ROOTED, Duration.END_OF_DRIVE)

fun Player.isRooted() = statusEffects.any { it.type == PlayerStatusEffectTypeCommon.ROOTED }
fun Player.isBanned() = statusEffects.any { it.type == PlayerStatusEffectTypeCommon.BANNED }
