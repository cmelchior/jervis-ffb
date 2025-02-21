package com.jervisffb.ui.game.viewmodel

import com.jervisffb.ui.game.UiGameController
import com.jervisffb.ui.game.state.RandomActionProvider
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

    private val actionProvider: RandomActionProvider = uiState.currentActionProvider as RandomActionProvider

    fun startActions() {
        actionProvider.startActionProvider()
    }

    fun pauseActions() {
        actionProvider.pauseActionProvider()
    }
}
