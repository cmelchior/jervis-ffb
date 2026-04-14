package com.jervisffb.ui.game.state.decorators

import com.jervisffb.engine.actions.DeselectPlayer
import com.jervisffb.engine.actions.PlayerDeselected
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.state.ManualActionProvider

object DeselectPlayerDecorator: PitchActionDecorator<DeselectPlayer> {
    override fun decorate(
        actionProvider: ManualActionProvider,
        state: Game,
        descriptor: DeselectPlayer,
        owner: Team?,
        acc: UiSnapshotAccumulator
    ) {
        descriptor.players.forEach { player ->
            val coordinate = player.location as PitchCoordinate
            acc.updateSquare(coordinate) {
                it.copy(onMenuHidden = { actionProvider.userActionSelected(PlayerDeselected(player)) })
            }
        }
    }
}
