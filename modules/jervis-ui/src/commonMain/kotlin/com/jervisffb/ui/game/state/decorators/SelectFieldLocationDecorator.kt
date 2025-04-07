package com.jervisffb.ui.game.state.decorators

import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.SelectFieldLocation
import com.jervisffb.engine.model.Game
import com.jervisffb.ui.game.UiGameSnapshot
import com.jervisffb.ui.game.state.ManualActionProvider

class SelectFieldLocationDecorator: FieldActionDecorator<SelectFieldLocation> {
    override fun decorate(actionProvider: ManualActionProvider, state: Game, snapshot: UiGameSnapshot, descriptor: SelectFieldLocation) {
        descriptor.squares.forEach { squareData ->
            val selectedAction = {
                actionProvider.userActionSelected(FieldSquareSelected(squareData.coordinate))
            }
            snapshot.fieldSquares[squareData.coordinate]?.apply {
                onSelected = selectedAction
                requiresRoll = (squareData.requiresRush || squareData.requiresDodge || squareData.requiresJump)
            } ?: error("Unexpected location : ${squareData.coordinate}")
        }
    }
}
