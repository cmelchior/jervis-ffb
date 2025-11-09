package com.jervisffb.ui.game.view.field

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import com.jervisffb.ui.game.dialogs.ActionWheelInputDialog
import com.jervisffb.ui.game.view.ActionWheelDialog
import com.jervisffb.ui.game.viewmodel.FieldViewModel

/**
 * Layer 12: Action Wheel.
 *
 * This layer is responsible for handling the Action Wheel.
 *
 * See [Field] for more details about layer ordering.
 */
@Composable
fun ActionWheelLayer(
    vm: FieldViewModel,
) {
    val dialogData: ActionWheelInputDialog? by vm.observeActionWheel().collectAsState(null)
    val fieldData by vm.fieldViewData.collectAsState()
    dialogData?.let { inputDialog ->
        inputDialog.viewModel.updateSharedFieldData(vm.sharedFieldData)
        // We create an invisible layer for intercepting pointer events when the Action Wheel
        // is potentially present. The general rule is:
        // 1. If the Action Wheel is visible, pressing anything outside the wheel will hide it
        // 2. If the Action Wheel is not shown
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    // If the action wheel is available, we need to filter pointer events
                    // to prevent weird behavior. The following logic applies:
                    //
                    // 1. If clicking an Action Wheel button, it will consume the event.
                    // 2. If clicking outside the wheel when it is visible, it will
                    //    hide the wheel. The click should be consumed here.
                    // 3. Hover events should alway be allowed through




                    // 1. If the Wheel is visible, we block all press/release events. Only hover
                    //    events are allowed. Press/Release are used to hide the wheel (if allowed)
                    //we block all pointer events
                    // that could trigger a click
                    awaitPointerEventScope {
                        var pressWhenVisible = false // Track press, so we can filter Release correctly
                        while (true) {
                            val e = awaitPointerEvent()
                            // Action Wheel Buttons might already have consumed the event, which we need to respect here.
                            if (e.changes.any { it.isConsumed }) continue
                            if (e.buttons.isSecondaryPressed) continue
                            when (e.type) {
                                PointerEventType.Press -> {
                                    // Press is allowed to reach lower layers when the wheel isn't visible
                                    if (vm.sharedFieldData.isContentMenuVisible) {
                                        pressWhenVisible = true
                                        if (inputDialog.viewModel.hideOnClickedOutside) {
                                            dialogData?.viewModel?.hideWheel(false)
                                        }
                                        e.changes.forEach { it.consume() }
                                    } else {
                                        pressWhenVisible = false
                                    }
                                }
                                PointerEventType.Release -> {
                                    if (pressWhenVisible) {
                                        e.changes.forEach { it.consume() }
                                        pressWhenVisible = false
                                    }
                                }
                            }
                        }
                    }
                }
        ) {
            ActionWheelDialog(vm, fieldData, inputDialog)
        }
    }
}

