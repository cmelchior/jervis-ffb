package com.jervisffb.ui.game.view.field

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.ui.game.view.ActionWheelDialog
import com.jervisffb.ui.game.view.ActionWheelUiState
import com.jervisffb.ui.game.view.NoActionWheel
import com.jervisffb.ui.game.view.NoActionWheel.hideWhenClickOutside
import com.jervisffb.ui.game.viewmodel.FieldViewModel

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
    var currentState by remember { mutableStateOf<ActionWheelUiState>(NoActionWheel) }

    LaunchedEffect(vm) {
        vm.contextActionWheelViewModel.observe().collect {
            currentState = it
        }
    }

    // Context menu visibility is different than the Action Wheel. Can we be sure
    // that this always works? I suspect so, since we also check if the main action wheel is present
    // We create an invisible layer for intercepting pointer events when the Action Wheel
    // is present.

    // When the Context Action-Wheel is visible, intercept all clicks to prevent them from reaching other layers.
    val localField = LocalFieldData.current
    DisposableEffect(contextActionWheelPresent) {
        if (contextActionWheelPresent) {
            val interceptor = object : PointerEventInterceptor {
                override fun onPress(square: FieldCoordinate, isPrimary: Boolean): Boolean {
                    if (!isPrimary) return false
                    if (currentState.hideWhenClickOutside) {
                        vm.contextActionWheelViewModel.hideWheel()
                    }
                    return true
                }
            }
            localField.pointerBus.addInterceptor(interceptor)
            onDispose { localField.pointerBus.removeInterceptor(interceptor) }
        } else {
            onDispose {}
        }
    }

    // We cannot animate visibility here, either using `AnimatedVisibility` or custom alpha.
    // The reason eludes me a bit, but the animation ends up being clipped. Probably because
    // Compose creates a custom layer for animations, and somehow it doesn't get the correct
    // dimensions. This requires further investigation. Moving the animation into the Action Wheel
    // itself fixes the issue.
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        ActionWheelDialog(
            uiState = currentState,
            vm,
            fieldData,
        )
    }
}

