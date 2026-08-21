package com.jervisffb.ui.game.state.decorators

import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndActionWhenReady
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.icons.ActionIcon
import com.jervisffb.ui.game.model.GuardedAction
import com.jervisffb.ui.game.model.UiAction
import com.jervisffb.ui.game.state.UiActionProvider
import com.jervisffb.ui.game.view.SimpleContextMenuOption

object EndActionDecorator: PitchActionDecorator<EndActionWhenReady> {
    override fun decorate(
        actionProvider: UiActionProvider,
        state: Game,
        descriptor: EndActionWhenReady,
        owner: Team?,
        isEnabled: Boolean,
        acc: UiSnapshotAccumulator
    ) {
        // We don't want to show Context Menu options to the inactive coach.
        if (!isEnabled) {
            return
        }

        state.activePlayer?.location?.let { location ->
            acc.updateSquare(location as PitchCoordinate) {
                // Add action at the front so the button is placed at the bottom.
                it.copy(contextMenuOptions = it.contextMenuOptions.add(0,
                    SimpleContextMenuOption(
                        "End action",
                        UiAction(EndAction, GuardedAction(acc) { id -> actionProvider.userActionSelected(id, EndAction) }),
                        ActionIcon.END_TURN
                    )
                ))
            }
        } ?: error("No active player")
    }
}
