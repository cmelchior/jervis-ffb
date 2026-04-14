package com.jervisffb.ui.game.state.decorators

import com.jervisffb.engine.actions.EndTurn
import com.jervisffb.engine.actions.EndTurnWhenReady
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.bb2025.procedures.tables.kickoff.Charge
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.state.ManualActionProvider

object EndTurnDecorator : PitchActionDecorator<EndTurnWhenReady> {
    override fun decorate(
        actionProvider: ManualActionProvider,
        state: Game,
        descriptor: EndTurnWhenReady,
        owner: Team?,
        acc: UiSnapshotAccumulator
    ) {
        val title = when {
            state.stack.containsProcedure(Charge) -> "End Charge!"
            else -> "End Turn"
        }
        acc.updateGameStatus {
            it.copy(
                centerBadgeText = title,
                centerBadgeAction = { actionProvider.userActionSelected(EndTurn) },
                centerBadgeEnabled = true
            )
        }
    }
}
