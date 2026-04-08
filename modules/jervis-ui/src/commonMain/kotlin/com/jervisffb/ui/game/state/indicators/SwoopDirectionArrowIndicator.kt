package com.jervisffb.ui.game.state.indicators

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.getContextOrNull
import com.jervisffb.engine.rules.bb2025.procedures.actions.throwteammate.SwoopContext
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.state.decorators.SelectDirectionDecorator

/**
 * Show intermediate arrows during a push sequence, i.e. directions
 * already selected.
 *
 * The current push is handled by [SelectDirectionDecorator].
 */
object SwoopDirectionArrowIndicator: FieldStatusIndicator {
    override fun decorate(
        node: ActionNode,
        state: Game,
        request: ActionRequest,
        acc: UiSnapshotAccumulator
    ) {
        state.getContextOrNull<SwoopContext>()?.let { context ->
            context.rolledDirection?.let { direction ->
                acc.updateSquare(context.player.coordinates.move(direction, steps = 1)) {
                    it.copy(directionSelected = direction)
                }
            }
        }
    }
}
