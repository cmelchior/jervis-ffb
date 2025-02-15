package com.jervisffb.ui.screen.p2p

import com.jervisffb.ui.screen.JervisScreenModel
import com.jervisffb.ui.viewmodel.MenuViewModel

/**
 * ViewModel class for the Team Selector subscreen. This is not a full screen,
 * but is a part of a flow when starting either Peer-to-Peer, Hotseat or AI
 * games.
 */
class StartGameScreenModel(
    private val menuViewModel: MenuViewModel,
) : JervisScreenModel {
    fun reset() {
        // Nothing to reset
    }
}
