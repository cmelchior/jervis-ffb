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
import com.jervisffb.ui.game.dialogs.wheel.isHiding
import com.jervisffb.ui.game.view.ActionWheelDialog
import com.jervisffb.ui.game.view.ActionWheelUiState
import com.jervisffb.ui.game.view.NoActionWheel
import com.jervisffb.ui.game.viewmodel.FieldViewModel

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

    // When the Action-Wheel is visible, intercept all clicks to prevent them from reaching other layers.
    val localField = LocalFieldData.current
    DisposableEffect(currentState) {
        if (currentState != NoActionWheel && !currentState.isHiding()) {
            val interceptor = object : PointerEventInterceptor {
                override fun onPress(square: FieldCoordinate, isPrimary: Boolean): Boolean {
                    if (!isPrimary) return false
                    if (hideWhenClickOutside) {
                        vm.actionWheelViewModel.hideWheel(currentState.onDismiss)
                    }
                    return true
                }
            }
            localField.pointerBus.addInterceptor(interceptor)
            onDispose { localField.pointerBus.removeInterceptor(interceptor) }
        } else {
            onDispose { /* Do nothing */ }
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
            currentState,
            vm,
            fieldData,
        )
    }
}

