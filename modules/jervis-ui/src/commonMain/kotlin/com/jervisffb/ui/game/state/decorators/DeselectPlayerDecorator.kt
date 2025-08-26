package com.jervisffb.ui.game.state.decorators

import com.jervisffb.engine.actions.DeselectPlayer
import com.jervisffb.engine.actions.PlayerDeselected
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.ui.game.UiGameSnapshot
import com.jervisffb.ui.game.state.ManualActionProvider

object DeselectPlayerDecorator: FieldActionDecorator<DeselectPlayer> {
    override fun decorate(
        actionProvider: ManualActionProvider,
        state: Game,
        snapshot: UiGameSnapshot,
        descriptor: DeselectPlayer,
        owner: Team?
    ) {
        descriptor.players.forEach { player ->
            val coordinate = player.location as FieldCoordinate
            snapshot.fieldSquares[coordinate]?.apply {
                this.onMenuHidden = { actionProvider.userActionSelected(PlayerDeselected(player)) }
            } ?: error("Could not find square: $coordinate")
        }
    }
}
