package com.jervisffb.ui.game.state.decorators

import com.jervisffb.engine.actions.EndSetup
import com.jervisffb.engine.actions.EndSetupWhenReady
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.state.ManualActionProvider

object EndSetupDecorator : FieldActionDecorator<EndSetupWhenReady> {
    override fun decorate(
        actionProvider: ManualActionProvider,
        state: Game,
        descriptor: EndSetupWhenReady,
        owner: Team?,
        acc: UiSnapshotAccumulator
    ) {
//        if (snapshot.actionsRequest.team?.isHomeTeam() == true) {
        acc.updateGameStatus {
            it.copy(
                centerBadgeText = "End Setup",
                centerBadgeAction = { actionProvider.userActionSelected(EndSetup) }
            )
        }
//            snapshot.homeTeamActions.add(
//                ButtonData(
//                    "End Setup",
//                    onClick = { actionProvider.userActionSelected(EndSetup) }
//                )
//            )
//        } else if (snapshot.actionsRequest.team?.isAwayTeam() == true) {
//            snapshot.awayTeamActions.add(
//                ButtonData(
//                    "End Setup",
//                    onClick = { actionProvider.userActionSelected(EndSetup) }
//                )
//            )
//        }
    }
}
