package com.jervisffb.ui.game.state.decorators

import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndActionWhenReady
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.icons.ActionIcon
import com.jervisffb.ui.game.state.ManualActionProvider
import com.jervisffb.ui.game.view.ContextMenuOption

object EndActionDecorator: FieldActionDecorator<EndActionWhenReady> {
    override fun decorate(
        actionProvider: ManualActionProvider,
        state: Game,
        descriptor: EndActionWhenReady,
        owner: Team?,
        acc: UiSnapshotAccumulator
    ) {
        state.activePlayer?.location?.let { location ->
            acc.updateSquare(location as FieldCoordinate) {
                it.copy(contextMenuOptions = it.contextMenuOptions.add(
                    ContextMenuOption(
                        "End action",
                        { actionProvider.userActionSelected(EndAction) },
                        ActionIcon.END_TURN
                    )
                ))
            }
        } ?: error("No active player")
    }
}
