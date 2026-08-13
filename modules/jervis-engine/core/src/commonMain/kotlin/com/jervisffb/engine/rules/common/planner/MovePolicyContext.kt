package com.jervisffb.engine.rules.common.planner

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.policy.GameRulePhase
import com.jervisffb.engine.rules.policy.GameRulePolicyContext

/**
 * Read-only game information made available to a [MovePolicy].
 */
data class MovePolicyContext(
    override val state: Game,
    val phase: GameRulePhase = GameRulePhase.LIVE,
): GameRulePolicyContext
