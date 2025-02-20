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
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties

data class ContextMenuOption(val title: String, val command: () -> Unit)

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
            Column(modifier = Modifier.width(IntrinsicSize.Max).background(MaterialTheme.colors.background)) {
                commands.forEach { (title, cmd) ->
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colors.background)
                                .clickable {
                                    hidePopup(false)
                                    cmd()
                                },
                    ) {
                        Text(
                            modifier = Modifier.padding(4.dp),
                            text = title,
                        )
                    }
                }
            }
        }
    }
}
