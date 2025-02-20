package com.jervisffb.ui.game.dialogs

import com.jervisffb.engine.model.Team

/**
 * Interface for describing dialogs that are shown to users.
 */
sealed interface UserInputDialog {
    var owner: Team?
}


