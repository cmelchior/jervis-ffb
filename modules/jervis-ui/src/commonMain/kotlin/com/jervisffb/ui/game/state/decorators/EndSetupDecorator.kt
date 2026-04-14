package com.jervisffb.ui.game.state.decorators

import com.jervisffb.engine.actions.EndSetup
import com.jervisffb.engine.actions.EndSetupWhenReady
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.common.procedures.tables.kickoff.QuickSnap
import com.jervisffb.engine.rules.common.procedures.tables.kickoff.SolidDefense
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.state.ManualActionProvider

object EndSetupDecorator : PitchActionDecorator<EndSetupWhenReady> {
    override fun decorate(
        actionProvider: ManualActionProvider,
        state: Game,
        descriptor: EndSetupWhenReady,
        owner: Team?,
        acc: UiSnapshotAccumulator
    ) {
        val title = when {
            state.stack.containsProcedure(SolidDefense) -> "End Solid Defense"
            state.stack.containsProcedure(QuickSnap) -> "End Quick Snap"
            else -> "End Setup"
        }
        acc.updateGameStatus {
            it.copy(
                centerBadgeText = title,
                centerBadgeAction = { actionProvider.userActionSelected(EndSetup) },
                centerBadgeEnabled = true
            )
        }
    }
}
