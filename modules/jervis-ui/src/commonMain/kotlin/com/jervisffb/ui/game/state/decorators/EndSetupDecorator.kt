package com.jervisffb.ui.game.state.decorators

import com.jervisffb.engine.actions.EndSetup
import com.jervisffb.engine.actions.EndSetupWhenReady
import com.jervisffb.engine.model.Game
import com.jervisffb.ui.game.UiGameSnapshot
import com.jervisffb.ui.game.state.ManualActionProvider

class EndSetupDecorator() : FieldActionDecorator<EndSetupWhenReady> {
    override fun decorate(actionProvider: ManualActionProvider, state: Game, snapshot: UiGameSnapshot, descriptor: EndSetupWhenReady) {
//        if (snapshot.actionsRequest.team?.isHomeTeam() == true) {
        snapshot.centerBadgeText = "End Setup"
        snapshot.centerBadgeAction = { actionProvider.userActionSelected(EndSetup) }
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
