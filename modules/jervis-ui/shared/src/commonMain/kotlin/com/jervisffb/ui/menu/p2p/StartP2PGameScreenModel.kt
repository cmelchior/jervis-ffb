package com.jervisffb.ui.menu.p2p

import com.jervisffb.engine.model.Team
import com.jervisffb.ui.game.model.ModelRef
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.JervisScreenModel
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel class for controlling the screen asking the user to accept the game.
 */
class StartP2PGameScreenModel(
    private val networkAdapter: P2PClientNetworkAdapter,
    private val menuViewModel: MenuViewModel,
) : JervisScreenModel {

    val homeTeam: StateFlow<ModelRef<Team>?> = networkAdapter.homeTeam
    val awayTeam: StateFlow<ModelRef<Team>?> = networkAdapter.awayTeam

    fun reset() {
        // Nothing to reset
    }
}
