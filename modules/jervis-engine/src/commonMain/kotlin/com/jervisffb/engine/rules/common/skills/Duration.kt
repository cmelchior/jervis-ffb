package com.jervisffb.engine.rules.common.skills

import com.jervisffb.engine.model.context.BB2025MultipleBlockContext
import com.jervisffb.engine.model.inducements.InducementEffect
import com.jervisffb.engine.model.modifiers.PlayerStatusEffect
import com.jervisffb.engine.model.modifiers.StatModifier

/**
 * Enum for descripting the duration of [Skill], [StatModifier],
 * [PlayerStatusEffect], [InducementEffect], or other effects that can have a
 * temporary lifetime.
 *
 * Developer's Commentary:
 * Tracking the duration of skill usages during Multiple Block is complex, as it
 * has a different flow compared to single blocks. To avoid having that
 * complexity leak into this enum, we leave it up to
 * [BB2025MultipleBlockContext] to track the lifetime of relevant skills during
 * a Multiple Block.
 */
enum class Duration {
    IMMEDIATE, // The effect expires immediately.
    START_OF_ACTIVATION, // The effect expires when the player is activated
    END_OF_ACTIVATION, // The effect expires at the end of the current players activation
    END_OF_ACTION, // The effect expires at the end of the action, this also includes subactions, like the Block part of a Blitz.
    END_OF_TURN, // The effect expires at the end of the current teams turn.
    END_OF_DRIVE, // The effect expires at the end of the current drive
    END_OF_HALF, // The effect expires at the end of the current half
    END_OF_GAME, // The effect lasts for the entire game, but doesn't carry over to the next game
    SPECIAL, // The duration of this effect is too hard to put into a bucket and must be handled manually.
    STANDING_UP, // The effect expires when the player is going from prone to standing up.
    PERMANENT, // The effect is a permanent change to the team.
}
