package com.jervisffb.ui.game.state.actionwheel

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.fsm.Node
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.state.UiActionProvider
import com.jervisffb.ui.menu.LocalFieldDataWrapper
import kotlin.time.Duration.Companion.milliseconds

/**
 * Interface responsible for controlling the lifecycle of an Action Wheel and
 * how it can transition between states for each action.
 *
 * This transition can both be forward in time and backwards (using Undo).
 * Also, it is also possible to start in the middle (like when reloading).
 */
abstract class ActionWheelDialogController {

    companion object {
        val DEFAULT_DELAY_AFTER_ROLL = 450.milliseconds
    }

    // Which nodes are part of this Action Wheel?
    abstract val nodes: Set<Node>

    open fun onApplyCurrentState(
        acc: UiSnapshotAccumulator,
        actionApplied: GameAction?,
        previousNode: Node?,
        currentNode: Node
    ): Boolean {
        return false
    }

    open fun onDecorateActions(
        acc: UiSnapshotAccumulator,
        provider: UiActionProvider,
        actions: ActionRequest,
        sharedData: LocalFieldDataWrapper,
    ) {
        // Do nothing
    }

    open fun onPostActionAnimation(
        acc: UiSnapshotAccumulator,
        selectedAction: GameAction,
    ): Boolean {
        return false
    }
}
