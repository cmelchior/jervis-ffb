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
import com.jervisffb.ui.game.view.ActionWheelDialog
import com.jervisffb.ui.game.view.ActionWheelUiStateData
import com.jervisffb.ui.game.viewmodel.FieldViewModel

/**
 * Layer 12: Action Wheel.
 *
 * This layer is responsible for handling the Action Wheel.
 *
 * See [Field] for more details about layer ordering.
 */
@Composable
fun ActionWheelLayer(vm: FieldViewModel) {
    val fieldData by vm.fieldViewData.collectAsState()
    // We create an invisible layer for intercepting pointer events when the Action Wheel
    // is present.

    var currentState by remember { mutableStateOf<ActionWheelUiStateData?>(null) }
    var showWheel = vm.sharedFieldData.isActionWheelVisible
    var hideWhenClickOutside by remember(vm.actionWheelViewModel.hideOnClickedOutside) { vm.actionWheelViewModel.hideOnClickedOutside }
    LaunchedEffect(vm) {
        vm.actionWheelViewModel.observe().collect {
            when (it) {
                is ActionWheelUiStateData -> {
                    currentState = it
                }
                null -> {
                    currentState = null
                }
                else -> { /* Do nothing */ }
            }
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
    if (!showWheel.value || currentState == null) {
        return
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(vm.actionWheelViewModel.version) {
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
                                if (vm.sharedFieldData.isActionWheelVisible.value) {
                                    pressWhenVisible = true
                                    vm.actionWheelViewModel.let {
                                        if (hideWhenClickOutside) {
                                            it.hideWheel(true, currentState?.onDismiss)
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

