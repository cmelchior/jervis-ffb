package com.jervisffb.ui.game.state.decorators

import com.jervisffb.engine.actions.DogoutSelected
import com.jervisffb.engine.actions.SelectDogout
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.state.ManualActionProvider

object SelectDogoutDecorator: FieldActionDecorator<SelectDogout> {
    override fun decorate(
        actionProvider: ManualActionProvider,
        state: Game,
        descriptor: SelectDogout,
        owner: Team?,
        acc: UiSnapshotAccumulator
    ) {
        if (owner?.isAwayTeam() == true) {
            acc.awayDogoutOnClickAction = {
                actionProvider.userActionSelected(DogoutSelected)
            }
        } else {
            acc.homeDogoutOnClickAction = {
                actionProvider.userActionSelected(DogoutSelected)
            }
        }
    }
}
