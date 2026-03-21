package com.jervisffb.ui.game.dialogs

import androidx.compose.runtime.mutableStateOf
import com.jervisffb.engine.model.Team
import com.jervisffb.ui.menu.LocalFieldDataWrapper
import kotlinx.coroutines.DelicateCoroutinesApi

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
 *
 * The lifecycle of Action Wheels are complicated and somewhat dis-associated from actions
 * themselves. This is due to them sometimes persisting across actions and also needing
 * to animate between those stages.
 */
@OptIn(DelicateCoroutinesApi::class)
abstract class AbstractActionWheelViewModel(
    team: Team,
    // Used to connect the view model to the UI
    val sharedFieldData: LocalFieldDataWrapper,
): UserInputDialog {

    // Whether the entire wheel is visible or not
    var hideOnClickedOutside = mutableStateOf(false)

    // Which team is responsible for selecting the action? This can affect some coloring
    // choices.
    override var owner: Team? = team

    abstract fun showWheel()
    abstract fun hideWheel(onDismiss: (() -> Unit)? = null)
}
