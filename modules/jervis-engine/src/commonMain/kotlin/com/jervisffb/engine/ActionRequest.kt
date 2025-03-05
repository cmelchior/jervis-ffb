package com.jervisffb.engine

import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndActionWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.SelectMoveType
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.model.Team

/**
 * This class represents a request from the [GameEngineController] to generate
 * a [GameAction] for the current [ActionNode]..
 *
 * @see [GameEngineController.getAvailableActions]
 */
data class ActionRequest(
    val team: Team?,
    val actions: List<GameActionDescriptor>
): List<GameActionDescriptor> by actions {
    val actionsCount = actions.sumOf { it.size } // TODO Should also count all sub actions

    /**
     * Returns `true` if the given action is one part of this request.
     *
     * Note, this method can be expensive to call.
     */
    fun isValid(action: GameAction): Boolean {
        return actions.any {
            it.createAll().contains(action)
        }
    }

    fun contains(action: EndAction): Boolean {
        return actions.contains(EndActionWhenReady)
    }

    fun contains(type: MoveType): Boolean {
        val found = actions.firstOrNull {
            it is SelectMoveType }
            ?.let { (it as SelectMoveType).types.contains(type) }
        return found == true
    }
}
