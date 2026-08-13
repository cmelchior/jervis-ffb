package com.jervisffb.engine.rules.policy

import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.model.Game

/**
 * Read-only engine information made available to an [ActionFilterPolicy].
 */
data class ActionFilterContext(
    override val state: Game,
    val node: ActionNode, // Which node an ActionRequest belongs to.
    val phase: GameRulePhase,
): GameRulePolicyContext
