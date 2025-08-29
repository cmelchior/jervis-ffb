package com.jervisffb.ui.game.model

import com.jervisffb.ui.game.viewmodel.UiPlayerTransientData

/**
 * Contains all the information needed to display a player in the dogout.
 */
data class UiSidebarPlayer(
    val player: UiFieldPlayer,
    val transientData: UiPlayerTransientData?,
) {
    val state get() = player.state
    val number get() = player.number
    val isOnHomeTeam get() = player.isOnHomeTeam
    val position get() = player.position
    val isActive get() = player.isActive
    val isGoingDown get() = player.isGoingDown
    val hasActivated get() = player.hasActivated
}
