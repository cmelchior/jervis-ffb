package com.jervisffb.ui.game.state.decorators

import com.jervisffb.engine.actions.DogoutSelected
import com.jervisffb.engine.actions.SelectDogout
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.model.GuardedAction
import com.jervisffb.ui.game.state.UiActionProvider

object SelectDogoutDecorator: PitchActionDecorator<SelectDogout> {
    override fun decorate(
        actionProvider: UiActionProvider,
        state: Game,
        descriptor: SelectDogout,
        owner: Team?,
        isEnabled: Boolean,
        acc: UiSnapshotAccumulator
    ) {
        if (owner?.isAwayTeam() == true) {
            acc.awayDogoutLooksSelectable = true
            acc.awayDogoutOnClickAction = GuardedAction(acc) { id ->
                actionProvider.userActionSelected(id, DogoutSelected)
            }.takeIf { isEnabled}
        } else {
            acc.homeDogoutLooksSelectable = true
            acc.homeDogoutOnClickAction = GuardedAction(acc) { id ->
                actionProvider.userActionSelected(id, DogoutSelected)
            }.takeIf { isEnabled}
        }
    }
}
