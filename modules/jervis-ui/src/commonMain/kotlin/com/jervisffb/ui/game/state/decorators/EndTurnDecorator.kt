package com.jervisffb.ui.game.state.decorators

import com.jervisffb.engine.actions.EndTurn
import com.jervisffb.engine.actions.EndTurnWhenReady
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.state.ManualActionProvider

object EndTurnDecorator : FieldActionDecorator<EndTurnWhenReady> {
    override fun decorate(
        actionProvider: ManualActionProvider,
        state: Game,
        descriptor: EndTurnWhenReady,
        owner: Team?,
        acc: UiSnapshotAccumulator
    ) {
        acc.updateGameStatus {
            it.copy(
                centerBadgeText = "End Turn",
                centerBadgeAction = { actionProvider.userActionSelected(EndTurn) }
            )
        }
    }
}
