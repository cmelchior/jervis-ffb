package com.jervisffb.ui.state.decorators

import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.SelectFieldLocation
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.model.Game
import com.jervisffb.ui.UiGameSnapshot
import com.jervisffb.ui.state.UiActionProvider

/**
 * Interface that allows a specific [GameActionDescriptor] to change the [UiGameSnapshot]
 * in order to enable the UI elements required to generate a valid [GameAction] for the
 * current [ActionNode].
 *
 * E.g., a [SelectFieldLocation] descriptor should define the on-click listener
 * for the given fields.
 */
interface FieldActionDecorator<T: GameActionDescriptor> {
    fun decorate(actionProvider: UiActionProvider, state: Game, snapshot: UiGameSnapshot, descriptor: T)
}
