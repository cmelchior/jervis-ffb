package com.jervisffb.ui.menu.components

/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.areAnyPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * This file contains a copy of the Material3 TooltipArea. Normally, this is only available
 * for Desktop, but having it here makes it available everywhere. The behavior probably doesn't
 * make sense on touch screen. So the implementation needs to be revisited, but for now it should
 * work fine on Web and Desktop.
 */

/**
 * Sets the tooltip for an element.
 *
 * @param tooltip Composable content of the tooltip.
 * @param modifier The modifier to be applied to the layout.
 * @param delayMillis Delay in milliseconds.
 * @param tooltipPlacement Defines position of the tooltip.
 * @param content Composable content that the current tooltip is set to.
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun JervisTooltipArea(
    tooltip: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    delayMillis: Int = 300,
    tooltipPlacement: JervisTooltipPlacement = JervisTooltipPlacement.CursorPoint(
        offset = DpOffset(0.dp, 16.dp)
    ),
    content: @Composable () -> Unit
) {
    var parentBounds by remember { mutableStateOf(Rect.Zero) }
    var cursorPosition by remember { mutableStateOf(Offset.Zero) }
    var isVisible by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var job: Job? by remember { mutableStateOf(null) }

    fun startShowing() {
        if (job?.isActive == true) {  // Don't restart the job if it's already active
            return
        }
        job = scope.launch {
            delay(delayMillis.toLong())
            isVisible = true
        }
    }

    fun hide() {
        job?.cancel()
        job = null
        isVisible = false
    }

    fun hideIfNotHovered(globalPosition: Offset) {
        if (!parentBounds.contains(globalPosition)) {
            hide()
        }
    }

    Box(
        modifier = modifier
            .onGloballyPositioned { parentBounds = it.boundsInWindow() }
            .onPointerEvent(PointerEventType.Enter) {
                cursorPosition = it.position
                if (!isVisible && !it.buttons.areAnyPressed) {
                    startShowing()
                }
            }
            .onPointerEvent(PointerEventType.Move) {
                cursorPosition = it.position
                if (!isVisible && !it.buttons.areAnyPressed) {
                    startShowing()
                }
            }
            .onPointerEvent(PointerEventType.Exit) {
                hideIfNotHovered(parentBounds.topLeft + it.position)
            }
            .onPointerEvent(PointerEventType.Press, pass = PointerEventPass.Initial) {
                hide()
            }
    ) {
        content()
        if (isVisible) {
            @OptIn(ExperimentalFoundationApi::class)
            Popup(
                popupPositionProvider = tooltipPlacement.positionProvider(cursorPosition),
                onDismissRequest = { isVisible = false }
            ) {
                var popupPosition by remember { mutableStateOf(Offset.Zero) }
                Box(
                    Modifier
                        .onGloballyPositioned { popupPosition = it.positionInWindow() }
                        .onPointerEvent(PointerEventType.Move) {
                            hideIfNotHovered(popupPosition + it.position)
                        }
                        .onPointerEvent(PointerEventType.Exit) {
                            hideIfNotHovered(popupPosition + it.position)
                        }
                ) {
                    tooltip()
                }
            }
        }
    }
}

private val PointerEvent.position get() = changes.first().position

/**
 * An interface for providing a [PopupPositionProvider] for the tooltip.
 */
@ExperimentalFoundationApi
interface JervisTooltipPlacement {
    /**
     * Returns [PopupPositionProvider] implementation.
     *
     * @param cursorPosition The position of the mouse cursor relative to the tooltip area.
     */
    @Composable
    fun positionProvider(cursorPosition: Offset): PopupPositionProvider

    /**
     * [TooltipPlacement] implementation for providing a [PopupPositionProvider] that calculates
     * the position of the popup relative to the current mouse cursor position.
     *
     * @param offset [DpOffset] to be added to the position of the popup.
     * @param alignment The alignment of the popup relative to the current cursor position.
     * @param windowMargin Defines the area within the window that limits the placement of the popup.
     */
    @ExperimentalFoundationApi
    class CursorPoint(
        private val offset: DpOffset = DpOffset.Zero,
        private val alignment: Alignment = Alignment.BottomEnd,
        private val windowMargin: Dp = 4.dp
    ) : JervisTooltipPlacement {
        @OptIn(ExperimentalComposeUiApi::class)
        @Composable
        override fun positionProvider(cursorPosition: Offset) =
            rememberPopupPositionProviderAtPosition(
                positionPx = cursorPosition,
                offset = offset,
                alignment = alignment,
                windowMargin = windowMargin
            )
    }

