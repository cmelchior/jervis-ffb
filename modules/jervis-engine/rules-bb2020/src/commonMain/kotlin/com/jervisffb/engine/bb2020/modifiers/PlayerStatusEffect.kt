package com.jervisffb.engine.bb2020.modifiers

import com.jervisffb.engine.model.modifiers.PlayerStatusEffect
import com.jervisffb.engine.model.modifiers.SimplePlayerStatusEffect
import com.jervisffb.engine.rules.common.skills.Duration

fun PlayerStatusEffect.Companion.unchannelledFury() = SimplePlayerStatusEffect(PlayerStatusEffectType2020.UNCHANNELLED_FURY, Duration.START_OF_ACTIVATION)
fun PlayerStatusEffect.Companion.boneHead() = SimplePlayerStatusEffect(PlayerStatusEffectType2020.BONE_HEAD, Duration.START_OF_ACTIVATION)
fun PlayerStatusEffect.Companion.reallyStupid() = SimplePlayerStatusEffect(PlayerStatusEffectType2020.REALLY_STUPID, Duration.START_OF_ACTIVATION)
