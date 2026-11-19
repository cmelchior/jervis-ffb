package com.jervisffb.engine.rules.common.actions

/** Controls variations when a procedure performs a nested Block or push. */
interface BlockPolicy {
    val ignoreFoulAppearance: Boolean
    val forceFollowUp: Boolean
    val pushWithoutBlockDice: Boolean
    val deferAttackerTouchdown: Boolean
}
