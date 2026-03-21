package com.jervisffb.ui.game.view.field

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import com.jervisffb.ui.game.view.ActionWheelDialog
import com.jervisffb.ui.game.viewmodel.FieldViewModel
import com.jervisffb.ui.utils.applyIf

/**
 * Layer 12(low): Context Action-Wheel Layer.
 *
 * This layer is responsible for handling the Action Wheel when it is displayed as a context menu.
 * We have two layers for this to better handle transition between the Action Wheel as a dialog
 * and as a context menu.
 *
 * See [Field] for more details about layer ordering.
 */
@Composable
fun ContextMenuLayer(vm: FieldViewModel) {
    val contextActionWheelPresent by vm.sharedFieldData.isContextActionWheelVisible
    val fieldData by vm.fieldViewData.collectAsState()
    val currenState by vm.contextMenuViewModel
    var hideWhenClickOutside by currenState.hideOnClickedOutside
    val wheelState by currenState.data

    // Context menu visibility is different than the Action Wheel. Can we be sure
    // that this always works? I suspect so, since we also check if the main action wheel is present
    // We create an invisible layer for intercepting pointer events when the Action Wheel
    // is present.

    // If the action wheel is available, we need to filter pointer events
    // to prevent weird behavior. The following logic applies:
    //
    // 1. If clicking an Action Wheel button, it will consume the event.
    // 2. If clicking outside the wheel when it is visible, it will
    //    hide the wheel. The click should be consumed here.
    // 3. Hover events should always be allowed through.

    // We cannot animate visibility here, either using `AnimatedVisibility` or custom alpha.
    // The reason eludes me a bit, but the animation ends up being clipped. Probably because
    // Compose creates a custom layer for animations, and somehow it doesn't get the correct
    // dimensions. This requires further investigation. Moving the animation into the Action Wheel
    // itself fixes the issue.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .applyIf(contextActionWheelPresent) {
                pointerInput(vm.contextMenuViewModel) {
                    awaitPointerEventScope {
                        var pressDetected = false // Track press, so we can filter Release correctly
                        while (true) {
                            val e = awaitPointerEvent()
                            // Action Wheel Buttons might already have consumed the event, which we need to respect here.
                            if (e.changes.any { it.isConsumed }) continue
                            if (e.buttons.isSecondaryPressed) continue
                            when (e.type) {
                                PointerEventType.Press -> {
                                    // Press is allowed to reach lower layers when the wheel isn't visible
                                    if (vm.sharedFieldData.isContextActionWheelVisible.value) {
                                        pressDetected = true
                                        vm.contextMenuViewModel.let {
                                            if (hideWhenClickOutside) {
                                                it.value.hideWheel()
                                            }
                                        }
                                        e.changes.forEach { it.consume() }
                                    } else {
                                        pressDetected = false
                                    }
                                }
                                PointerEventType.Release -> {
                                    if (pressDetected) {
                                        e.changes.forEach { it.consume() }
                                        pressDetected = false
                                    }
                                }
                                else -> {
                                    // Need to pass events to Jervis
                                }
                            }
                        }
                    }
                }
            }
    ) {
        ActionWheelDialog(
            uiState = wheelState,
            vm,
            fieldData,
        )
    }
}

