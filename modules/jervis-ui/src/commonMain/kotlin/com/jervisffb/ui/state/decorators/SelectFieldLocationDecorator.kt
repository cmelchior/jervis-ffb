package com.jervisffb.ui.state.decorators

import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.SelectFieldLocation
import com.jervisffb.engine.model.Game
import com.jervisffb.ui.UiGameSnapshot
import com.jervisffb.ui.state.ManualActionProvider

class SelectFieldLocationDecorator: FieldActionDecorator<SelectFieldLocation> {
    override fun decorate(actionProvider: ManualActionProvider, state: Game, snapshot: UiGameSnapshot, descriptor: SelectFieldLocation) {
        descriptor.squares.forEach { squareData ->
            val selectedAction = {
                actionProvider.userActionSelected(FieldSquareSelected(squareData.coordinate))
            }
            val square = snapshot.fieldSquares[squareData.coordinate]
            snapshot.fieldSquares[squareData.coordinate] = square?.copy(
                onSelected = selectedAction,
                requiresRoll = (squareData.requiresRush || squareData.requiresDodge || squareData.requiresJump)
            ) ?: error("Unexpected location : ${squareData.coordinate}")

        }
    }
}
