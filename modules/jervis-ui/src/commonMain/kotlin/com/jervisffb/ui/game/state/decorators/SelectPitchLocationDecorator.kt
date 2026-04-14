package com.jervisffb.ui.game.state.decorators

import com.jervisffb.engine.actions.PitchSquareSelected
import com.jervisffb.engine.actions.SelectPitchLocation
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.state.ManualActionProvider

object SelectPitchLocationDecorator: PitchActionDecorator<SelectPitchLocation> {
    override fun decorate(
        actionProvider: ManualActionProvider,
        state: Game,
        descriptor: SelectPitchLocation,
        owner: Team?,
        acc: UiSnapshotAccumulator
    ) {
        descriptor.squares.forEach { squareData ->
            val selectedAction = {
                actionProvider.userActionSelected(PitchSquareSelected(squareData.coordinate))
            }
            acc.updateSquare(squareData.coordinate) {
                it.copy(
                    selectedAction = selectedAction,
                    requiresRoll = (squareData.requiresRush || squareData.requiresDodge || squareData.requiresJump)
                )
            }
        }
    }
}
