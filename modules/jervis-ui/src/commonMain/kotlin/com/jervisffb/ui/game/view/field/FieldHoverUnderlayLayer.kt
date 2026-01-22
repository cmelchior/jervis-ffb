package com.jervisffb.ui.game.view.field

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.jervis.generated.SettingsKeys
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.ui.SETTINGS_MANAGER
import com.jervisffb.ui.game.view.ActionWheelUiStateData
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.view.ShowActionWheel
import com.jervisffb.ui.game.viewmodel.FieldViewModel
import kotlinx.coroutines.launch

/**
 * Layer 5: Hover Underlay:
 *
 * This layer controls background highlights for when the mouse hovers over the
 * square.
 *
 * TODO: Should this behave differently for Giants which are bigger than one square?
 *
 * See [Field] for more details about layer ordering.
 */
@Composable
fun FieldHoverUnderlayLayer(
    vm: FieldViewModel,
) {
    val fieldSizeData = LocalFieldData.current.size
    val highlightedSquare: FieldCoordinate? by vm.highlights.collectAsState()
    val showHover by SETTINGS_MANAGER.observeBooleanKey(SettingsKeys.JERVIS_UI_SHOW_MOUSE_OVER_EFFECT_ON_SQUARE_VALUE, true).collectAsState(true)
    // When the Action Wheel is showing, we do not want to show the hover effect (regardless of it being enabled or not)
    // as it looks confusing with too many things happening at once. The onHover on players will still trigger to show
    // player cards.
    var showingPrimaryWheel by remember { mutableStateOf(false) }
    val showingSecondaryWheel by remember { vm.sharedFieldData.isContentMenuVisible }
    LaunchedEffect(vm) {
        launch {
            vm.actionWheelViewModel.observe().collect {
                showingPrimaryWheel = when (it) {
                    is ActionWheelUiStateData,
                    ShowActionWheel -> true
                    else -> false
                }
            }
        }
    }

    val isShowingActionWheel = showingPrimaryWheel || showingSecondaryWheel
    if (!isShowingActionWheel && highlightedSquare != null && showHover && highlightedSquare!!.isOnField(vm.game.rules)) {
        Box(
            modifier = Modifier
                .jervisSquare(fieldSizeData, highlightedSquare!!)
                .background(JervisTheme.hoverColor)
        )
    }
}
