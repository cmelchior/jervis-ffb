package com.jervisffb.ui.game.view.field

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import com.jervisffb.ui.game.dialogs.wheel.isHiding
import com.jervisffb.ui.game.view.ActionWheelDialog
import com.jervisffb.ui.game.view.ActionWheelUiState
import com.jervisffb.ui.game.view.NoActionWheel
import com.jervisffb.ui.game.viewmodel.FieldViewModel
import com.jervisffb.ui.utils.applyIf

/**
 * Layer 12: Action Wheel.
 *
 * This layer is responsible for handling the Action Wheel. It must also intercept
 * and filter pointer events when the Action Wheel is present.
 *
 * See [Field] for more details about layer ordering.
 */
@Composable
fun ActionWheelLayer(vm: FieldViewModel) {
    val fieldData by vm.fieldViewData.collectAsState()
    var currentState by remember { mutableStateOf<ActionWheelUiState>(NoActionWheel) }
    var hideWhenClickOutside by remember(vm.actionWheelViewModel.hideOnClickedOutside) { vm.actionWheelViewModel.hideOnClickedOutside }
    LaunchedEffect(vm) {
        vm.actionWheelViewModel.observe().collect {
            currentState = it
        }
    }

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
            .applyIf(currentState != NoActionWheel && !currentState.isHiding()) {
                pointerInput(currentState) {
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
                                    if (currentState != NoActionWheel && !currentState.isHiding() /*vm.sharedFieldData.isActionWheelVisible.value*/) {
                                        pressWhenVisible = true
                                        vm.actionWheelViewModel.let {
                                            if (hideWhenClickOutside) {
                                                it.hideWheel(currentState.onDismiss)
                                            }
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
            currentState,
            vm,
            fieldData,
        )
    }
}

