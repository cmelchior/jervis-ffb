package com.jervisffb.ui.game.state.decorators

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.SelectPitchLocation
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.ui.game.UiGameSnapshot
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.animations.JervisAnimation
import com.jervisffb.ui.game.state.UiActionProvider

/**
 * Interface responsible for setting up event handlers in the UI so it can generate
 * the available actions.
 *
 * This is done by mapping a specific [GameActionDescriptor] to a change in the
 * [UiGameSnapshot].
 *
 * E.g., a [SelectPitchLocation] descriptor should define the on-click listener
 * for the given squares.
 */
interface PitchActionDecorator<T: GameActionDescriptor> {

    /**
     * A more fine-grained check that just matching on the type of action descriptor.
     * Allows this decorator to only be applied for a subset of uses of the same action descriptors.
     */
    fun isApplicable(state: Game, request: ActionRequest) = true

    /**
     * Add visual queues for the given action.
     */
    fun decorate(
        actionProvider: UiActionProvider,
        state: Game,
        // The type of action being generated. This determines what the UI should actually generate.
        descriptor: T,
        // Which team is responsible for generating the action.
        owner: Team?,
        // If `false`, the decoration is just a visual cue, and should not be interactive
        isEnabled: Boolean,
        acc: UiSnapshotAccumulator
    )

    /**
     * Add visual queues after an action has been selected.
     *
     * This is mostly an API for P2P games where we want to make visual queues
     * for actions that are selected on the other client.
     */
    fun selectedActionAnimation(
        action: GameAction,
        state: Game,
        acc: UiSnapshotAccumulator
    ): JervisAnimation? {
        return null // Do nothing
    }
}
