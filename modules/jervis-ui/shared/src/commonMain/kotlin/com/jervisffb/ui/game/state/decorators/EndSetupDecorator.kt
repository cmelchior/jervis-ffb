package com.jervisffb.ui.game.state.decorators

import com.jervisffb.engine.actions.EndSetup
import com.jervisffb.engine.actions.EndSetupWhenReady
import com.jervisffb.engine.common.procedures.tables.kickoff.QuickSnap
import com.jervisffb.engine.common.procedures.tables.kickoff.SolidDefense
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.model.GuardedBadgeAction
import com.jervisffb.ui.game.state.UiActionProvider

object EndSetupDecorator : PitchActionDecorator<EndSetupWhenReady> {
    override fun decorate(
        actionProvider: UiActionProvider,
        state: Game,
        descriptor: EndSetupWhenReady,
        owner: Team?,
        isEnabled: Boolean,
        acc: UiSnapshotAccumulator
    ) {
        // We don't want to show potential "Badge Actions" to the inactive coach.
        if (!isEnabled) {
            return
        }

        val title = when {
            state.stack.containsProcedure(SolidDefense) -> "End Solid Defense"
            state.stack.containsProcedure(QuickSnap) -> "End Quick Snap"
            else -> "End Setup"
        }
        acc.updateGameStatus {
            it.copy(
                centerBadgeText = title,
                centerBadgeAction = GuardedBadgeAction(acc) { id, _ ->
                    actionProvider.userActionSelected(id, EndSetup)
                },
                centerBadgeEnabled = true
            )
        }
    }
}
