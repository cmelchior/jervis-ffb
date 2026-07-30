package com.jervisffb.ui.game.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.jervisffb.ui.game.icons.ActionIcon
import com.jervisffb.ui.game.model.UiAction

/**
 * Interface representing each entry in a context menu.
 */
@Stable
sealed interface ContextMenuOption {
    val title: String
    val command: UiAction
    val icon: ActionIcon
}

// Context menus that does not carry state, but just trigger a simple effect.
data class SimpleContextMenuOption(
    override val title: String,
    override val command: UiAction,
    override val icon: ActionIcon,
) : ContextMenuOption

// Context menus that have state between each Game Action, i.e., something that
// can be enabled/disabled before the next game action.
class ToggleContextMenuOption(
    initial: ContextData,
    private val calculateStateFunc: () -> ContextData,
): ContextMenuOption {
    data class ContextData(val title: String, val icon: ActionIcon, val command: UiAction)

    // Snapshot state rather than a plain `var`, so that flipping the option notifies
    // composition. Without that, the `@Immutable` promise on the enclosing UiPitchSquare
    // would be a lie: the square would compare equal while displaying a stale title/icon.
    private var data: ContextData by mutableStateOf(initial)

    override val title: String
        get() = data.title
    override val command: UiAction
        get() = data.command
    override val icon: ActionIcon
        get() = data.icon

    fun recalculateState() {
        data = calculateStateFunc()
    }

    // [calculateStateFunc] is deliberately excluded: it is a raw lambda, so including it would
    // make `equals` always false. Two options with equal [data] have interchangeable
    // rules in practice, because the rule is keyed to the same player as the commands it hands
    // back.
    override fun equals(other: Any?): Boolean = (other is ToggleContextMenuOption && data == other.data)
    override fun hashCode(): Int = data.hashCode()
}

@Composable
fun ContextPopupMenu(
    // Boolean = true, if popup is manually dismissed
    hidePopup: (Boolean) -> Unit,
    commands: List<ContextMenuOption>,
) {
    // Calculate the offset of the popup, so it is displayed best on the screen
    // Prefer right of content, and then left. If there is no space, place on top.
    fun calculateOffset(
        anchorBounds: IntRect, // Bounds for the content we want to place popup around
        windowSize: IntSize, // Size of the window
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val screenLeft = 0
        val screenRight = windowSize.width
        val contentWidth = popupContentSize.width

        // Check for room on the right
        if (anchorBounds.right + contentWidth <= screenRight) {
            return IntOffset(anchorBounds.right, anchorBounds.bottom)
        }

        // Check for room on the left
        if (screenLeft + contentWidth <= anchorBounds.left) {
            return IntOffset(anchorBounds.left - contentWidth, anchorBounds.bottom)
        }

        // else do-best-effort starting from the right
        return IntOffset(
            anchorBounds.right.coerceIn(0, (screenRight - contentWidth)),
            anchorBounds.top.coerceIn(0, windowSize.height - popupContentSize.height),
        )
    }
    if (commands.isEmpty()) {
        hidePopup(false)
        return
    }
    Box(modifier = Modifier.fillMaxSize().clickable { /* Intercept events outside popup */ }) {
        Popup(
            popupPositionProvider =
                object : PopupPositionProvider {
                    override fun calculatePosition(
                        anchorBounds: IntRect,
                        windowSize: IntSize,
                        layoutDirection: LayoutDirection,
                        popupContentSize: IntSize,
                    ): IntOffset {
                        return calculateOffset(anchorBounds, windowSize, layoutDirection, popupContentSize)
                    }
                },
            properties = PopupProperties(),
            onDismissRequest = { hidePopup(true) },
        ) {
            Column(modifier = Modifier.width(IntrinsicSize.Max).background(MaterialTheme.colorScheme.background)) {
                commands.forEach { command ->
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .clickable {
                                    hidePopup(false)
                                    command.command()
                                },
                    ) {
                        Text(
                            modifier = Modifier.padding(4.dp),
                            text = command.title,
                        )
                    }
                }
            }
        }
    }
}
