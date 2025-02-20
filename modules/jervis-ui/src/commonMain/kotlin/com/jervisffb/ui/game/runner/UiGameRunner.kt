package com.jervisffb.ui.game.runner

import com.jervisffb.engine.GameRunner
import com.jervisffb.ui.game.state.UiActionProvider

/**
 * Game runner with extra UI capabilities (mostly used to make sure we can get the right things
 */
interface UiGameRunner: GameRunner {
    var actionProvider: UiActionProvider?
}
