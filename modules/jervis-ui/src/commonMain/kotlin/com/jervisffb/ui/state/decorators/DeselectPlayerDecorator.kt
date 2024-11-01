package com.jervisffb.ui.state.decorators

import com.jervisffb.engine.actions.DeselectPlayer
import com.jervisffb.engine.actions.PlayerDeselected
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.ui.UiGameSnapshot
import com.jervisffb.ui.state.UiActionProvider

class DeselectPlayerDecorator: FieldActionDecorator<DeselectPlayer> {
    override fun decorate(actionProvider: UiActionProvider, state: Game, snapshot: UiGameSnapshot, descriptor: DeselectPlayer) {
        descriptor.players.forEach { player ->
            val coordinate = player.location as FieldCoordinate
            snapshot.fieldSquares[coordinate] = snapshot.fieldSquares[coordinate]?.copy(
                onMenuHidden = { actionProvider.userActionSelected(PlayerDeselected(player)) }
            ) ?: error ("Could not find square: $coordinate")
        }
    }
}
