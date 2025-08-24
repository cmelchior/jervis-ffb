package com.jervisffb.ui.game.state.indicators

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.model.Game
import com.jervisffb.ui.game.UiGameSnapshot
import com.jervisffb.ui.game.state.decorators.FieldActionDecorator

/**
 * Interface for enhancing the [UiGameSnapshot] based on the state of the field
 * that isn't directly tied to an [com.jervisffb.engine.actions.GameActionDescriptor]
 *
 * E.g. these can be used to display block indicators for a specific player.
 *
 * Changes to the UI that should be be made to indicate an available action should
 * be handled by [FieldActionDecorator].
 */
interface FieldStatusIndicator {
    fun decorate(
        uiSnapshot: UiGameSnapshot,
        node: ActionNode,
        state: Game,
        request: ActionRequest
    )
}
