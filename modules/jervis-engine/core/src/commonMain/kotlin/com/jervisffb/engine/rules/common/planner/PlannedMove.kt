package com.jervisffb.engine.rules.common.planner

import com.jervisffb.engine.GameRulesContext
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.TargetSquare

/**
 * A legal, executable movement option. This move should be valid, according
 * to all rules and associated policies.
 *
 * See [GameRulesContext]
 */
data class PlannedMove(
    val target: TargetSquare,
    val action: GameAction,
) {
    val requiresRoll: Boolean = target.requiresDodge || target.requiresRush || target.requiresJump
}
