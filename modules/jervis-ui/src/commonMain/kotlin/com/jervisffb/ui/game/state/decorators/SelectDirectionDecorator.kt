package com.jervisffb.ui.game.state.decorators

import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.SelectDirection
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.ui.game.UiGameSnapshot
import com.jervisffb.ui.game.state.ManualActionProvider

object SelectDirectionDecorator: FieldActionDecorator<SelectDirection> {
    override fun decorate(
        actionProvider: ManualActionProvider,
        state: Game,
        snapshot: UiGameSnapshot,
        descriptor: SelectDirection,
        owner: Team?
    ) {
        val origin = state.field[descriptor.origin as FieldCoordinate]

        // If pushing into the crowd is the only option, figure out how to handle this.
        // Should it just be done inside the rules engine through a "Continue" event? Are
        // there any Special Play Cards or rules that could affect this?
        if (
            descriptor.directions.size == 1
            && origin.move(descriptor.directions.single(), 1) == FieldCoordinate.OUT_OF_BOUNDS
        ) {
            snapshot.unknownActions.add(descriptor.createAll().single())
        } else {
            descriptor.directions.forEach { direction ->
                val square = snapshot.fieldSquares[origin.move(direction, 1)]
                snapshot.fieldSquares[origin.move(direction, 1)]?.apply {
                    onSelected = { actionProvider.userActionSelected(DirectionSelected(direction)) }
                    selectableDirection = direction
                } ?: error("Cannot find square: ${origin.move(direction, 1)}")
            }
        }
    }
}
