package com.jervisffb.ui.game.state.actionwheel

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.state.UiActionProvider
import com.jervisffb.ui.menu.LocalPitchDataWrapper
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

    // Where should this ActionWheel be centered?
    // If `null` it will be centered in the middle of the pitch.
    abstract fun getActionWheelCenter(state: Game): PitchCoordinate?

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
        sharedData: LocalPitchDataWrapper,
    ) {
        // Do nothing
    }

    open fun onPostActionAnimation(
        acc: UiSnapshotAccumulator,
        selectedAction: GameAction,
    ): Boolean {
        return false
    }

    protected fun getHomeCenterCoordinates(state: Game): PitchCoordinate {
        val y = (state.rules.pitchHeight / 2)
        val x = (state.rules.pitchWidth / 4)
        return PitchCoordinate(x, y)
    }

    protected fun getAwayCenterCoordinates(state: Game): PitchCoordinate {
        val y = (state.rules.pitchHeight / 2)
        val x = state.rules.pitchWidth - (state.rules.pitchWidth / 4) - 1
        return PitchCoordinate(x, y)
    }
}
