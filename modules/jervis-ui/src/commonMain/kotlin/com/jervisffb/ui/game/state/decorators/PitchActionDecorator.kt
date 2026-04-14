package com.jervisffb.ui.game.state.decorators

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.SelectPitchLocation
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.state.ManualActionProvider

/**
 * Interface that allows a specific [GameActionDescriptor] to change the [UiGameSnapshot]
 * in order to enable the UI elements required to generate a valid [GameAction] for the
 * current [ActionNode].
 *
 * E.g., a [SelectPitchLocation] descriptor should define the on-click listener
 * for the given squares.
 */
interface PitchActionDecorator<T: GameActionDescriptor> {
    fun isApplicable(state: Game, request: ActionRequest) = true
    fun decorate(
        actionProvider: ManualActionProvider,
        state: Game,
        descriptor: T,
        owner: Team?,
        acc: UiSnapshotAccumulator
    )
}
