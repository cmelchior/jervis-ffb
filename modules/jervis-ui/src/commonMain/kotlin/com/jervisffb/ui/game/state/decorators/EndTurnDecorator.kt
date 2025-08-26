package com.jervisffb.ui.game.state.decorators

import com.jervisffb.engine.actions.EndTurn
import com.jervisffb.engine.actions.EndTurnWhenReady
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.ui.game.UiGameSnapshot
import com.jervisffb.ui.game.state.ManualActionProvider

object EndTurnDecorator : FieldActionDecorator<EndTurnWhenReady> {
    override fun decorate(
        actionProvider: ManualActionProvider,
        state: Game,
        snapshot: UiGameSnapshot,
        descriptor: EndTurnWhenReady,
        owner: Team?
    ) {
        snapshot.gameStatus.centerBadgeText = "End Turn"
        snapshot.gameStatus.centerBadgeAction = { actionProvider.userActionSelected(EndTurn) }
    }
}
