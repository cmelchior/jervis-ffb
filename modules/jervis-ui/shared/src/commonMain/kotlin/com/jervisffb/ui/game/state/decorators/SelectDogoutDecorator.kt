package com.jervisffb.ui.game.state.decorators

import com.jervisffb.engine.actions.DogoutSelected
import com.jervisffb.engine.actions.SelectDogout
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.model.GuardedAction
import com.jervisffb.ui.game.state.ManualActionProvider

object SelectDogoutDecorator: PitchActionDecorator<SelectDogout> {
    override fun decorate(
        actionProvider: ManualActionProvider,
        state: Game,
        descriptor: SelectDogout,
        owner: Team?,
        acc: UiSnapshotAccumulator
    ) {
        if (owner?.isAwayTeam() == true) {
            acc.awayDogoutOnClickAction = GuardedAction(actionProvider.controller) { id ->
                actionProvider.userActionSelected(id, DogoutSelected)
            }
        } else {
            acc.homeDogoutOnClickAction = GuardedAction(actionProvider.controller) { id ->
                actionProvider.userActionSelected(id, DogoutSelected)
            }
        }
    }
}
