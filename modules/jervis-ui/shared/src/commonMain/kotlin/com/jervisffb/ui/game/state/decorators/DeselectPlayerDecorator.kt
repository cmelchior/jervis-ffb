package com.jervisffb.ui.game.state.decorators

import com.jervisffb.engine.actions.DeselectPlayer
import com.jervisffb.engine.actions.PlayerDeselected
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.model.GuardedAction
import com.jervisffb.ui.game.model.UiAction
import com.jervisffb.ui.game.state.UiActionProvider

object DeselectPlayerDecorator: PitchActionDecorator<DeselectPlayer> {
    override fun decorate(
        actionProvider: UiActionProvider,
        state: Game,
        descriptor: DeselectPlayer,
        owner: Team?,
        isEnabled: Boolean,
        acc: UiSnapshotAccumulator
    ) {
        // Unclear how to handle the in-active coach here, so for now, just disable it.
        if (!isEnabled) {
            return
        }

        descriptor.players.forEach { player ->
            val coordinate = player.location as PitchCoordinate
            val action = PlayerDeselected(player)
            acc.updateSquare(coordinate) {
                it.copy(onMenuHidden = UiAction(action, GuardedAction(acc) { id -> actionProvider.userActionSelected(id, action) }))
            }
        }
    }
}
