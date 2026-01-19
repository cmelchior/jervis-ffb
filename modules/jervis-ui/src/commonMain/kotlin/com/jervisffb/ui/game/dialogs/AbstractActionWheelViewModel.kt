package com.jervisffb.ui.game.dialogs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.ui.game.dialogs.wheel.ActionWheelMenuController
import com.jervisffb.ui.game.dialogs.wheel.MenuExpandMode
import com.jervisffb.ui.game.icons.ActionIcon
import com.jervisffb.ui.game.state.actionwheel.ActionWheelDialogController
import com.jervisffb.ui.menu.LocalFieldDataWrapper
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.jvm.JvmInline
import kotlin.time.Duration

/**
 * This class describes a given "change" to the action wheel.
 * The UI will attempt to animate changes between states to the best of its ability.
 * Failing that, an event will be just reset any animation state.
 *
 * On a high level, each event should be isolated, meaning we can always
 * apply it, regardless of the current state.
 *
 * This also makes it possible to load a save file from any state and be sure
 * that the Action Wheel can be displayed correctly.
 */
sealed interface ActionWheelEvent {
    val center: FieldCoordinate?
}

// Fade out the entire action wheel, hiding it
data class ShowActionWheel(
    override val center: FieldCoordinate?,
) : ActionWheelEvent

// Fade in the entire action wheel in its current state
object HideActionWheel : ActionWheelEvent {
    override val center: FieldCoordinate? = null
}

data class ExpandD6Values(
    override val center: FieldCoordinate? = null
) : ActionWheelEvent

data class ContractD6Values(
    override val center: FieldCoordinate? = null
) : ActionWheelEvent

data class ShowRerollOptions(
    override val center: FieldCoordinate? = null
) : ActionWheelEvent

// These ID's identify a single button. They should be stable across
// action requests so we can animate changes correctly.
@JvmInline
value class ButtonId(val id: String)

@Suppress("UNCHECKED_CAST")
data class RollAnimationData<T: DieResult>(
    val endValue: T,
    val additionalDelayAfterRoll: Duration? = ActionWheelDialogController.DEFAULT_DELAY_AFTER_ROLL
) {
    val startingValue: T = endValue.allOptions(endValue).random() as T
    val intermediateValue: T = endValue.allOptions(endValue, startingValue).random() as T
}

sealed interface ButtonData {
    val id: ButtonId
    val action: () -> Unit
    val label: () -> String?
    var targetAngle: Float
    var defaultStartingAngle: Float
    val animateRoll: RollAnimationData<*>?
}

data class ActionButtonData(
    override val id: ButtonId,
    override val label: () -> String,
    val icon: ActionIcon,
    override val action: () -> Unit,
    val enabled: Boolean = true,
): ButtonData {
    override var targetAngle: Float = 0f
    override var defaultStartingAngle: Float = 0f
    override val animateRoll: RollAnimationData<*>? = null
}

data class DieButtonData<T: DieResult>(
    override val id: ButtonId,
    override val label: () -> String?,
    var diceValue: T,
    override val action: () -> Unit,
    val options: List<T>,
    val expandable: Boolean,
    val enabled: Boolean = true,
    override val animateRoll: RollAnimationData<T>? = null,
    val preferLtr: Boolean = true
): ButtonData {
    override var targetAngle: Float = 0f
    override var defaultStartingAngle: Float = 0f
}

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
abstract class AbstractActionWheelViewModel(
    team: Team,
    // Used to connect the view model to the UI
    val sharedFieldData: LocalFieldDataWrapper,
    // Title for the Action Wheel state, this is being displayed above the field, and is decoupled from it.
    var title: String? = null,
    // Similar to the title, but will be displayed inside the Action Wheel.
    // TODO: With the introduction of title, do we ever need this?
    startHoverText: String? = null,
    // If true, `startHoverText` will be used when no other hover text is showing.
    // Otherwise, it will only show until hidden for the first time
    fallbackToShowStartHoverText: Boolean = false,
    topExpandMode: MenuExpandMode = MenuExpandMode.Compact(),
    bottomExpandMode: MenuExpandMode = MenuExpandMode.Compact(),
    var hideOnClickedOutside: Boolean = false,
    var onMenuHidden: (() -> Unit)? = null,
    // This contains the set of nodes we know this wheel model can handle. This is used as an addition
    // to `hideWhenActionIsSelected` and we use this to be able to hide action wheels in the next game-loop.
    // The two overlap to a big degree, but in some cases we might want to trigger an animation before
    // handling the actual event.
    val availableInNodes: Set<Node>? = null,
    val autoShowOnNewActionButtons: Boolean = true
): UserInputDialog {
    val animationDuration: Int = 300

    // Whether the entire wheel is visible or not
    var isVisible = mutableStateOf(false)

    // Where should the Action Wheel be placed? If `null` it will be centered in the middle of the screen.
    // Otherwise, we will do a best-effort attempt at placing the circle directly over the `center` coordinate,
    // but in case there is no room, like if the square is too close to the edge. It will be placed to the side
    // with an arrow pointing it. How this happens is up to the UI.
    var center = mutableStateOf<FieldCoordinate?>(null)

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
    override var owner: Team? = team

    var version by mutableStateOf(0)

    init {
        GlobalScope.launch {
            hoverText.collect { text ->
                if (text != null) {
                    startingHoverTextHasBeenHidden = true
                }
            }
        }
    }

    abstract fun showWheel()
    abstract fun hideWheel(userUiAction: Boolean)
}
