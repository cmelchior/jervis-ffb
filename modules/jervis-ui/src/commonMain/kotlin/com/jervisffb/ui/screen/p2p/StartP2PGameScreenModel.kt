package com.jervisffb.ui.screen.p2p

import com.jervisffb.engine.model.Team
import com.jervisffb.ui.screen.JervisScreenModel
import com.jervisffb.ui.viewmodel.MenuViewModel
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel class for controlling the screen asking the user to accept the game.
 */
class StartP2PGameScreenModel(
    private val controller: P2PClientGameController,
    private val menuViewModel: MenuViewModel,
) : JervisScreenModel {

    val homeTeam: StateFlow<Team?> = controller.homeTeam
    val awayTeam: StateFlow<Team?> = controller.awayTeam

    fun reset() {
        // Nothing to reset
    }
}
