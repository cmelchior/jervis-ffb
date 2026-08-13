package com.jervisffb.engine.rules.policy

import com.jervisffb.engine.GameRulesContext
import com.jervisffb.engine.rules.Rules

/**
 * A stateless policy layered on top of a normal [Rules] instance.
 * This is added through [GameRulesContext]
 *
 * Policies default to only affecting actual play. Mostly to support challenges.
 * Challenge setup actions are authored data and are allowed to establish
 * positions that the resulting challenge rules would otherwise forbid.
 */
interface GameRulePolicy {
    fun appliesDuring(phase: GameRulePhase): Boolean = (phase == GameRulePhase.LIVE)
}
