package com.jervisffb.engine

import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.model.Team

/**
 * This class represents a request from the [GameController] to generate
 * a [GameAction] for the current [ActionNode]..
 *
 * @see [GameController.getAvailableActions]
 */
data class ActionRequest(
    val team: Team?,
    val actions: List<GameActionDescriptor>
) {
    val size = actions.size
}


