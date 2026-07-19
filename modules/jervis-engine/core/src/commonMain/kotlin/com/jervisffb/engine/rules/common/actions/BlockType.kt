package com.jervisffb.engine.rules.common.actions

import kotlinx.serialization.Serializable

/**
 * During a Blitz, many skills make it possible to exchange the standard block
 * for a different type. This enum contains all options across all rulesets.
 */
@Serializable
enum class BlockType {
    BREATHE_FIRE,
    CHAINSAW,
    CHOMP,
    MULTIPLE_BLOCK,
    PROJECTILE_VOMIT,
    STAB,
    STANDARD,
    // Multiple Block
    // Ball & Chain (replace all other actions)
    // Bombardier (Its own action, 1 pr. team turn)
    // Chainsaw (Replace block action or block part of Blitz, 1. pr activation)
    // Kick Team-mate (its own action, 1 pr. team turn)
    // Projectile Vomit (Replace block action or block part of Blitz, 1. pr activation)
    // Stab (Replace block action or block part of Blitz, no limit)
    // Hypnotic Gaze (Its own action)
    // Breathe Fire (replace block or block part of blitz, once pr. activation)
}
