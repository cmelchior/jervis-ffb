package com.jervisffb.engine.common.modifiers

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.modifiers.PlayerStatusEffect
import com.jervisffb.engine.model.modifiers.SimplePlayerStatusEffect
import com.jervisffb.engine.rules.common.skills.Duration

// Wll be removed at end-of-drive, unless manually removed before (by being knocked down or placed prone)
fun PlayerStatusEffect.Companion.rooted() = SimplePlayerStatusEffect(PlayerStatusEffectTypeCommon.ROOTED, Duration.END_OF_DRIVE)

fun Player.isRooted() = statusEffects.any { it.type == PlayerStatusEffectTypeCommon.ROOTED }
