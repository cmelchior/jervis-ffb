package com.jervisffb.ui.game.state.decorators

import com.jervisffb.engine.actions.EndTurn
import com.jervisffb.engine.actions.EndTurnWhenReady
import com.jervisffb.engine.bb2025.procedures.table.kickoff.Charge
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.model.GuardedBadgeAction
import com.jervisffb.ui.game.state.UiActionProvider

object EndTurnDecorator : PitchActionDecorator<EndTurnWhenReady> {
    override fun decorate(
        actionProvider: UiActionProvider,
        state: Game,
        descriptor: EndTurnWhenReady,
        owner: Team?,
        isEnabled: Boolean,
        acc: UiSnapshotAccumulator
    ) {
        // We don't want to show potential "Badge Actions" to the inactive coach.
        if (!isEnabled) {
            return
        }

        val title = when {
            state.stack.containsProcedure(Charge) -> "End Charge!"
            else -> "End Turn"
        }
        acc.updateGameStatus {
            it.copy(
                centerBadgeText = title,
                centerBadgeAction = GuardedBadgeAction(acc) { id, _ -> actionProvider.userActionSelected(id, EndTurn) },
                centerBadgeEnabled = true
            )
        }
    }
}
