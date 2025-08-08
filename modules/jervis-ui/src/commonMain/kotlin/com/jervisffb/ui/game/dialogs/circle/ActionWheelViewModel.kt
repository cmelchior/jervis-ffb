package com.jervisffb.ui.game.dialogs.circle

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.locations.FieldCoordinate
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * ViewModel wrapping all relevant state, we need to modify an Action Wheel menu.
 * All properties used by the UI should be exposed as [androidx.compose.runtime.MutableState]
 * so the UI can react correctly to changes.
 *
 * At a high level the menu comes in two modes:
 *
 * 1. A "simple" menu system with submenus: In this case, all menu items
 *    are the same. Items can either be evenly spread out or grouped
 *    together.
 *
 * 2. A mix of actions and dice: Dice are placed at the top, actions at the bottom.
 *
 * The [com.jervisffb.ui.game.view.ActionWheelDialog] will attempt to nicely animate changes to
 * the best of its abilities. If you want to "reset" the state, it is quickest
 * to just create a new [ActionWheelViewModel] instance.
 */
class ActionWheelViewModel(
    team: Team,
    // Where should the Action Wheel be placed? If `null` it will be centered in the middle of the screen.
    // We will do a best-effort attempt at placing the circle directly over `center`, but in case there is not
    // room, like if the square is closed to the edge. It will be placed to the side with an arrow pointing it.
    // How this happens is up to the UI.
    val center: FieldCoordinate?,
    startHoverText: String? = null,
    // If true, `startHoverText` will be used when no other hover text is showing.
    // Otherwise, it will only show until hidden for the first time
    fallbackToShowStartHoverText: Boolean = false,
    topExpandMode: MenuExpandMode = MenuExpandMode.COMPACT,
    bottomExpandMode: MenuExpandMode = MenuExpandMode.COMPACT,
    var shown: MutableState<Boolean> = mutableStateOf(true),
    val hideOnClickedOutside: Boolean = false,
    val onMenuHidden: (() -> Unit)? = null
) {
    // Wheel is shown on screen
    // var shown by mutableStateOf(true)
    var topMenu = ActionWheelMenuController(this, -90f, topExpandMode)
    var bottomMenu = ActionWheelMenuController(this, 90f, bottomExpandMode)

    // Hover text properties
    var fallbackToStartHoverText: Boolean by mutableStateOf(fallbackToShowStartHoverText)
    var startingHoverText: String? by mutableStateOf(startHoverText)
    var startingHoverTextHasBeenHidden: Boolean by mutableStateOf(false)
    var hoverText = MutableStateFlow(startHoverText)

    // Properties controlling message boxes (shown instead of actions)
    var topMessage: String? by mutableStateOf(null)
    var bottomMessage: String? by mutableStateOf(null)

    // Which team is responsible for selecting the action? This can affect some coloring
    // choices.
    var owner: Team by mutableStateOf(team)

    fun onActionOptionsExpandChange(item: ActionWheelMenuItem, expanded: Boolean) {
        topMenu.menuItems.forEach {
            it.onActionOptionsExpandChange(item, expanded)
        }
        bottomMenu.menuItems.forEach {
            it.onActionOptionsExpandChange(item, expanded)
        }
    }

    fun showWheel() {
        shown.value = true
    }

    fun hideWheel(actionSelected: Boolean) {
        shown.value = false
        if (!actionSelected) {
            onMenuHidden?.invoke()
        }
    }
}
