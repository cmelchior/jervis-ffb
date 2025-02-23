package com.jervisffb.ui.game.viewmodel

import com.jervisffb.ui.game.UiGameController
import com.jervisffb.ui.game.state.UiActionProvider
import com.jervisffb.ui.menu.GameScreenModel

/**
 * View model responsible for controlling "Random mode", i.e. will just generate
 * a ranndom action in order to progress the game state.
 *
 * This sequence can be started and paused, and the frequency can be adjusted.
 *
 * This mode is mostly for development purposes.
 */
class RandomActionsControllerViewModel(
    uiState: UiGameController,
    screenModel: GameScreenModel,
) {

    init {
        // Need to convert this to a Random AI Player
        TODO()
    }

    private val actionProvider: UiActionProvider = uiState.actionProvider

    fun startActions() {
        TODO("FIgure out how to start the random action provider")
        // actionProvider.startActionProvider()
    }

    fun pauseActions() {
        TODO("FIgure out how to pause the random action provider")
        // actionProvider.pauseActionProvider()
    }
}