    /**
     * [TooltipPlacement] implementation for providing a [PopupPositionProvider] that calculates
     * the position of the popup relative to the current component bounds.
     *
     * @param anchor The anchor point relative to the current component bounds.
     * @param alignment The alignment of the popup relative to the [anchor] point.
     * @param offset [DpOffset] to be added to the position of the popup.
     */
    @ExperimentalFoundationApi
    class ComponentRect(
        private val anchor: Alignment = Alignment.BottomCenter,
        private val alignment: Alignment = Alignment.BottomCenter,
        private val offset: DpOffset = DpOffset.Zero
    ) : JervisTooltipPlacement {
        @Composable
        override fun positionProvider(cursorPosition: Offset) =
            rememberComponentRectPositionProvider(
                anchor = anchor,
                alignment = alignment,
                offset = offset
            )
    }
}

/**
 * Provides [PopupPositionProvider] relative to the current component bounds.
 *
 * @param anchor The anchor point relative to the current component bounds.
 * @param alignment The alignment of the popup relative to the [anchor] point.
 * @param offset [DpOffset] to be added to the position of the popup.
 */
@Composable
private fun rememberComponentRectPositionProvider(
    anchor: Alignment = Alignment.BottomCenter,
    alignment: Alignment = Alignment.BottomCenter,
    offset: DpOffset = DpOffset.Zero
): PopupPositionProvider {
    val offsetPx = with(LocalDensity.current) {
        IntOffset(offset.x.roundToPx(), offset.y.roundToPx())
    }
    return remember(anchor, alignment, offsetPx) {
        object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize
            ): IntOffset {
                val anchorPoint = anchor.align(IntSize.Zero, anchorBounds.size, layoutDirection)
                val tooltipArea = IntRect(
                    IntOffset(
                        anchorBounds.left + anchorPoint.x - popupContentSize.width,
                        anchorBounds.top + anchorPoint.y - popupContentSize.height,
                    ),
                    IntSize(
                        popupContentSize.width * 2,
                        popupContentSize.height * 2
                    )
                )
                val position = alignment.align(popupContentSize, tooltipArea.size, layoutDirection)
                return tooltipArea.topLeft + position + offsetPx
            }
        }
    }
}


/**
 * A [PopupPositionProvider] that positions the popup at the given position relative to the anchor.
 *
 * @param positionPx the offset, in pixels, relative to the anchor, to position the popup at.
 * @param offset [DpOffset] to be added to the position of the popup.
 * @param alignment The alignment of the popup relative to desired position.
 * @param windowMargin Defines the area within the window that limits the placement of the popup.
 */
@ExperimentalComposeUiApi
@Composable
fun rememberPopupPositionProviderAtPosition(
    positionPx: Offset,
    offset: DpOffset = DpOffset.Zero,
    alignment: Alignment = Alignment.BottomEnd,
    windowMargin: Dp = 4.dp
): PopupPositionProvider = with(LocalDensity.current) {
    val offsetPx = Offset(offset.x.toPx(), offset.y.toPx())
    val windowMarginPx = windowMargin.roundToPx()

    remember(positionPx, offsetPx, alignment, windowMarginPx) {
        PopupPositionProviderAtPosition(
            positionPx = positionPx,
            isRelativeToAnchor = true,
            offsetPx = offsetPx,
            alignment = alignment,
            windowMarginPx = windowMarginPx
        )
    }
}

/**
 * A [PopupPositionProvider] that positions the popup at the given offsets and alignment.
 *
 * @param positionPx The offset of the popup's location, in pixels.
 * @param isRelativeToAnchor Whether [positionPx] is relative to the anchor bounds passed to
 * [calculatePosition]. If `false`, it is relative to the window.
 * @param offsetPx Extra offset to be added to the position of the popup, in pixels.
 * @param alignment The alignment of the popup relative to desired position.
 * @param windowMarginPx Defines the area within the window that limits the placement of the popup,
 * in pixels.
 */
@ExperimentalComposeUiApi
class PopupPositionProviderAtPosition(
    val positionPx: Offset,
    val isRelativeToAnchor: Boolean,
    val offsetPx: Offset,
    val alignment: Alignment = Alignment.BottomEnd,
    val windowMarginPx: Int,
): PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        val anchor = IntRect(
            offset = positionPx.round() +
                (if (isRelativeToAnchor) anchorBounds.topLeft else IntOffset.Zero),
            size = IntSize.Zero)
        val tooltipArea = IntRect(
            IntOffset(
                anchor.left - popupContentSize.width,
                anchor.top - popupContentSize.height,
            ),
            IntSize(
                popupContentSize.width * 2,
                popupContentSize.height * 2
            )
        )
        val position = alignment.align(popupContentSize, tooltipArea.size, layoutDirection)
        var x = tooltipArea.left + position.x + offsetPx.x
        var y = tooltipArea.top + position.y + offsetPx.y
        if (x + popupContentSize.width > windowSize.width - windowMarginPx) {
            x -= popupContentSize.width
        }
        if (y + popupContentSize.height > windowSize.height - windowMarginPx) {
            y -= popupContentSize.height + anchor.height
        }
        x = x.coerceAtLeast(windowMarginPx.toFloat())
        y = y.coerceAtLeast(windowMarginPx.toFloat())

        return IntOffset(x.roundToInt(), y.roundToInt())
    }
}
