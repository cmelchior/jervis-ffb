package com.jervisffb.ui.game.state.decorators

import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.SelectFieldLocation
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.state.ManualActionProvider

object SelectFieldLocationDecorator: FieldActionDecorator<SelectFieldLocation> {
    override fun decorate(
        actionProvider: ManualActionProvider,
        state: Game,
        descriptor: SelectFieldLocation,
        owner: Team?,
        acc: UiSnapshotAccumulator
    ) {
        descriptor.squares.forEach { squareData ->
            val selectedAction = {
                actionProvider.userActionSelected(FieldSquareSelected(squareData.coordinate))
            }
            acc.updateSquare(squareData.coordinate) {
                it.copy(
                    selectedAction = selectedAction,
                    requiresRoll = (squareData.requiresRush || squareData.requiresDodge || squareData.requiresJump)
                )
            }
        }
    }
}
