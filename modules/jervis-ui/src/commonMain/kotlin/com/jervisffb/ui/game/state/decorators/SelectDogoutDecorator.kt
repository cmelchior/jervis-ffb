package com.jervisffb.ui.game.state.decorators

import com.jervisffb.engine.actions.DogoutSelected
import com.jervisffb.engine.actions.SelectDogout
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.ui.game.UiGameSnapshot
import com.jervisffb.ui.game.state.ManualActionProvider

object SelectDogoutDecorator: FieldActionDecorator<SelectDogout> {
    override fun decorate(
        actionProvider: ManualActionProvider,
        state: Game,
        snapshot: UiGameSnapshot,
        descriptor: SelectDogout,
        owner: Team?
    ) {
        if (owner?.isAwayTeam() == true) {
            snapshot.awayDogoutOnClickAction = {
                actionProvider.userActionSelected(DogoutSelected)
            }
        } else {
            snapshot.homeDogoutOnClickAction = {
                actionProvider.userActionSelected(DogoutSelected)
            }
        }
    }
}
