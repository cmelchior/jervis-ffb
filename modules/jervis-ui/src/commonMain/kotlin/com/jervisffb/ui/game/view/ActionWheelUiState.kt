package com.jervisffb.ui.game.view

import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.ui.game.dialogs.wheel.ButtonData
import com.jervisffb.ui.game.dialogs.wheel.ButtonLayoutMode
import com.jervisffb.ui.game.dialogs.wheel.MenuExpandMode
import kotlin.math.ceil

/**
 * Classes responsible for representing the Context Wheel.
 * This is seperate from [ActionWheelUiState] as there are enough changes
 * between Primary and Context Action Wheels, that using the same API is
 * challenging.
 *
 * All of these APIs are still a bit of a mess, though, and probably need
 * another pass.
 */
sealed interface ContextWheelUiState
data class ContextWheelMenu(
    val coordinates: FieldCoordinate,
    val options: List<ContextMenuOption>,
): ContextWheelUiState
object NoContextMenu: ContextWheelUiState

/**
 * Classes responsible for representing the stable state of an Action Wheel.
 * The UI method [ActionWheel], is responsible for rendering the actual changes
 * between them, but these states do provide hints to it on what kind of animation
 * should be performed.
 */
sealed class ActionWheelUiState {
    // Where should the Action Wheel be placed? If `null` it will be centered in the middle of the screen.
    // Otherwise, we will do a best-effort attempt at placing the circle directly over the `center` coordinate,
    // but in case there is no room, like if the square is too close to the edge. It will be placed to the side
    // with an arrow pointing it. How this happens is up to the UI.
    open val center: FieldCoordinate? = null
    open val ringAnimationMode: ButtonLayoutMode = ButtonLayoutMode.STABLE
    open val topItems: List<ButtonData> = emptyList()
    open val topExpandMode: MenuExpandMode = MenuExpandMode.Compact()
    open val topAnimationType: ButtonLayoutMode = ButtonLayoutMode.STABLE
    open val bottomItems: List<ButtonData> = emptyList()
    open val bottomExpandMode: MenuExpandMode = MenuExpandMode.Compact()
    open val bottomAnimationType: ButtonLayoutMode = ButtonLayoutMode.STABLE
    open val onDismiss: (() -> Unit)? = null
    open val animationOnly: Boolean = false
    open val hideWhenClickOutside: Boolean = false
    open val enableAnimation: Boolean = true
    open val bottomMessage: String? = null // Message being shown as long as no onHover message is shown

    open fun isLastActionUndo(): Boolean = false

    fun isHiding(): Boolean {
        return ringAnimationMode == ButtonLayoutMode.HIDE || ringAnimationMode == ButtonLayoutMode.CONTRACT_UNDO
    }

    override fun toString(): String {
        return "${this::class.simpleName}[center=$center, topItems=${topItems.size}, bottomItems=${bottomItems.size}, ringAnim=${ringAnimationMode}, topAnim=${topAnimationType}, bottomAnim=${bottomAnimationType}"
    }
}
object NoActionWheel: ActionWheelUiState() {
    override val ringAnimationMode: ButtonLayoutMode = ButtonLayoutMode.STABLE
    override val topAnimationType: ButtonLayoutMode = ButtonLayoutMode.STABLE
    override val bottomAnimationType: ButtonLayoutMode = ButtonLayoutMode.STABLE

}
class HideActionWheel(hideImmediately: Boolean): ActionWheelUiState() {
    val animationMode = when (hideImmediately) {
        true -> ButtonLayoutMode.CONTRACT_UNDO
        false -> ButtonLayoutMode.HIDE
    }
    override val ringAnimationMode: ButtonLayoutMode = animationMode
    override val topAnimationType: ButtonLayoutMode = animationMode
    override val bottomAnimationType: ButtonLayoutMode = animationMode
}

data class ActionWheelUiStateData(
    override val center: FieldCoordinate?,
    override val ringAnimationMode: ButtonLayoutMode = ButtonLayoutMode.STABLE,
    override val topItems: List<ButtonData> = emptyList(),
    override val topExpandMode: MenuExpandMode = MenuExpandMode.Compact(),
    override val topAnimationType: ButtonLayoutMode = ButtonLayoutMode.STABLE,
    override val bottomItems: List<ButtonData> = emptyList(),
    override val bottomExpandMode: MenuExpandMode = MenuExpandMode.Compact(),
    override val bottomAnimationType: ButtonLayoutMode = ButtonLayoutMode.STABLE,
    override val onDismiss: (() -> Unit)? = null,
    override val animationOnly: Boolean = false,
    override val hideWhenClickOutside: Boolean = false,
    override val enableAnimation: Boolean = true,
    override val bottomMessage: String? = null,
): ActionWheelUiState() {

    override fun isLastActionUndo(): Boolean {
        return lastActionWasUndo
    }

    var lastActionWasUndo: Boolean = false

    // Only used in `COMPACT` mode and defines the distance in degrees between
    // each sub item.
    private val stepAngle = 45.0f

    init {
        recalculateSubMenuAngles(topItems, topExpandMode, -90f)
        recalculateSubMenuAngles(bottomItems, bottomExpandMode, 90f)
    }

    private fun recalculateSubMenuAngles(
        buttons: List<ButtonData>,
        expandMode: MenuExpandMode,
        startAngle: Float
    ) {
        if (buttons.isEmpty()) return
        buttons.forEach {
            it.defaultStartingAngle = startAngle
        }
        when (val mode = expandMode) {
            MenuExpandMode.None -> error("Not supported")
            MenuExpandMode.TwoWay -> {
                buttons.forEachIndexed { index, item ->
                    when (index) {
                        0 -> {
                            item.defaultStartingAngle = 0f
                            item.targetAngle = -90f
                        }
                        1 -> {
                            item.defaultStartingAngle = 180f
                            item.targetAngle = 90f
                        }
                        else -> error("Too many item: ${buttons.size}")
                    }
                }
            }
            is MenuExpandMode.FanOut -> {
                // If no parent, evenly distribute items in the ring.
                // For an even number of items, one will always be on the `defaultStartAngle`
                // For an odd number of items, `defaultStartAngle` will be in the middle between two items.
                // directly on `centerAngle`
                buttons.forEachIndexed { index, item ->
                    item.targetAngle = (mode.spread / buttons.size) * index + startAngle
                }
            }
            is MenuExpandMode.Compact -> {
                // Clump menu items together at `centerAngle`. For an even number of menu items
                // This means none of them will be directly on `centerAngle`.
                val offset = if (buttons.size % 2 == 0) stepAngle / 2f else 0f
                // val parentModifier = if (parent == null) 0 else 1
                val parentModifier = 0
                buttons.forEachIndexed { index, item ->
                    // This will swap direction (starting with clockwise) and gradually move out,
                    // so [cw(1), ccw(1), cw(2), ccw(2), ...]
                    val direction = if (index % 2 == 1) -1 else 1
                    val magnitude = ceil((index + parentModifier) / 2.0).toFloat()
                    item.targetAngle = (startAngle + offset + direction * magnitude * stepAngle)
                }
            }
        }
    }

    companion object {
        val None = ActionWheelUiStateData(center = null)
    }
}
