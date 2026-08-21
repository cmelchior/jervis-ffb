package com.jervisffb.ui.game.state.decorators

import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.SelectDirection
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.animations.DirectionSelectedAnimation
import com.jervisffb.ui.game.animations.JervisAnimation
import com.jervisffb.ui.game.model.GuardedAction
import com.jervisffb.ui.game.model.UiAction
import com.jervisffb.ui.game.state.UiActionProvider

object SelectDirectionDecorator: PitchActionDecorator<SelectDirection> {

    override fun decorate(
        actionProvider: UiActionProvider,
        state: Game,
        descriptor: SelectDirection,
        owner: Team?,
        isEnabled: Boolean,
        acc: UiSnapshotAccumulator
    ) {
        val origin = state.pitch[descriptor.origin as PitchCoordinate]

        // If pushing into the crowd is the only option, figure out how to handle this.
        // Should it just be done inside the rules engine through a "Continue" event? Are
        // there any Special Play Cards or rules that could affect this?
        val outOfBounds = descriptor.directions.firstOrNull {
            origin.move(it, 1).isOutOfBounds(state.rules)
        }
        if (outOfBounds != null) {
            acc.addUnknownAction(DirectionSelected(outOfBounds))
        } else {
            descriptor.directions.forEach { direction ->
                val action = DirectionSelected(direction)
                val uiAction = UiAction(action, GuardedAction(acc) { id -> actionProvider.userActionSelected(id, action) })
                acc.updateSquare(origin.move(direction, 1)) {
                    it.copy(
                        isSelectable = true,
                        selectedAction = uiAction.takeIf { isEnabled },
                        selectableDirection = direction
                    )
                }
            }
        }
    }

    override fun selectedActionAnimation(
        action: GameAction,
        state: Game,
        acc: UiSnapshotAccumulator
    ): JervisAnimation? {
        val selectedAction = action as? DirectionSelected ?: return null
        val target = acc.squares.values.firstOrNull { square ->
            square.selectableDirection == selectedAction.direction
                && !square.coordinates.isOutOfBounds(state.rules)
        } ?: return null

        // Keep the normal arrow in the snapshot. The animation layer fades the
        // filled (selected) arrow over it before the engine advances.
        acc.updateSquare(target.coordinates) {
            it.copy(
                selectedAction = null
            )
        }
        return DirectionSelectedAnimation(
            uiController = acc.uiController,
            coordinate = target.coordinates,
            direction = selectedAction.direction,
        )
    }
}
