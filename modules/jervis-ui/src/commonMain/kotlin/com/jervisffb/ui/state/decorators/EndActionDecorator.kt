package com.jervisffb.ui.state.decorators

import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndActionWhenReady
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.ui.UiGameSnapshot
import com.jervisffb.ui.state.ManualActionProvider
import com.jervisffb.ui.view.ContextMenuOption

class EndActionDecorator: FieldActionDecorator<EndActionWhenReady> {
    override fun decorate(actionProvider: ManualActionProvider, state: Game, snapshot: UiGameSnapshot, descriptor: EndActionWhenReady) {
        state.activePlayer?.location?.let { location ->
            val squareData = snapshot.fieldSquares[location as FieldCoordinate] ?: error("Could not find square: $location")
            snapshot.fieldSquares[location] = squareData.copyAddContextMenu(
                ContextMenuOption(
                    "End action",
                    { actionProvider.userActionSelected(EndAction) },
                )
            )
        } ?: error("No active player")
    }
}
