package com.jervisffb.ui.screen.components.starting

import com.jervisffb.engine.model.Team
import com.jervisffb.ui.screen.JervisScreenModel
import com.jervisffb.ui.viewmodel.MenuViewModel
import kotlinx.coroutines.flow.Flow

/**
 * ViewModel class for "Start Game" component or sub-screen. This is not a full screen,
 * but is the last of of the flow for starting all types of stand-alone games.
 */
class StartGameComponentModel(
    val homeTeam: Flow<Team?>,
    val awayTeam: Flow<Team?>,
    private val menuViewModel: MenuViewModel,
) : JervisScreenModel {
}
