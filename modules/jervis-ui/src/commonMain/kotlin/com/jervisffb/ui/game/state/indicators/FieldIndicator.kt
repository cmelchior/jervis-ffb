package com.jervisffb.ui.game.state.indicators

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.model.Game
import com.jervisffb.ui.game.UiGameSnapshot

/**
 * Interface for enhancing the [UiGameSnapshot] based on the state of the field
 * that isn't directly tied to an [com.jervisffb.engine.actions.GameActionDescriptor]
 *
 * These decorators should only be used for ephemeral indicators that cannot be
 * directly calculated from the Engine Model. E.g. these can be used to display
 * block indicators for a specific player
 * Note, this is different than [com.jervisffb.ui.game.state.decorators.FieldActionDecorator]
 * which i
 */
interface FieldIndicator {
    fun decorate(
        uiSnapshot: UiGameSnapshot,
        node: ActionNode,
        state: Game,
        request: ActionRequest
    )
}
