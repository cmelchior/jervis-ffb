package com.jervisffb.ui.game.state.decorators

import com.jervisffb.engine.actions.EndTurn
import com.jervisffb.engine.actions.EndTurnWhenReady
import com.jervisffb.engine.model.Game
import com.jervisffb.ui.game.UiGameSnapshot
import com.jervisffb.ui.game.state.ManualActionProvider

class EndTurnDecorator() : FieldActionDecorator<EndTurnWhenReady> {
    override fun decorate(actionProvider: ManualActionProvider, state: Game, snapshot: UiGameSnapshot, descriptor: EndTurnWhenReady) {
        snapshot.gameStatus.centerBadgeText = "End Turn"
        snapshot.gameStatus.centerBadgeAction = { actionProvider.userActionSelected(EndTurn) }
    }
}
