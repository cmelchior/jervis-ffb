package com.jervisffb.ui.game.state.decorators

import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.SelectDirection
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.ui.game.UiGameSnapshot
import com.jervisffb.ui.game.state.ManualActionProvider

class SelectDirectionDecorator: FieldActionDecorator<SelectDirection> {
    override fun decorate(actionProvider: ManualActionProvider, state: Game, snapshot: UiGameSnapshot, descriptor: SelectDirection) {
        val origin = state.field[descriptor.origin as FieldCoordinate]
        descriptor.directions.forEach { direction ->
            val square = snapshot.fieldSquares[origin.move(direction, 1)]
            snapshot.fieldSquares[origin.move(direction, 1)]?.apply {
                onSelected = { actionProvider.userActionSelected(snapshot.nextActionId, DirectionSelected(direction)) }
                selectableDirection = direction
            } ?: error("Cannot find square: ${origin.move(direction, 1)}")
        }
    }
}
