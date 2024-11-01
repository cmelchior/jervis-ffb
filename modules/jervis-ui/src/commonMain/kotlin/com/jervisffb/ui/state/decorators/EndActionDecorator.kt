package com.jervisffb.ui.state.decorators

import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndActionWhenReady
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.ui.UiGameSnapshot
import com.jervisffb.ui.state.UiActionProvider
import com.jervisffb.ui.view.ContextMenuOption

class EndActionDecorator: FieldActionDecorator<EndActionWhenReady> {
    override fun decorate(actionProvider: UiActionProvider, state: Game, snapshot: UiGameSnapshot, descriptor: EndActionWhenReady) {
        state.activePlayer?.location?.let { location ->
            snapshot.fieldSquares[location as FieldCoordinate] = snapshot.fieldSquares[location]?.copyAddContextMenu(
                ContextMenuOption(
                    "End action",
                    { actionProvider.userActionSelected(EndAction) },
                )
            ) ?: error("Could not find square: $location")
        } ?: error("No active player")
    }
}
