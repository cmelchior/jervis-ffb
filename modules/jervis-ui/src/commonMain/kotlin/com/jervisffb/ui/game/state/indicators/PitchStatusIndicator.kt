package com.jervisffb.ui.game.state.indicators

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.model.Game
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.state.decorators.PitchActionDecorator
/**
 * Interface for enhancing the [UiGameSnapshot] based on the state of the pitch
 * that isn't directly tied to an [GameActionDescriptor], but rather enhances
 * the information available to the coach.
 *
 * E.g. these can be used to display block indicators for a specific player.
 *
 * Changes to the UI that are made to indicate an available action should
 * be handled by [PitchActionDecorator].
 */
interface PitchStatusIndicator {
    fun decorate(
        node: ActionNode,
        state: Game,
        request: ActionRequest,
        acc: UiSnapshotAccumulator
    )
}
