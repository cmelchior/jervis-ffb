package com.jervisffb.ui.menu.components.starting

import com.jervisffb.engine.model.Team
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.JervisScreenModel
import kotlinx.coroutines.flow.Flow

/**
 * ViewModel class for "Start Game" component or sub-screen. This is not a full screen,
 * but is the last of of the flow for starting all types of stand-alone games.
 *
 * @see [StartGameComponent]
 */
class StartGameComponentModel(
    val homeTeam: Flow<Team?>,
    val awayTeam: Flow<Team?>,
    private val menuViewModel: MenuViewModel,
) : JervisScreenModel {
}
