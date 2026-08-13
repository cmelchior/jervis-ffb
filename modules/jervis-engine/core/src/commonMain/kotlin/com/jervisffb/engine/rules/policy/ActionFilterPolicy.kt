package com.jervisffb.engine.rules.policy

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor

/**
 * Represents a game policy, making it possible to add a filter on an
 * [ActionRequest] and extra validation on an incoming [GameAction]
 *
 * This makes it possible to remove, otherwise legal, actions or restrict
 * which values a die can roll.
 */
interface ActionFilterPolicy : GameRulePolicy {
    /**
     * Filters [GameActionDescriptor]s found in an out-going [ActionRequest].
     * Returns the updated [ActionRequest].
     */
    fun filterRequest(context: ActionFilterContext, request: ActionRequest): ActionRequest
}
