package com.jervisffb.engine.model.modifiers

import com.jervisffb.engine.commands.AddPlayerStatusEffect
import com.jervisffb.engine.rules.common.skills.Duration

/**
 * Some effects are hard to put into other buckets, like a player that failed a
 * Blood Lust roll or a player that was added to the pitch through Spot The
 * Sneak. In these cases, we might want to mark the player somehow. This is done
 * through this enum and is added to players through [AddPlayerStatusEffect].
 *
 * It is up to the individual procedures to check for effects and react to them.
 * Effects are removed again as part of checking for any other modifiers that
 * has a [Duration].
 */
interface PlayerStatusEffectType {
    val label: String
}
