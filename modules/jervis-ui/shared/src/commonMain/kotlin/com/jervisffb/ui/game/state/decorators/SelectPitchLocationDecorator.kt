package com.jervisffb.ui.game.state.decorators

import com.jervisffb.engine.actions.PitchSquareSelected
import com.jervisffb.engine.actions.SelectPitchLocation
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.model.GuardedAction
import com.jervisffb.ui.game.model.UiAction
import com.jervisffb.ui.game.state.UiActionProvider

object SelectPitchLocationDecorator: PitchActionDecorator<SelectPitchLocation> {

    override fun decorate(
        actionProvider: UiActionProvider,
        state: Game,
        descriptor: SelectPitchLocation,
        owner: Team?,
        isEnabled: Boolean,
        acc: UiSnapshotAccumulator
    ) {
        descriptor.squares.forEach { squareData ->
            val action = PitchSquareSelected(squareData.coordinate)
            val selectedAction = UiAction(action, GuardedAction(acc) { id -> actionProvider.userActionSelected(id, action) })
            acc.updateSquare(squareData.coordinate) {
                it.copy(
                    isSelectable = true,
                    selectedAction = selectedAction.takeIf { isEnabled },
                    requiresRoll = (squareData.requiresRush || squareData.requiresDodge || squareData.requiresJump)
                )
            }
        }
    }
}
