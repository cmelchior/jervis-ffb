package com.jervisffb.ui.state.decorators

import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.SelectFieldLocation
import com.jervisffb.engine.model.Game
import com.jervisffb.ui.UiGameSnapshot
import com.jervisffb.ui.state.ManualActionProvider

class SelectFieldLocationDecorator: FieldActionDecorator<SelectFieldLocation> {
    override fun decorate(actionProvider: ManualActionProvider, state: Game, snapshot: UiGameSnapshot, descriptor: SelectFieldLocation) {
        val selectedAction = {
            actionProvider.userActionSelected(FieldSquareSelected(descriptor.coordinate))
        }
        val square = snapshot.fieldSquares[descriptor.coordinate]
        snapshot.fieldSquares[descriptor.coordinate] = square?.copy(
            onSelected = selectedAction,
            requiresRoll = (descriptor.requiresRush || descriptor.requiresDodge)
        ) ?: error("Unexpected location : ${descriptor.coordinate}")
    }
}
